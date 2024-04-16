package com.example.safe_ride;

import com.example.safe_ride.locationInfo.entity.LocationInfo;
import com.example.safe_ride.locationInfo.service.CsvDataReader;
import com.example.safe_ride.locationInfo.service.DatabaseLoader;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

// 프로그램 시작 시 address.csv를 불러오고 DB에 추가하는 클래스
@Component
public class DataInitializer {

    private final DatabaseLoader databaseLoader;

    public DataInitializer(DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() {
        try {
            // 데이터베이스 연결
            String url = "jdbc:sqlite:db.sqlite";
            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt = conn.createStatement()) {
                // 테이블 데이터 삭제
                stmt.execute("DELETE FROM location_info");
                // 데이터 로딩 로직
                List<LocationInfo> addresses = new CsvDataReader().readCsvData("src/main/resources/address.csv");
                databaseLoader.insertData(addresses);
                System.out.println("Data has been loaded!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}