package com.example.safe_ride.locationInfo.service;

import com.example.safe_ride.locationInfo.entity.LocationInfo;
import com.example.safe_ride.locationInfo.repo.LocationInfoRepo;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationInfoService {

    private final LocationInfoRepo locationInfoRepo;
    private static final String BASE_URL = "https://apis.data.go.kr/B551982/pbdo"; // baseURL
    // Encoding 인증키
    private static final String SERVICE_KEY = "z88eBNe%2B8cP%2BJbPk%2BPcjoRg1biqWAPB%2B1oH6sqToU4SqwZJzkUMKuWZKQpRolY8gg6nISJeqtLLDv5I7COksvw%3D%3D"; // 발급받은 인증키
    // Decoding 인증키
//    private static final String SERVICE_KEY = "z88eBNe+8cP+JbPk+PcjoRg1biqWAPB+1oH6sqToU4SqwZJzkUMKuWZKQpRolY8gg6nISJeqtLLDv5I7COksvw=="; // 발급받은 인증키


    // 0-1. totalCount를 받아오기 위한 초기 URL 설정 메서드
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

    // 0-2. totalCount를 받아오는 메서드
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
            System.out.println("total Count : " + jsonResponse.getJSONObject("body").getString("totalCount"));
            return Integer.parseInt(jsonResponse.getJSONObject("body").getString("totalCount"));
        } finally {
            connection.disconnect();
        }
    }

    // 1. 공통 URL 설정 메서드 (total count를 받아 최종 result Url 생성)
    public String buildUrl(String apiUrl, String lcgvmnInstCd, String fromCrtrYmd, String toCrtrYmd, int pageNo, int numOfRows) throws UnsupportedEncodingException {
        System.out.println("pageNo :" + pageNo);
        System.out.println("numOfRows :" + numOfRows);
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


    // 2. http 응답 수신 및 문자열 조합
    public List<String> fetchData(String apiUrl, String lcgvmnInstCd, String fromCrtrYmd, String toCrtrYmd, String status) throws IOException {
        // totalCount 기반 동적 데이터 생성
        int totalCount = getTotalCntData(apiUrl, lcgvmnInstCd);
        int numOfRows = 700;
        // 필요한 전체 페이지 수 계산
        int totalPages = (totalCount + numOfRows - 1) / numOfRows;

        // 모든 결과를 저장할 List
        List<String> allResults = new ArrayList<>();

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
                    List<String> results = parseDataBasedOnStatus(response.toString(), status);
                    if (!results.isEmpty()) {
                        allResults.addAll(results);
                    } else {
                        System.out.println("페이지 " + pageNo + "에서 유효한 데이터가 없습니다.");
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

    // 3. status에 따른 데이터 파싱 REST
    private List<String> parseDataBasedOnStatus(String  jsonResponse, String status) throws JSONException {
        if (status.equals("1")) return parseBicycleStationData(jsonResponse);
        else if (status.equals("2")) return parseAllBicycleData(jsonResponse);
        else return parseNowBicycleData(jsonResponse);
    }

    // 3-1. 데이터 파싱 (공영자전거)
    public List<String> parseBicycleStationData(String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.getJSONObject("body").getJSONArray("item");

        List<String> results = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String lcgvmnInstCd = item.getString("lcgvmnInstCd"); // 지자체 코드(1100000000)
            String lcgvmnInstNm = item.getString("lcgvmnInstNm"); // 지자체명(서울특별시)
            String rntstnId = item.getString("rntstnId"); // 대여소 아이디(ST-10)
            String rntstnNm = item.getString("rntstnNm"); // 자전거대여소명(108. 서교동 사거리)
            String roadNmAddr = item.getString("roadNmAddr"); // 소재지도로명주소(서울특별시 마포구 양화로 93 427)
            String lotnoAddr = item.getString("lotnoAddr"); // 소재지지번주소(서울특별시 마포구 서교동 378-5)
            String lat = item.getString("lat"); // 위도(37.5527458200)
            String lot = item.getString("lot"); // 경도(126.9186172500)

            String result = String.format("%s, %s, %s, %s, %s, %s, %s, %s",
                    lcgvmnInstCd, lcgvmnInstNm, rntstnId, rntstnNm, roadNmAddr, lotnoAddr, lat, lot);
            results.add(result);
        }

        return results;
    }

    // 3-2. 데이터 파싱 (공영자전거 전체 사용 현황)
    public List<String> parseAllBicycleData(String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.getJSONObject("body").getJSONArray("item");

        List<String> results = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String lcgvmnInstCd = item.getString("lcgvmnInstCd"); // 지자체 코드(1100000000)
            String lcgvmnInstNm = item.getString("lcgvmnInstNm"); // 지자체명(서울특별시)
            String rntstnId = item.getString("rntstnId"); // 대여소 아이디(ST-10)
            String rntstnNm = item.getString("rntstnNm"); // 자전거대여소명(108. 서교동 사거리)
            String lat = item.getString("lat"); // 위도(37.5527458200)
            String lot = item.getString("lot"); // 경도(126.9186172500)
            String bcyclTpkctNocs = item.getString("bcyclTpkctNocs"); //자전거 주차 총 건수(12)

            String result = String.format("%s, %s, %s, %s, %s, %s, %s", lcgvmnInstCd, lcgvmnInstNm, rntstnId, rntstnNm, lat, lot, bcyclTpkctNocs);
            results.add(result);
        }
        return results;
    }

    // 3-3. 데이터 파싱 (공영자전거 대여, 반납 현황)
    // 지금 프로젝트에서는 사용하지 않아도 되는 정보로, 활용하고 있지 않음
    public List<String> parseNowBicycleData(String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.getJSONObject("body").getJSONArray("item");

        List<String> results = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String rntstnNm = item.getString("rntstnNm"); // 자전거대여소명(108. 서교동 사거리)
            String rntNocs = item.getString("rntNocs"); // 대여건수(23)
            String rtnNocs = item.getString("rtnNocs"); // 반납건(13)
            String crtrYmd = item.getString("crtrYmd"); // 대여집계일자(20240211)

            String result = String.format("%s, %s, %s, %s", rntstnNm, rntNocs, rtnNocs, crtrYmd);
            results.add(result);
        }

        return results;
    }

    // 전체 데이터 중에 시도 데이터 불러오기 (폼 제출용)
    public List<String> getSido() {
        return locationInfoRepo.findAll().stream()
                .map(LocationInfo::getSido)
                .distinct()
                .collect(Collectors.toList());
    }
    // 시도 중에 시군구 데이터 불러오기 (폼 제출용)
    public List<String> getSigunguBySido(String sido) {
        return locationInfoRepo.findAllBySido(sido).stream()
                .map(LocationInfo::getSigungu)
                .distinct()
                .collect(Collectors.toList());
    }
    // 시군구 중에 읍면동 데이터 불러오기 (폼 제출용)
    public List<String> getEupmyundongBySigungu(String sigungu) {
        return locationInfoRepo.findAllBySigungu(sigungu).stream()
                .map(LocationInfo::getEupmyundong)
                .distinct()
                .collect(Collectors.toList());
    }

    // 제출된 폼의 지역 코드 추출 (API 인자로 써야함)
    public String getAddressCode(String sido) {
        List<LocationInfo> locations = locationInfoRepo.findBySido(sido);
        if (!locations.isEmpty()) {
            return locations.get(0).getAddressCode();
        }
        return null;  // 결과가 없으면 null 반환
    }

    // 제출된 시도, 시군구, 읍면동 데이터로 해당하는 지역의 데이터 표현하기
    // 1. API 데이터 중 소재지지번주소 (lotnoAddr) 와 일치하는 데이터를 추출하는 쿼리 작성
    // 2. 대여소 정보 데이터를 추출
    // 3. 추출된 대여소의 ID(rntstnId)와 일치하는 데이터를 추출하는 쿼리 작성
    // 4. 대여소 자전거 정보 데이터를 추출
}
