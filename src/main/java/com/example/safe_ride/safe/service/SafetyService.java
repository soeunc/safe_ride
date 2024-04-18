package com.example.safe_ride.safe.service;

import com.example.safe_ride.safe.dto.PointDto;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoResponseDto;
import com.example.safe_ride.safe.entity.AccidentInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyService {
    private final NcpService ncpService;

    private static final String BASE_URL = "https://apis.data.go.kr/B552061/frequentzoneBicycle/getRestFrequentzoneBicycle";
    @Value("${public.api.key}")
    private String SERVICE_KEY;
    private static final String URL = "jdbc:sqlite:db.sqlite";


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


    // 사고다발지역 데이터 저장
    public void saveAccidentInfo(List<AccidentInfo> accidentInfoList) {
        String sql = "INSERT INTO accident_info (bjd_cd, spot_nm, occrrnc_cnt, caslt_cnt, dth_dnv_cnt, se_dnv_cnt, sl_dnv_cnt, geom_json, lo_crd, la_crd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (AccidentInfo info : accidentInfoList) {
                pstmt.setString(1, info.getBjDongCode());
                pstmt.setString(2, info.getSpotNm());
                pstmt.setInt(3, info.getOccrrncCnt());
                pstmt.setInt(4, info.getCasltCnt());
                pstmt.setInt(5, info.getDthDnvCnt());
                pstmt.setInt(6, info.getSeDnvCnt());
                pstmt.setInt(7, info.getSlDnvCnt());
                pstmt.setString(8, info.getGeomJson());
                pstmt.setString(9, info.getLoCrd());
                pstmt.setString(10, info.getLaCrd());
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 법정동 일치하는 데이터만 저장
    // 문제 - 데이터를 이미 불러온 상태 / 데이터 불러오기 전에 필터링을 해서 가져오고 싶음
    public void saveFilteredAccidentInfo(PointDto dto) {
        List<AccidentInfo> allAccidentInfo = fetchDataFromApi(); // API로부터 모든 사고 정보를 가져옴
        List<AccidentInfo> filteredAccidentInfo = new ArrayList<>();

        String ncpBjDongCode = ncpService.getBjDongCode(dto).getBjDongCode(); // PointDto로부터 법정동 코드를 가져옴

        for (AccidentInfo accidentInfo : allAccidentInfo) {
            if (accidentInfo.getBjDongCode().equals(ncpBjDongCode)) {
                filteredAccidentInfo.add(accidentInfo); // 법정동 코드가 일치하는 사고 정보만 필터링
            }
        }

        saveAccidentInfo(filteredAccidentInfo); // 필터링된 사고 정보를 저장
    }

    // 법정동 코드 일치 확인 메서드
    // 법정동 코드 앞자리 4개만 일치하는 것으로 수정 예정
    public boolean getBjDongCodeMatch(PointDto dto) {
        try {
            // NcpService를 통해 법정동 코드 가져오기
            RGeoResponseDto rGeoResponseDto = ncpService.getBjDongCode(dto);
            String ncpBjDongCode = rGeoResponseDto.getBjDongCode();

            List<AccidentInfo> accidentInfoList = fetchDataFromApi();
            for (AccidentInfo accidentInfo : accidentInfoList) {
                String safetyServiceBjDongCode = accidentInfo.getBjDongCode();
                if (safetyServiceBjDongCode.equals(ncpBjDongCode)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 일치한 법정동 코드 좌표 반환 메서드


}
