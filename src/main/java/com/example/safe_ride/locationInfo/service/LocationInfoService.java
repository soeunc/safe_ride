package com.example.safe_ride.locationInfo.service;

import com.example.safe_ride.locationInfo.dto.BicycleInfo;
import com.example.safe_ride.locationInfo.dto.StationInfo;
import com.example.safe_ride.locationInfo.entity.LocationInfo;
import com.example.safe_ride.locationInfo.repo.LocationInfoRepo;
import com.example.safe_ride.safe.dto.PointDto;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoNcpResponse;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoResponseDto;
import com.example.safe_ride.safe.dto.rgeocoding.RGoCode;
import com.example.safe_ride.safe.service.NcpMapApiService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationInfoService {
    private final LocationInfoRepo locationInfoRepo;
    private final StationMapManager mapManager;
    private final NcpMapApiService mapApiService;

    private static final String BASE_URL = "https://apis.data.go.kr/B551982/pbdo"; // baseURL
    // Encoding 인증키
    private static final String SERVICE_KEY = "z88eBNe%2B8cP%2BJbPk%2BPcjoRg1biqWAPB%2B1oH6sqToU4SqwZJzkUMKuWZKQpRolY8gg6nISJeqtLLDv5I7COksvw%3D%3D"; // 발급받은 인증키
    // Decoding 인증키
    // private static final String SERVICE_KEY = "z88eBNe+8cP+JbPk+PcjoRg1biqWAPB+1oH6sqToU4SqwZJzkUMKuWZKQpRolY8gg6nISJeqtLLDv5I7COksvw=="; // 발급받은 인증키

    // 1. totalCount를 받아오기 위한 초기 URL 설정 메서드
    public String buildInitUrlpublic(String apiUrl, String lcgvmnInstCd, String fromCrtrYmd, String toCrtrYmd) throws UnsupportedEncodingException {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        urlBuilder.append("/").append(apiUrl);
        urlBuilder.append("?serviceKey=").append(SERVICE_KEY);
        urlBuilder.append("&pageNo=").append(URLEncoder.encode("1", "UTF-8")); // page Num
        urlBuilder.append("&numOfRows=").append(URLEncoder.encode("1", "UTF-8")); // 한페이지당 데이터 수
        urlBuilder.append("&type=").append(URLEncoder.encode("json", "UTF-8")); // 데이터 형식 (xml or json)
        urlBuilder.append("&lcgvmnInstCd=").append(URLEncoder.encode(lcgvmnInstCd, "UTF-8"));

        return urlBuilder.toString();
    }

    // 2. totalCount를 받아오는 메서드
    public int getTotalCntData(String apiUrl, String lcgvmnInstCd) throws IOException, JSONException {
        String initUrl = buildInitUrlpublic(apiUrl, lcgvmnInstCd, null, null);
        URL url = new URL(initUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            JSONObject jsonResponse = new JSONObject(response.toString());
            log.info("total Count : " + jsonResponse.getJSONObject("body").getString("totalCount"));
            return Integer.parseInt(jsonResponse.getJSONObject("body").getString("totalCount"));
        } finally {
            connection.disconnect();
        }
    }

    // 3. 공통 URL 설정 메서드 (total count를 받아 최종 result Url 생성)
    public String buildUrl(String apiUrl, String lcgvmnInstCd, String fromCrtrYmd, String toCrtrYmd, int pageNo, int numOfRows) throws UnsupportedEncodingException {
        log.info("pageNo :" + pageNo + " / numOfRows :" + numOfRows);
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        urlBuilder.append("/").append(apiUrl);
        urlBuilder.append("?serviceKey=").append(SERVICE_KEY);
        urlBuilder.append("&pageNo=").append(URLEncoder.encode(String.valueOf(pageNo), "UTF-8")); // page Num
        urlBuilder.append("&numOfRows=").append(URLEncoder.encode(String.valueOf(numOfRows), "UTF-8")); // 한페이지당 데이터 수
        urlBuilder.append("&type=").append(URLEncoder.encode("json", "UTF-8")); // 데이터 형식 (xml or json)
        urlBuilder.append("&lcgvmnInstCd=").append(URLEncoder.encode(lcgvmnInstCd, "UTF-8"));

        // 실시간 대여/반납 정보는 매개변수 추가
        // 대여/반납 정보는 불필요하므로 실질적으로 사용하지 않는 상태임
        if (fromCrtrYmd != null && toCrtrYmd != null) {
            fromCrtrYmd = "20240401"; // 임시 (폼에서 받아와야함)
            toCrtrYmd = "20240402"; // 임시 (폼에서 받아와야함)
            urlBuilder.append("&fromCrtrYmd=").append(URLEncoder.encode(fromCrtrYmd, "UTF-8"));
            urlBuilder.append("&toCrtrYmd=").append(URLEncoder.encode(toCrtrYmd, "UTF-8"));
        }
        return urlBuilder.toString();
    }


    // 도로명주소에 대한 결과 리스트 반환
    public List<StationInfo> fetchStationData(String doroJuso, String apiUrl, String lcgvmnInstCd, String fromCrtrYmd, String toCrtrYmd) throws IOException {
        // map 초기화
        mapManager.clearMap();
        // totalCount 기반 동적 데이터 생성
        int totalCount = getTotalCntData(apiUrl, lcgvmnInstCd);
        int numOfRows = 700;
        // 필요한 전체 페이지 수 계산
        int totalPages = (totalCount + numOfRows - 1) / numOfRows;

        // 모든 결과를 저장할 List
        List<StationInfo> allResults = new ArrayList<>();

        // url로 http 응답 받아오기
        for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
            String resultUrl = buildUrl(apiUrl, lcgvmnInstCd, fromCrtrYmd, toCrtrYmd, pageNo, numOfRows);

            URL url = new URL(resultUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-type", "application/json");

            int responseCode = connection.getResponseCode();
            // 응답 상태가 좋으면 받아온 http를 읽고 문자열로 조합
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // try-with-resources문 :: 자동으로 버퍼리더 닫힘 (마지막 데이터 이후 오류 발생 문제)
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    StringBuilder response = new StringBuilder();
                    // 데이터 없을 때까지 반복
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    // 페이지 데이터를 파싱하여 결과 리스트에 추가
                    List<StationInfo> stationResults = filterStationsByDoroJuso(doroJuso, response.toString());
                    if (!stationResults.isEmpty()) {
                        allResults.addAll(stationResults);
                    } else {
                        log.info("페이지 " + pageNo + "에서 유효한 데이터가 없습니다.");
                    }
                }
                // 응답 코드가 HTTP_OK가 아닌 경우 예외 처리
            } else {
                throw new IOException("서버 응답 오류 -> HTTP 상태 코드 : " + responseCode);
            }
        }
        // 한 페이지당 파싱
        return allResults;
    }

    // 대여소 아이디에 대한 결과 리스트 반환
    public List<BicycleInfo> fetchStationAndBicycleData(String apiUrl, String lcgvmnInstCd, String fromCrtrYmd, String toCrtrYmd) throws IOException, JSONException {
        // 대여소 아이디 Map 가져오기
        Map<String, JSONObject> stationIdMap = mapManager.getStationMap();
        // totalCount 기반 동적 데이터 생성
        int totalCount = getTotalCntData(apiUrl, lcgvmnInstCd);
        int numOfRows = 700;
        // 필요한 전체 페이지 수 계산
        int totalPages = (totalCount + numOfRows - 1) / numOfRows;

        // 모든 결과를 저장할 List
        List<BicycleInfo> allResults = new ArrayList<>();

        // url로 http 응답 받아오기
        for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
            String resultUrl = buildUrl(apiUrl, lcgvmnInstCd, fromCrtrYmd, toCrtrYmd, pageNo, numOfRows);

            URL url = new URL(resultUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-type", "application/json");

            int responseCode = connection.getResponseCode();
            // 응답 상태가 좋으면 받아온 http를 읽고 문자열로 조합
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // try-with-resources문 :: 자동으로 버퍼리더 닫힘 (마지막 데이터 이후 오류 발생 문제)
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    StringBuilder response = new StringBuilder();
                    // 데이터 없을 때까지 반복
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // 페이지 데이터를 파싱하여 결과 리스트에 추가
                    List<BicycleInfo> bicycleResults = filterBicycleByStationId(stationIdMap, response.toString());
                    if (!bicycleResults.isEmpty()) {
                        allResults.addAll(bicycleResults);
                    } else {
                        log.info("페이지 " + pageNo + "에서 유효한 데이터가 없습니다.");
                    }
                }
                // 응답 코드가 HTTP_OK가 아닌 경우 예외 처리
            } else {
                throw new IOException("서버 응답 오류 -> HTTP 상태 코드 : " + responseCode);
            }
        }
        // 한 페이지당 파싱
        return allResults;
    }

    // 위치 좌표에 대한 대여소 결과 리스트 반환
    public List<StationInfo> fetchPointData(Double lat, Double lot, String apiUrl, String lcgvmnInstCd, String fromCrtrYmd, String toCrtrYmd) throws IOException {
        // map 초기화
        mapManager.clearMap();
        // totalCount 기반 동적 데이터 생성
        int totalCount = getTotalCntData(apiUrl, lcgvmnInstCd);
        int numOfRows = 700;
        // 필요한 전체 페이지 수 계산
        int totalPages = (totalCount + numOfRows - 1) / numOfRows;

        // 모든 결과를 저장할 List
        List<StationInfo> allResults = new ArrayList<>();

        // url로 http 응답 받아오기
        for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
            String resultUrl = buildUrl(apiUrl, lcgvmnInstCd, fromCrtrYmd, toCrtrYmd, pageNo, numOfRows);

            URL url = new URL(resultUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-type", "application/json");

            int responseCode = connection.getResponseCode();
            // 응답 상태가 좋으면 받아온 http를 읽고 문자열로 조합
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // try-with-resources문 :: 자동으로 버퍼리더 닫힘 (마지막 데이터 이후 오류 발생 문제)
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    StringBuilder response = new StringBuilder();
                    // 데이터 없을 때까지 반복
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    // 페이지 데이터를 파싱하여 결과 리스트에 추가
                    List<StationInfo> pointResults = filterStationsByPoint(lat, lot, response.toString());
                    if (!pointResults.isEmpty()) {
                        allResults.addAll(pointResults);
                    } else {
                        log.info("페이지 " + pageNo + "에서 유효한 데이터가 없습니다.");
                    }
                }
                // 응답 코드가 HTTP_OK가 아닌 경우 예외 처리
            } else {
                throw new IOException("서버 응답 오류 -> HTTP 상태 코드 : " + responseCode);
            }
        }
        // 한 페이지당 파싱
        return allResults;
    }
    // 주소를 기준으로 필터링 하여 데이터 파싱 (대여소 현황)
    public List<StationInfo> filterStationsByDoroJuso(String doroJuso, String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.getJSONObject("body").getJSONArray("item");

        List<StationInfo> FilteredResult = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            // 도로명 주소가 포함하는지 확인
            if (item.getString("roadNmAddr").contains(doroJuso)) {
                FilteredResult.add(formatStation(item));
                mapManager.updateMap(item.getString("rntstnId"), item);
            }
        }
        return FilteredResult;
    }

    // 좌표를 기준으로 필터링 하여 데이터 파싱 (대여소 현황)
    public List<StationInfo> filterStationsByPoint(double lat, double lot, String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.getJSONObject("body").getJSONArray("item");

        List<StationInfo> FilteredResult = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            // 위도 경도가 반경안에 포함하는지 확인
            if (isWithinRange(item, lat, lot)) {
                FilteredResult.add(formatStation(item));
                mapManager.updateMap(item.getString("rntstnId"), item);
            }
        }
        return FilteredResult;
    }

    // 데이터 포매팅 (대여소 현황)
    private StationInfo formatStation(JSONObject item) throws JSONException {
        String bcyclDataCrtrYmd = item.getString("bcyclDataCrtrYmd");           // 관리기관명(서울특별시)
        String mngInstNm = item.getString("mngInstNm");                         // 데이터기준일자(2024-02-08)
        String lcgvmnInstCd = item.getString("lcgvmnInstCd");                   // 지자체 코드(1100000000)
        String lcgvmnInstNm = item.getString("lcgvmnInstNm");                   // 지자체명(서울특별시)
        String rntstnId = item.getString("rntstnId");                           // 대여소 아이디(ST-10)
        String rntstnNm = item.getString("rntstnNm");                           // 자전거대여소명(108. 서교동 사거리)
        String roadNmAddr = item.getString("roadNmAddr");                       // 소재지도로명주소(서울특별시 마포구 양화로 93 427)
        String lotnoAddr = item.getString("lotnoAddr");                         // 소재지지번주소(서울특별시 마포구 서교동 378-5)
        String lat = item.getString("lat");                                     // 위도(37.5527458200)
        String lot = item.getString("lot");                                     // 경도(126.9186172500)
        String operBgngHrCn = item.getString("operBgngHrCn");                   // 운영시작시각(00:00)
        String operEndHrCn = item.getString("operEndHrCn");                     // 운영종료시각(23:59)
        String rpfactInstlYn = item.getString("rpfactInstlYn");                 // 수리대설치여부(N)
        String arinjcInstlYn = item.getString("arinjcInstlYn");                 // 공기주입기비치여부(N)
        String arinjcTypeNm = item.getString("arinjcTypeNm");                   // 공기주입기유형(FV)
        String rntstnFcltTypeNm = item.getString("rntstnFcltTypeNm");           // 자전거대여소구분(무인)
        String rntstnOperDayoffDayCn = item.getString("rntstnOperDayoffDayCn"); // 휴무일(연중무휴)
        String rntFeeTypeNm = item.getString("rntFeeTypeNm");                   // 요금구분(유료)
        String mngInstTelno = item.getString("mngInstTelno");                   // 관리기관전화번호(https://data.seoul.go.kr)

        return new StationInfo(bcyclDataCrtrYmd, mngInstNm, lcgvmnInstCd, lcgvmnInstNm, rntstnId, rntstnNm, roadNmAddr, lotnoAddr, lat, lot,
                operBgngHrCn, operEndHrCn, rpfactInstlYn, arinjcInstlYn, arinjcTypeNm, rntstnFcltTypeNm, rntstnOperDayoffDayCn, rntFeeTypeNm, mngInstTelno);
    }

    // 추출된 대여소 ID로 일치하는 자전겨 현황을 API 응답으로 부터 필터링
    // 필터링 된 데이터 문자열 리스트 (Client에게 전달할 Result Data Table)
    public List<BicycleInfo> filterBicycleByStationId(Map<String, JSONObject> stationMap, String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse); // 자전거 현황 데이터 가져옴
        JSONArray items = jsonObject.getJSONObject("body").getJSONArray("item"); // 자전거 body의 item을 확인

        List<BicycleInfo> results = new ArrayList<>(); // 결과를 담을 리저트

        for (int i = 0; i < items.length(); i++) { // 아이템 없을때까지 반복 (아이템 개당 수행)
            JSONObject item = items.getJSONObject(i); // item 변수에 items의 i번째 값을 담음
            String rntstnId = item.getString("rntstnId"); // rntstnId 변수에 item의 rntstnId를 담음

            // 대여소Id가 일치하는지 확인
            if (stationMap.containsKey(rntstnId)) {
                results.add(formatBicycle(item)); // 조건에 맞으면 item에 대한 포맷팅 진행
            }
        }
        return results;
    }

    // 필터링 된 데이터 문자열 생성
    public BicycleInfo formatBicycle(JSONObject item) throws JSONException {
        String lcgvmnInstCd = item.getString("lcgvmnInstCd");                       // 지자체 코드(1100000000)
        String lcgvmnInstNm = item.getString("lcgvmnInstNm");                       // 지자체명(서울특별시)
        String rntstnId = item.getString("rntstnId");                               // 대여소 아이디(ST-10)
        String rntstnNm = item.getString("rntstnNm");                               // 자전거대여소명(108. 서교동 사거리)
        String lat = item.getString("lat");                                         // 위도(37.5527458200)
        String lot = item.getString("lot");                                         // 경도(126.9186172500)
        String bcyclTpkctNocs = item.optString("bcyclTpkctNocs", "0");  //자전거 주차 총 건수(12)

        return new BicycleInfo(lcgvmnInstCd, lcgvmnInstNm, rntstnId, rntstnNm, lat, lot, bcyclTpkctNocs);
    }

    // 대여소 아이디에 해당하는 자전거 현황 찾기
    public BicycleInfo findBicycleInfoByStationId(String rntstnId, List<BicycleInfo> bicycleInfos) {
        return bicycleInfos.stream()
                .filter(b -> b.getRntstnId().equals(rntstnId))
                .findFirst()
                .orElse(null);  // 만약 찾지 못했다면 null을 반환
    }

    // 대여소 아이디에 해당하는 대여소 현황 찾기
    public StationInfo findStationByStationId(String rntstnId, List<StationInfo> stationInfos) {
        return stationInfos.stream()
                .filter(b -> b.getRntstnId().equals(rntstnId))
                .findFirst()
                .orElse(null);  // 만약 찾지 못했다면 null을 반환
    }


    // 주소 관련 서비스
    // 제출된 폼의 메인 도로명주소 추출 (ex. 충청남도 천안시 동남구 목천읍 독립기념관로)
    public String getDoroJuso(String roadAddrPart1) {
        /*
         *  정규 표현식을 컴파일
         * ^: 문자열의 시작
         * (.*?):가능한 적은 문자를 비탐욕적(non-greedy)으로 매칭
         * (\\d+)는 하나 이상의 숫자를 매칭
         */
        Pattern pattern = Pattern.compile("^(.*?)(\\d+)");
        Matcher matcher = pattern.matcher(roadAddrPart1);
        if (matcher.find()) log.info(matcher.group(1));
        return matcher.group(1);
    }

    // 추출한 도로명주소의 시도 추출하기
    public String getSido(String roadAddrPart1) {
        String doroJuso = getDoroJuso(roadAddrPart1);
        if (doroJuso == null || doroJuso.isEmpty()) {
            return "";
        }
        String[] parts = doroJuso.split(" ");
        return parts[0];  // 첫 번째 단어 반환
    }

    // 추출한 시도의 지역 코드 추출
    public String getAddressCode(String roadAddrPart1) {
        String sido = getSido(roadAddrPart1);
        List<LocationInfo> locations = locationInfoRepo.findBySido(sido);
        if (!locations.isEmpty()) {
            return locations.get(0).getAddressCode();
        }
        return null;  // 결과가 없으면 null 반환
    }

    // 전체 데이터 중에 시도 데이터 불러오기 (폼 제출용)
    public List<String> getSidoList() {
        return locationInfoRepo.findAll().stream()
                .map(LocationInfo::getSido)
                .distinct()
                .collect(Collectors.toList());
    }

    // 시도 중에 시군구 데이터 불러오기 (폼 제출용)
    public List<String> getSigunguBySidoList(String sido) {
        return locationInfoRepo.findAllBySido(sido).stream()
                .map(LocationInfo::getSigungu)
                .distinct()
                .collect(Collectors.toList());
    }

    // 시군구 중에 읍면동 데이터 불러오기 (폼 제출용)
    public List<String> getEupmyundongBySigunguList(String sigungu) {
        return locationInfoRepo.findAllBySigungu(sigungu).stream()
                .map(LocationInfo::getEupmyundong)
                .distinct()
                .collect(Collectors.toList());
    }

    // 위치 관련 서비스
    // 위도, 경도가 일정 범위 안에 있는지 (대략 5km) 확인
    public boolean isWithinRange(JSONObject item, double userLat, double userLot) {
        double lat = item.getDouble("lat");
        double lot = item.getDouble("lot");

        double latDistance = Math.abs(lat - userLat);
        double lotDistance = Math.abs(lot - userLot);

        // 위도 또는 경도 차이가 0.045도 이내인지 확인
        return latDistance <= 0.045 && lotDistance <= 0.045;
    }

    // 입력받은 좌표의 법정동코드 확인
    // reverse grocode를 사용해서 좌표를 입력받아 결과값을 세션으로 반환하는 메서드
    public RGeoResponseDto setTransBjDongCode(PointDto point) {
        Map<String, Object> params = new HashMap<>();
        params.put("coords", point.toQueryValue());
        params.put("orders", "legalcode"); // 법정동으로 변환 작업
        params.put("output", "json");
        log.info("coords : {}", params.get("coords"));
        RGeoNcpResponse response = mapApiService.reversGeocode(params);
        log.info("response : {}", response);
        RGoCode code = response.getResults()
                .get(0)
                .getCode();

        String bjDongCode = code.getId();
        String transBjDongCode = code.getId();

        if (bjDongCode.startsWith("11"))  transBjDongCode = "1100000000";
        else if (bjDongCode.startsWith("36")) transBjDongCode = "3611000000";
        else if (bjDongCode.startsWith("30")) transBjDongCode = "3000000000";
        else transBjDongCode = bjDongCode;
        return new RGeoResponseDto(transBjDongCode.trim());
    }
}
