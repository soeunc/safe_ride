package com.example.safe_ride.safe.service;

import com.example.safe_ride.safe.entity.AccidentInfo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ApiService {
    private static final String BASE_URL = "https://apis.data.go.kr/B552061/frequentzoneBicycle/getRestFrequentzoneBicycle";
    @Value("${public.api.key}")
    private String SERVICE_KEY;

    // 자전거 사고다발지역
    public String buildApi() throws UnsupportedEncodingException {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);

        urlBuilder.append("?ServiceKey=").append(SERVICE_KEY);
        urlBuilder.append("&searchYearCd=").append(URLEncoder.encode("2022", StandardCharsets.UTF_8));
        urlBuilder.append("&siDo=");  // 공백시 전체 추출
        urlBuilder.append("&guGun="); // 공백시 전체 추출
        urlBuilder.append("&type=").append(URLEncoder.encode("json", StandardCharsets.UTF_8)); // 데이터 형식 (json)
        urlBuilder.append("&numOfRows=").append(URLEncoder.encode("10", StandardCharsets.UTF_8)); // 한페이지당 데이터 수
        urlBuilder.append("&pageNo=").append(URLEncoder.encode("1", StandardCharsets.UTF_8)); // page Num
        log.info("url확인: {}", urlBuilder);
        return urlBuilder.toString();
    }

    // http 응답받아 문자열로 변환
    // API에서 데이터를 가져오는 메소드
    // 여기서 법정동 코드 일치 메서드 사용하기
    public List<AccidentInfo> fetchDataFromApi() {
        try {
            String urlStr = buildApi();
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


                // 응답 문자열 반환
                log.info("법정동 코드 응답 확인: {}", ParsingDate(response.toString()).get(0).getBjDongCode());

                return ParsingDate(response.toString());
            } else {
                System.out.println("GET 요청에 실패했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 자전거 사고다발지역 데이터 파싱
    public List<AccidentInfo> ParsingDate(String jsonResponse) throws JSONException {
        // JSON 응답 문자열 파싱
        JSONObject jsonObject = new JSONObject(jsonResponse);
        // 사고 정보 추출
        JSONArray items = jsonObject.getJSONObject("items").getJSONArray("item");

        // 사고다발지역 정보를 저장
        List<AccidentInfo> accidentInfoList = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            AccidentInfo accidentInfo = new AccidentInfo();
            accidentInfo.setBjDongCode(item.getString("bjd_cd")); // 법정동 코드
            accidentInfo.setSpotNm(item.getString("spot_nm")); // 사고가 발생한 시도 및 시군구 명칭
            accidentInfo.setOccrrncCnt(item.getInt("occrrnc_cnt")); // 발생 건수
            accidentInfo.setCasltCnt(item.getInt("caslt_cnt")); // 사상자 수
            accidentInfo.setDthDnvCnt(item.getInt("dth_dnv_cnt")); // 사망자 수
            accidentInfo.setSeDnvCnt(item.getInt("se_dnv_cnt")); // 중상자 수
            accidentInfo.setSlDnvCnt(item.getInt("sl_dnv_cnt")); // 경상자 수
            accidentInfo.setGeomJson(item.getString("geom_json")); // 사고 지점 지리적 위치 데이터
            accidentInfo.setLoCrd(item.getString("lo_crd")); // 경도(lnt)
            accidentInfo.setLaCrd(item.getString("la_crd")); // 위도(lat)

            accidentInfoList.add(accidentInfo);
        }

        return accidentInfoList;
    }

    // totalCount를 얻기 위한 API URL 생성
    private String buildApiForTotalCount() throws UnsupportedEncodingException {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);

        // 기본 파라미터 설정
        urlBuilder.append("?ServiceKey=").append(SERVICE_KEY);
        urlBuilder.append("&searchYearCd=").append(URLEncoder.encode("2022", StandardCharsets.UTF_8));
        urlBuilder.append("&siDo=");
        urlBuilder.append("&guGun=");
        urlBuilder.append("&type=").append(URLEncoder.encode("json", StandardCharsets.UTF_8));
        urlBuilder.append("&numOfRows=1");  // totalCount 확인을 위해 numOfRows를 1로 설정

        return urlBuilder.toString();
    }
}
