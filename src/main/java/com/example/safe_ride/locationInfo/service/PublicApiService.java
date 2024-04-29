package com.example.safe_ride.locationInfo.service;

import com.example.safe_ride.locationInfo.dto.LocationInfoResponseDto;
import com.example.safe_ride.locationInfo.entity.TempLocationInfoResponse;
import com.example.safe_ride.locationInfo.repo.TempLocationInfoRepo;
import com.example.safe_ride.safe.dto.PointDto;
import com.example.safe_ride.safe.dto.geocoding.GeoNcpResponse;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoNcpResponse;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoResult;
import com.example.safe_ride.safe.service.NcpMapApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicApiService {
    private final NcpMapApiService mapApiService;
    private final TempLocationInfoRepo tempLocationInfoRepo;

    private static final String BASE_URL = "https://apis.data.go.kr/B551982/pbdo"; // baseURL
    private static final String SERVICE_KEY = "z88eBNe%2B8cP%2BJbPk%2BPcjoRg1biqWAPB%2B1oH6sqToU4SqwZJzkUMKuWZKQpRolY8gg6nISJeqtLLDv5I7COksvw%3D%3D"; // Encoding 인증키
    // private static final String SERVICE_KEY = "z88eBNe+8cP+JbPk+PcjoRg1biqWAPB+1oH6sqToU4SqwZJzkUMKuWZKQpRolY8gg6nISJeqtLLDv5I7COksvw==";     // Decoding 인증키

    /*
    공공데이터 포털 API : 대여소 현황, 자전거 현황 관련 서비스
    (패치 후 Dto, Entity로 관리하는 로직은 LocationInfoService에 있다)
    */
    // 1. URL 설정 메서드
    public String buildUrl(String apiUrl, String lcgvmnInstCd, int pageNo, int numOfRows) throws UnsupportedEncodingException {
        log.info("pageNo :" + pageNo + " / numOfRows :" + numOfRows);
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        urlBuilder.append("/").append(apiUrl);
        urlBuilder.append("?serviceKey=").append(SERVICE_KEY);
        urlBuilder.append("&pageNo=").append(URLEncoder.encode(String.valueOf(pageNo), "UTF-8")); // page Num
        urlBuilder.append("&numOfRows=").append(URLEncoder.encode(String.valueOf(numOfRows), "UTF-8")); // 한페이지당 데이터 수
        urlBuilder.append("&type=").append(URLEncoder.encode("json", "UTF-8")); // 데이터 형식 (xml or json)
        urlBuilder.append("&lcgvmnInstCd=").append(URLEncoder.encode(lcgvmnInstCd, "UTF-8"));

        return urlBuilder.toString();
    }

    // 2. totalCount를 받아오는 메서드
    public int getTotalCntData(String apiUrl, String lcgvmnInstCd) throws IOException, JSONException {
        String initUrl = buildUrl(apiUrl, lcgvmnInstCd, 1, 1);
        JSONObject jsonResponse = fetchJsonResponse(initUrl);
        log.info("total Count : " + jsonResponse.getJSONObject("body").getString("totalCount"));
        return Integer.parseInt(jsonResponse.getJSONObject("body").getString("totalCount"));
    }

    // 3. API 서버 데이터로부터 응답 결과를 받아오는 메서드
    JSONObject fetchJsonResponse(String url) throws IOException, JSONException {
        URL urlObject = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-type", "application/json");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) throw new IOException("HTTP 상태 코드 : " + responseCode);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return new JSONObject(response.toString());
        } finally {
            connection.disconnect();
        }
    }

    /** Geoloacation API : 위치 관련 서비스 */
    // 사용자 좌표 갱신
    public LocationInfoResponseDto getLocationInfo(Map<String, Double> payload) {
        double lng = payload.get("lng"); // 경도
        double lat = payload.get("lat"); // 위도
        PointDto pointDto = new PointDto(lng, lat);
        return getLocationInfo(pointDto);
    }

    // 위도, 경도가 일정 범위 안에 있는지 (대략 2km) 확인

    /**
     * 위도 / 경도 1도 : 약 88.74km (서울 기준)
     * 1(도) : 88.74 km = x(도) : 1km
     * 1km : 약 0.009도 / 2km : 약 0.018도
     */
    public boolean isWithinRange(JSONObject item, PointDto pointDto) {
        double lot = item.getDouble("lot"); // 경도
        double lat = item.getDouble("lat"); // 위도

        double lotDistance = Math.abs(lot - pointDto.getLng()); //경도
        double latDistance = Math.abs(lat - pointDto.getLat()); // 위도
        log.info("경도차 : {}, 위도차 : {}", lotDistance, latDistance);

        // 위도 또는 경도 차이가 0.018도 이내인지 확인
        return latDistance <= 0.018 && lotDistance <= 0.018;
    }


    /** NCP API : GeoCoding 관련 서비스 */
    // 주소 입력 -> 좌표 반환
    public PointDto createAddressPosition(String roadAddrPart1) {
        // 주소의 좌표 찾기
        Map<String, Object> params = new HashMap<>();
        params.put("query", roadAddrPart1);
        params.put("page", 1);
        params.put("count", 1);
        GeoNcpResponse response = mapApiService.geocode(params);
        log.info(response.toString());

        if (response.getAddresses().isEmpty()) {
            log.error("주소 검색 결과가 없습니다.");
            return null; // 주소 검색 결과가 없을 경우
        }

        Double lng = Double.valueOf(response.getAddresses().get(0).getX());
        Double lat = Double.valueOf(response.getAddresses().get(0).getY());

        // 좌표 정보 반환
        return new PointDto(lng, lat);
    }

    /** NCP API : Reverse GeoCoding 관련 서비스 */
    // 좌표 입력 -> 법정동 코드 반환
    public LocationInfoResponseDto getLocationInfo(String roadAddrPart1) {
        PointDto pointDto = createAddressPosition(roadAddrPart1);
        // 경도/위도 순서 이슈로 확인 후 사용 'lon,lat' 형식으로 입력 (경도 위도)
        Double lng = pointDto.getLng();
        Double lat = pointDto.getLat();
        return getLocationInfo(pointDto);
    }

    // 법정동 코드에 따라 지자체 코드 변환하는 메서드
    private String transBjDongCode(String bjDongCode) {
        if (bjDongCode.startsWith("11")) return "1100000000";
        else if (bjDongCode.startsWith("36")) return "3611000000";
        else if (bjDongCode.startsWith("30")) return "3000000000";
        else return bjDongCode;
    }

    // PointDto를 받아서 위치 정보를 반환하는 메서드
    public LocationInfoResponseDto getLocationInfo(PointDto pointDto) {
        Map<String, Object> params = new HashMap<>();
        params.put("coords", pointDto.getLng() + "," + pointDto.getLat());
        params.put("orders", "legalcode");
        params.put("output", "json");

        RGeoNcpResponse response = mapApiService.reversGeocode(params);
        if (response.getResults().isEmpty()) {
            log.error("좌표로부터 법정동 정보를 얻는데 실패했습니다.");
            return null;
        }

        RGeoResult result = response.getResults().get(0);
        String code = result.getCode().getId();
        String transCode = transBjDongCode(code);
        String area1 = result.getRegion().getArea1().getName();
        String area2 = result.getRegion().getArea2().getName();
        String area3 = result.getRegion().getArea3().getName();
        String fullAddress = area1 + " " + area2 + " " + area3;

        return new LocationInfoResponseDto(pointDto, transCode, fullAddress);
    }

    // 임시테이블에 검색 정보 저장
    public void saveUserLocationInfo(PointDto pointDto) {
        LocationInfoResponseDto infoResponse = getLocationInfo(pointDto);
        if (infoResponse != null) {
            TempLocationInfoResponse tempLocationInfo = LocationInfoResponseDto.toEntity(infoResponse);
            tempLocationInfoRepo.save(tempLocationInfo);
        }
    }
}
