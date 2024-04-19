package com.example.safe_ride.safe.service;

import com.example.safe_ride.safe.dto.PointDto;
import com.example.safe_ride.safe.dto.rgeocoding.RGeoResponseDto;
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
        // DB한번 삭제하고 다시 저장하도록 수정
//        String deleteSql = "DELETE FROM accident_info";
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
        List<AccidentInfo> allAccidentInfo = apiService.fetchDataFromApi(); // API로부터 모든 사고 정보를 가져옴
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

            List<AccidentInfo> accidentInfoList = apiService.fetchDataFromApi();
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
