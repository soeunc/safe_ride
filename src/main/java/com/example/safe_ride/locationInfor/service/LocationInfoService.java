package com.example.safe_ride.locationInfor.service;

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
import java.util.ArrayList;
import java.util.List;

@Service
public class LocationInfoService {
    private static final String BASE_URL = "https://apis.data.go.kr/B551982/pbdo"; // baseURL

    // Encoding 인증키
    private static final String SERVICE_KEY = "z88eBNe%2B8cP%2BJbPk%2BPcjoRg1biqWAPB%2B1oH6sqToU4SqwZJzkUMKuWZKQpRolY8gg6nISJeqtLLDv5I7COksvw%3D%3D"; // 발급받은 인증키
    // Decoding 인증키
//    private static final String SERVICE_KEY = "z88eBNe+8cP+JbPk+PcjoRg1biqWAPB+1oH6sqToU4SqwZJzkUMKuWZKQpRolY8gg6nISJeqtLLDv5I7COksvw=="; // 발급받은 인증키


    // 1. 공통 URL 설정 메서드
    public String buildUrl(String apiUrl, String lcgvmnInstCd, String fromCrtrYmd , String toCrtrYmd) throws UnsupportedEncodingException {
        // result Url 생성
        StringBuilder urlBuilder = new StringBuilder(BASE_URL) ;
        urlBuilder.append("/").append(apiUrl);
        urlBuilder.append("?serviceKey=").append(SERVICE_KEY);
        urlBuilder.append("&pageNo=").append(URLEncoder.encode("2", "UTF-8")); // page Num
        urlBuilder.append("&numOfRows=").append(URLEncoder.encode("10", "UTF-8")); // 한페이지당 데이터 수
        urlBuilder.append("&type=").append(URLEncoder.encode("json", "UTF-8")); // 데이터 형식 (xml or json)
        lcgvmnInstCd = "1100000000"; // 임시 -- 지자체 코드 (1100000000 : 서울) (폼에서 받아와야함)
        urlBuilder.append("&lcgvmnInstCd=").append(URLEncoder.encode(lcgvmnInstCd, "UTF-8"));

        // 실시간 현황은 매개변수 추가
        if (fromCrtrYmd != null && toCrtrYmd != null) {
            fromCrtrYmd = "20240401"; // 임시 (폼에서 받아와야함)
            toCrtrYmd = "20240402"; // 임시 (폼에서 받아와야함)
            urlBuilder.append("&fromCrtrYmd=").append(URLEncoder.encode(fromCrtrYmd, "UTF-8"));
            urlBuilder.append("&toCrtrYmd=").append(URLEncoder.encode(toCrtrYmd, "UTF-8"));
        }
        return urlBuilder.toString();
    }


    // 2. http 응답 수신 및 문자열 조합
    public List<String> fetchData(String apiUrl, String lcgvmnInstCd, String fromCrtrYmd , String toCrtrYmd, String status) throws IOException {
        // url로 http 응답 받아오기
        String resultUrl = buildUrl(apiUrl, lcgvmnInstCd, fromCrtrYmd, toCrtrYmd);
        URL url = new URL(resultUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-type", "application/json");

        int responseCode = connection.getResponseCode();
        System.out.println("Response code: " + responseCode);

        // 응답 상태가 좋으면 받아온 http를 읽고 문자열로 조합
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            // 데이터 없을 때 까지 반복
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // status 코드에 따라 각각 다른 데이터로 파싱
            if (status.equals("1")) return parseBicycleStationData(response.toString()); // 대여소 현황
            else if (status.equals("2")) return parseAllBicycleData(response.toString()); // 대여소 내 전체 자전거 현황
            else return parseNowBicycleData(response.toString()); // 대여/반납 현황
        }
        // 응답 상태가 나쁘면 예외
        else {
            throw new IOException("서버 응답 오류 -> HTTP 상태 코드 : " + responseCode);
        }
    }

    // 3-1. 데이터 파싱 (공영자전거)
    public List<String> parseBicycleStationData(String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.getJSONObject("body").getJSONArray("item");

        List<String> results = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String rntstnNm = item.getString("rntstnNm"); // 자전거대여소명(108. 서교동 사거리)
            String roadNmAddr = item.getString("roadNmAddr"); // 소재지도로명주소(서울특별시 마포구 양화로 93 427)
            String lotnoAddr = item.getString("lotnoAddr"); // 소재지지번주소(서울특별시 마포구 서교동 378-5)
            String lat = item.getString("lat"); // 위도(37.5527458200)
            String lot = item.getString("lot"); // 경도(126.9186172500)
            String operBgngHrCn = item.getString("operBgngHrCn"); // 운영시작시각(00:00)
            String operEndHrCn = item.getString("operEndHrCn"); // 운영종료시각(23:59)
            String rntstnOperDayoffDayCn = item.getString("rntstnOperDayoffDayCn"); // 휴무일(연중무휴)
            String mngInstTelno = item.getString("mngInstTelno"); // 관리기관전화번호(https://data.seoul.go.kr)

            String result = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s",
                    rntstnNm, roadNmAddr, lotnoAddr, lat, lot, operBgngHrCn, operEndHrCn, rntstnOperDayoffDayCn, mngInstTelno);
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
            String rntstnNm = item.getString("rntstnNm"); // 자전거대여소명(108. 서교동 사거리)
            String lat = item.getString("lat"); // 위도(37.5527458200)
            String lot = item.getString("lot"); // 경도(126.9186172500)
            String bcyclTpkctNocs = item.getString("bcyclTpkctNocs"); //자전거 주차 총 건수(12)

            String result = String.format("%s, %s, %s, %s",  rntstnNm, lat, lot, bcyclTpkctNocs);
            results.add(result);
        }

        return results;
    }

    // 3-3. 데이터 파싱 (공영자전거 대여, 반납 현황)
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
}
