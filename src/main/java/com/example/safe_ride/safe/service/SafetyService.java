package com.example.safe_ride.safe.service;

import com.example.safe_ride.safe.dto.PointDto;
import com.example.safe_ride.safe.dto.CoordinateDto;
import com.example.safe_ride.safe.entity.AccidentInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyService {
    private final NcpService ncpService;
    private final ApiService apiService;
    private static final String URL = "jdbc:sqlite:db.sqlite";

    // 사고다발지역 데이터 저장
    public void saveAccidentInfo(List<AccidentInfo> accidentInfoList) {
        // 사용자 위치를 입력할 때마다 DB 새로 저장
        String deleteSql = "DELETE FROM accident_info";
        String sql = "INSERT INTO accident_info (bjd_cd, spot_nm, occrrnc_cnt, caslt_cnt, dth_dnv_cnt, se_dnv_cnt, sl_dnv_cnt, geom_json, lo_crd, la_crd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             // 기존 데이터 삭제를 위한 PreparedStatement
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);

             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 삭제 실행
            deleteStmt.executeUpdate();
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

    // 법정동 코드 시/군/구 자리 일치하는 데이터 필터링
    public void saveFilteredAccidentInfo(PointDto dto) {
        // API로부터 모든 사고 정보를 가져오기
        List<AccidentInfo> allAccidentInfo = apiService.fetchDataFromApi();
        List<AccidentInfo> filteredAccidentInfo = new ArrayList<>();

        // PointDto로부터 법정동 코드를 가져오기
        String ncpBjDongCode = ncpService.getBjDongCode(dto).getBjDongCode();
        // 법정동 코드의 앞 5자리만 추출(시/군/구)
        String ncpCiGunGu = ncpBjDongCode.substring(0, 4);

        for (AccidentInfo accidentInfo : allAccidentInfo) {
            // 사고 정보의 법정동 코드 앞 5자리 추출(시/군/구)
            String accidentCiGunGu = accidentInfo.getBjDongCode().substring(0, 4);
            if (accidentCiGunGu.equals(ncpCiGunGu)) {
                filteredAccidentInfo.add(accidentInfo);
            }
        }

        // 필터링된 사고 정보를 저장
        saveAccidentInfo(filteredAccidentInfo);
    }

    // db에 저장된 좌표 가져오기
    public List<CoordinateDto> getCoordinates() {
        List<CoordinateDto> coordinates = new ArrayList<>();
        String sql = "SELECT lo_crd, la_crd FROM accident_info";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String lnt = rs.getString("lo_crd");
                    String lat = rs.getString("la_crd");
                    coordinates.add(new CoordinateDto(lnt, lat));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return coordinates;
    }
}
