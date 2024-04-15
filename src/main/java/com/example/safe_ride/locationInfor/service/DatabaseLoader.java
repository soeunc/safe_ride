package com.example.safe_ride.locationInfor.service;

import com.example.safe_ride.locationInfor.entity.LocationInfor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Service
public class DatabaseLoader {

    private static final String URL = "jdbc:sqlite:db.sqlite";

    public void createTableIfNotExist() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS location_infor (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sido TEXT," +
                    "sigungu TEXT," +
                    "eubmyundong TEXT," +
                    "address_code TEXT);");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertData(List<LocationInfor> addresses) {
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO location_infor (sido, sigungu, eubmyundong, address_code) VALUES (?, ?, ?, ?)")) {
            conn.setAutoCommit(false);  // 자동 커밋 비활성화
            int affectedRows = 0;
            for (LocationInfor address : addresses) {
                pstmt.setString(1, address.getSido());
                pstmt.setString(2, address.getSigungu());
                pstmt.setString(3, address.getEubmyundong());
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