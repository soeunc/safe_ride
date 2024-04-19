package com.example.safe_ride.locationInfo.service;

import com.example.safe_ride.locationInfo.entity.LocationInfo;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Service
public class DbLoader {

    private static final String URL = "jdbc:sqlite:db.sqlite";

    public void createTableIfNotExist() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS location_info (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sido TEXT," +
                    "sigungu TEXT," +
                    "eupmyundong TEXT," +
                    "address_code TEXT);");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertData(List<LocationInfo> addresses) {
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO location_info (sido, sigungu, eupmyundong, address_code) VALUES (?, ?, ?, ?)")) {
            conn.setAutoCommit(false);  // 자동 커밋 비활성화
            int affectedRows = 0;
            for (LocationInfo address : addresses) {
                pstmt.setString(1, address.getSido());
                pstmt.setString(2, address.getSigungu());
                pstmt.setString(3, address.getEupmyundong());
                pstmt.setString(4, address.getAddressCode());
                pstmt.executeUpdate();
                affectedRows ++;
            }
            System.out.println("DB Path: " + conn.getMetaData().getURL());
            System.out.println("Inserted rows: " + affectedRows);  // 삽입된 행의 수 출력
            conn.commit();  // 변경사항 커밋
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}