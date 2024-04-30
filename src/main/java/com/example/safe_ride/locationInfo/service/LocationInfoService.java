package com.example.safe_ride.locationInfo.service;

import com.example.safe_ride.locationInfo.dto.BicycleInfoDto;
import com.example.safe_ride.locationInfo.dto.CombinedInfoDto;
import com.example.safe_ride.locationInfo.dto.StationInfoDto;
import com.example.safe_ride.locationInfo.dto.TotalInfoDto;
import com.example.safe_ride.locationInfo.entity.TempCombinedInfo;
import com.example.safe_ride.locationInfo.repo.TempCombinedInfoRepo;
import com.example.safe_ride.locationInfo.repo.TempLocationInfoRepo;
import com.example.safe_ride.safe.dto.PointDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationInfoService {
    private final StationMapManager mapManager;
    private final PublicApiService apiService;
    private final TempTableService tempService;
    private final TempCombinedInfoRepo tempRepo;
    private final TempLocationInfoRepo tempLocationInfoRepo;


    // 1. 데이터 패치
    public List<?> fetchData(
            String status, PointDto pointDto, String apiUrl, String lcgvmnInstCd) throws IOException {
        // map 초기화
        if ("station".equals(status)) mapManager.clearMap();

        // totalCount 기반 동적 데이터 생성
        int totalCount = apiService.getTotalCntData(apiUrl, lcgvmnInstCd);
        int numOfRows = 700;
        int totalPages = (totalCount + numOfRows - 1) / numOfRows;

        // 모든 결과 데이터를 저장할 List
        List<Object> allResults = new ArrayList<>();

        // 각 페이지 별로 데이터 요청 및 처리
        for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
            String resultUrl = apiService.buildUrl(apiUrl, lcgvmnInstCd, pageNo, numOfRows);
            processPageData(status, pointDto, resultUrl, allResults, pageNo);
        }
        return allResults;
    }

    // 2. 각 페이지 데이터를 처리
    public void processPageData(
            String status, PointDto pointDto, String url, List<Object> allResults, int pageNo) throws IOException {
        JSONObject jsonResponse = apiService.fetchJsonResponse(url);

        List<?> result = null;

        //필터링
        if ("station".equals(status)) {
            result = filterStationsByPoint(pointDto, jsonResponse);
        } else if ("bicycle".equals(status)) {
            Map<String, JSONObject> stationIdMap = mapManager.getStationMap();
            result = filterBicycleByStationId(stationIdMap, jsonResponse);
        }

        if (result != null && !result.isEmpty()) {
            allResults.addAll(result);
        } else {
            log.info("페이지 " + pageNo + "에는 유효한 데이터가 없습니다.");
        }
    }


    // 3-1. 추출된 좌표로 일치하는 대여소 현황을 API 응답으로 부터 필터링
    // 좌표를 기준으로 필터링 하여 데이터 파싱 (대여소 현황)
    public List<StationInfoDto> filterStationsByPoint(PointDto pointDto, JSONObject jsonResponse) throws JSONException {
        JSONArray items = jsonResponse.getJSONObject("body").getJSONArray("item");

        List<StationInfoDto> FilteredResult = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            // 위도 경도가 반경안에 포함하는지 확인
            if (apiService.isWithinRange(item, pointDto)) {
                FilteredResult.add(formatStation(item, pointDto));
                mapManager.updateMap(item.getString("rntstnId"), item);
            }
        }
        return FilteredResult;
    }

    // 3-2. 추출된 대여소 ID로 일치하는 자전거 현황을 API 응답으로 부터 필터링
    // 대여소 아이디를 기준으로 필터링 하여 데이터 파싱 (자전거 현황)
    public List<BicycleInfoDto> filterBicycleByStationId(Map<String, JSONObject> stationMap, JSONObject jsonResponse) throws JSONException {
        JSONArray items = jsonResponse.getJSONObject("body").getJSONArray("item");

        List<BicycleInfoDto> results = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String rntstnId = item.getString("rntstnId");

            // 대여소Id가 일치하는지 확인
            if (stationMap.containsKey(rntstnId)) {
                results.add(formatBicycle(item)); // 조건에 맞으면 item에 대한 포맷팅 진행
            }
        }
        return results;
    }


    // 4-1. 필터링 된 데이터 포매팅 (대여소 현황) + 거리 계산 데이터 추가
    private StationInfoDto formatStation(JSONObject item, PointDto pointDto) throws JSONException {
        double distance = apiService.calculateDistance(item, pointDto.getLat(), pointDto.getLng());
        item.put("distance", distance); // CombinedInfo를 만들 때 Map value가 JsonObject 타입으로 JSON 데이터에도 넣어줘야한다.
        return StationInfoDto.builder()
                .bcyclDataCrtrYmd(item.getString("bcyclDataCrtrYmd"))           // 관리기관명(서울특별시)
                .mngInstNm(item.getString("mngInstNm"))                         // 데이터기준일자(2024-02-08)
                .lcgvmnInstCd(item.getString("lcgvmnInstCd"))                   // 지자체 코드(1100000000)
                .lcgvmnInstNm(item.getString("lcgvmnInstNm"))                   // 지자체명(서울특별시)
                .rntstnId(item.getString("rntstnId"))                           // 대여소 아이디(ST-10)
                .rntstnNm(item.getString("rntstnNm"))                           // 자전거대여소명(108. 서교동 사거리)
                .roadNmAddr(item.getString("roadNmAddr"))                       // 소재지도로명주소(서울특별시 마포구 양화로 93 427)
                .lotnoAddr(item.getString("lotnoAddr"))                         // 소재지지번주소(서울특별시 마포구 서교동 378-5)
                .lat(item.getString("lat"))                                     // 위도(37.5527458200)
                .lot(item.getString("lot"))                                     // 경도(126.9186172500)
                .operBgngHrCn(item.getString("operBgngHrCn"))                   // 운영시작시각(00:00)
                .operEndHrCn(item.getString("operEndHrCn"))                     // 운영종료시각(23:59)
                .rpfactInstlYn(item.getString("rpfactInstlYn"))                 // 수리대설치여부(N)
                .arinjcInstlYn(item.getString("arinjcInstlYn"))                 // 공기주입기비치여부(N)
                .arinjcTypeNm(item.getString("arinjcTypeNm"))                   // 공기주입기유형(FV)
                .rntstnFcltTypeNm(item.getString("rntstnFcltTypeNm"))           // 자전거대여소구분(무인)
                .rntstnOperDayoffDayCn(item.getString("rntstnOperDayoffDayCn")) // 휴무일(연중무휴)
                .rntFeeTypeNm(item.getString("rntFeeTypeNm"))                   // 요금구분(유료)
                .mngInstTelno(item.getString("mngInstTelno"))                   // 관리기관전화번호(https://data.seoul.go.kr)
                .distance(distance)                                  // 검색 대상과의 거리
                .build();
    }

    // 4-2. 필터링 된 데이터 포매팅 (자전거 현황)
    public BicycleInfoDto formatBicycle(JSONObject item) throws JSONException {
        return BicycleInfoDto.builder()
                .lcgvmnInstCd(item.getString("lcgvmnInstCd"))
                .lcgvmnInstNm(item.getString("lcgvmnInstNm"))
                .rntstnId(item.getString("rntstnId"))
                .rntstnNm(item.getString("rntstnNm"))
                .lat(item.getString("lat"))
                .lot(item.getString("lot"))
                .bcyclTpkctNocs(item.optInt("bcyclTpkctNocs", 0))
                .build();
    }


    // 5-1. StationInfo와 BicycleInfo 리스트를 받아서 CombinedInfoDto 리스트를 생성하는 메소드
    public List<CombinedInfoDto> createCombinedInfo(List<BicycleInfoDto> bicycleList) {

        List<CombinedInfoDto> combinedList = new ArrayList<>();
        Map<String, JSONObject> stationMap = mapManager.getStationMap();

        // stationList를 기준으로 bcyclTpkctNocs를 매칭하여 CombinedInfoDto를 생성하고 리스트에 추가
        for (BicycleInfoDto bicycle : bicycleList) {
            JSONObject station = stationMap.get(bicycle.getRntstnId());
            log.info("JsonObject test :{}", station);

                if (station != null) {
                    try {
                    CombinedInfoDto combinedInfo = CombinedInfoDto.builder()
                            .bcyclDataCrtrYmd(station.getString("bcyclDataCrtrYmd"))           // 데이터기준일자(2024-02-08)
                            .mngInstNm(station.getString("mngInstNm"))                         // 관리기관명(서울특별시)
                            .lcgvmnInstCd(station.getString("lcgvmnInstCd"))                   // 지자체 코드(1100000000)
                            .lcgvmnInstNm(station.getString("lcgvmnInstNm"))                   // 지자체명(서울특별시)
                            .rntstnId(station.getString("rntstnId"))                           // 대여소 아이디(ST-10)
                            .rntstnNm(station.getString("rntstnNm"))                           // 자전거대여소명(108. 서교동 사거리)
                            .roadNmAddr(station.getString("roadNmAddr"))                        // 소재지도로명주소(서울특별시 마포구 양화로 93 427)
                            .lat(station.getString("lat"))                                 // 위도(37.5527458200)
                            .lot(station.getString("lot"))                                     // 경도(126.9186172500)
                            .operBgngHrCn(station.getString("operBgngHrCn"))                   // 운영시작시각(00:00)
                            .operEndHrCn(station.getString("operEndHrCn"))                     // 운영종료시각(23:59)
                            .rpfactInstlYn(station.getString("rpfactInstlYn"))                 // 수리대설치여부(N)
                            .arinjcInstlYn(station.getString("arinjcInstlYn"))                 // 공기주입기비치여부(N)
                            .arinjcTypeNm(station.getString("arinjcTypeNm"))                   // 공기주입기유형(FV)
                            .rntstnFcltTypeNm(station.getString("rntstnFcltTypeNm"))           // 자전거대여소구분(무인)
                            .rntstnOperDayoffDayCn(station.getString("rntstnOperDayoffDayCn")) // 휴무일(연중무휴)
                            .rntFeeTypeNm(station.getString("rntFeeTypeNm"))                   // 요금구분(유료)
                            .mngInstTelno(station.getString("mngInstTelno"))                   // 관리기관전화번호(https://data.seoul.go.kr)
                            .bcyclTpkctNocs(bicycle.getBcyclTpkctNocs())
                            .distance(station.getDouble("distance"))
                            .build();
                        combinedList.add(combinedInfo);
                    } catch (JSONException e) {
                        log.error("JSON 파싱에 실패했습니다.", e);
                }
            }
        }
        saveCombinedInfo(combinedList);
        return combinedList;
    }

    // plus. bcyclTpkctNocs를 정수로 변환
    private int parseBicycleCount(String bcyclTpkctNocs) {
        try {
            return Integer.parseInt(bcyclTpkctNocs);
        } catch (NumberFormatException e) {
            return 0; // 변환 실패 시 0으로 설정
        }
    }

    // 5-2. 생성된 List를 TempCombined 엔티티로 변환하고 리스트를 임시DB에 저장
    public void saveCombinedInfo(List<CombinedInfoDto> combinedList) {
        List<TempCombinedInfo> tempInfoList = combinedList.stream()
                .map(CombinedInfoDto::toEntity)
                .collect(Collectors.toList());
        tempService.insertData(tempInfoList);
    }

    // 5-3. TempCombined 테이블에 저장된 값 불러오기
    public Page<TempCombinedInfo> readTempCombinedInfo(Pageable pageable) {
        return tempRepo.findAll(pageable);
    }

    // 🔖임시테이블 생성 최종 메서드
    public List <CombinedInfoDto> processData (PointDto pointDto) throws IOException {
        // step 1. 요청 필요 파라미터 정의
        String stationUrl = "inf_101_00010001";
        String bicycleUrl = "inf_101_00010002";
        String jijacheCode = apiService.getLocationInfo(pointDto).getTransCode();

        // step 2. API로부터 데이터 패치
        List<StationInfoDto> fetchStation = (List<StationInfoDto>) fetchData("station", pointDto, stationUrl, jijacheCode);
        List<BicycleInfoDto> fetchbicycle = (List<BicycleInfoDto>) fetchData("bicycle", pointDto, bicycleUrl, jijacheCode);

        // step 3. 정제된 데이터로 최종 데이터 생성
        return createCombinedInfo(fetchbicycle);
    }

    // 생성된 임시테이블의 ToTal 정보를 구하는 메서드
    public TotalInfoDto getTotalInfo() {
        List<TempCombinedInfo> allInfos = tempRepo.findAll();
        int totalStation = allInfos.size();
        int totalBicycle = allInfos.stream()
                .filter(info -> info.getBcyclTpkctNocs() != 0)
                .mapToInt(info -> info.getBcyclTpkctNocs())
                .sum();
        return new TotalInfoDto(totalStation, totalBicycle);
    }
}
