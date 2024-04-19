package com.example.safe_ride.safe.service;

import com.example.safe_ride.safe.entity.AccidentInfo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ApiService {
    private static final String BASE_URL = "https://apis.data.go.kr/B552061/frequentzoneBicycle/getRestFrequentzoneBicycle";
    @Value("${public.api.key}")
    private String SERVICE_KEY;

    // totalCount를 얻기 위한 API URL 생성
    private String buildApiForTotalCount() {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);

        // 기본 파라미터 설정
        urlBuilder.append("?ServiceKey=").append(SERVICE_KEY);
        urlBuilder.append("&searchYearCd=").append(URLEncoder.encode("2022", StandardCharsets.UTF_8));
        urlBuilder.append("&siDo=");
        urlBuilder.append("&guGun=");
        urlBuilder.append("&type=").append(URLEncoder.encode("json", StandardCharsets.UTF_8));
        urlBuilder.append("&numOfRows=1");  // totalCount 확인을 위해 numOfRows를 1로 설정
        urlBuilder.append("&pageNo=1");

        return urlBuilder.toString();
    }

    // totalCount를 얻기 위한 API 호출 메소드
    public int fetchTotalCount() throws IOException {
        String urlStr = buildApiForTotalCount();
        URL url = new URL(urlStr);
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
            int totalCount = jsonResponse.getInt("totalCount");
            System.out.println("Total Count: " + totalCount);
            return totalCount;
        } finally {
            connection.disconnect();
        }
    }


    // 자전거 사고다발지역 데이터 API URL
    public String buildApi(int numOfRows, int pageNo) throws UnsupportedEncodingException {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);

        urlBuilder.append("?ServiceKey=").append(SERVICE_KEY);
        // TODO 데이터 업데이터가 1년주기로 1년의 사고만 업데이터 되는것 같다. 그럼 지금까지의 전체 데이터를 불러와야 될꺼 같다.
        urlBuilder.append("&searchYearCd=").append(URLEncoder.encode("2022", StandardCharsets.UTF_8));
        urlBuilder.append("&siDo=");  // 공백시 전체 추출
        urlBuilder.append("&guGun="); // 공백시 전체 추출
        urlBuilder.append("&type=").append(URLEncoder.encode("json", StandardCharsets.UTF_8)); // 데이터 형식 (json)
        urlBuilder.append("&numOfRows=").append(URLEncoder.encode(String.valueOf(numOfRows), "UTF-8")); // 한페이지당 데이터 수
        urlBuilder.append("&pageNo=").append(URLEncoder.encode(String.valueOf(pageNo)));  // 페이지 번호
        log.info("url확인: {}", urlBuilder);
        return urlBuilder.toString();
    }

    // http 응답받아 문자열로 변환, API 호출 메소드
    public List<AccidentInfo> fetchDataFromApi() {
        try {
            // 전체 데이터 개수 얻기
            int totalCount = fetchTotalCount();
            // 한 번에 요청할 데이터 수
            int numOfRows = 100;
            // 필요한 전체 API 호출 횟수 계산(총 3페이지)
            int totalCalls = (int) Math.ceil((double) totalCount / numOfRows);
            log.info("totalCalls: {}", totalCalls);

            List<AccidentInfo> resultList = new ArrayList<>();

            for (int pageNo = 1; pageNo <= totalCalls; pageNo++) {
                String urlStr = buildApi(numOfRows, pageNo);
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();
                System.out.println("Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    // JSON 파싱을 시도하고, 실패할 경우 JSONException을 잡아 처리
                    try {
                        List<AccidentInfo> parsedList = parsingDate(response.toString());
                        resultList.addAll(parsedList);
                    } catch (JSONException e) {
                        log.error("JSON 파싱 중 오류 발생: {}", e.getMessage());
                    }
                } else {
                    System.out.println("GET 요청에 실패했습니다.");
                }
            }
            return resultList;
        } catch (IOException e) {
            throw new RuntimeException("API 호출 중 오류 발생", e);
        }
    }

    // 자전거 사고다발지역 데이터 파싱
    public List<AccidentInfo> parsingDate(String jsonResponse) throws JSONException {
        // JSON 응답 문자열 파싱
        JSONObject jsonObject = new JSONObject(jsonResponse);
        // 사고 정보 추출
        JSONArray items = jsonObject.getJSONObject("items").getJSONArray("item");

        // 사고다발지역 정보를 저장
        List<AccidentInfo> accidentInfoList = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            AccidentInfo accidentInfo = new AccidentInfo();
            accidentInfo.setBjDongCode(item.getString("bjd_cd"));   // 법정동 코드
            accidentInfo.setSpotNm(item.getString("spot_nm"));      // 사고가 발생한 시도 및 시군구 명칭
            accidentInfo.setOccrrncCnt(item.getInt("occrrnc_cnt")); // 발생 건수
            accidentInfo.setCasltCnt(item.getInt("caslt_cnt"));     // 사상자 수
            accidentInfo.setDthDnvCnt(item.getInt("dth_dnv_cnt"));  // 사망자 수
            accidentInfo.setSeDnvCnt(item.getInt("se_dnv_cnt"));    // 중상자 수
            accidentInfo.setSlDnvCnt(item.getInt("sl_dnv_cnt"));    // 경상자 수
            accidentInfo.setGeomJson(item.getString("geom_json"));  // 사고 지점 지리적 위치 데이터
            accidentInfo.setLoCrd(item.getString("lo_crd"));        // 경도(lnt)
            accidentInfo.setLaCrd(item.getString("la_crd"));        // 위도(lat)

            accidentInfoList.add(accidentInfo);
        }
        return accidentInfoList;
    }
}
