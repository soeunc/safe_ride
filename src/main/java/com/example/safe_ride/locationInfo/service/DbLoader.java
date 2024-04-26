package com.example.safe_ride.locationInfo.service;

import com.example.safe_ride.locationInfo.entity.LocationInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Service
public class DbLoader {
    private final String URL;
    private final String username;
    private final String password;

    public DbLoader(@Value("${spring.datasource.url}") String URL,
                    @Value("${spring.datasource.username}") String username,
                    @Value("${spring.datasource.password}") String password
    ) {
        this.URL = URL;
        this.username = username;
        this.password = password;
    }

    public void createTableIfNotExist() {
        try (Connection conn = DriverManager.getConnection(URL, username, password);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS location_info (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    // mysql 문법
//                    "id INT NOT NULL AUTO_INCREMENT," +
                    "sido TEXT," +
                    "sigungu TEXT," +
                    "eupmyundong TEXT," +
                    "address_code TEXT);");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertData(List<LocationInfo> addresses) {
        try (Connection conn = DriverManager.getConnection(URL, username, password);
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
            log.info("Inserted rows: " + affectedRows);  // 삽입된 행의 수 출력
            conn.commit();  // 변경사항 커밋
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}