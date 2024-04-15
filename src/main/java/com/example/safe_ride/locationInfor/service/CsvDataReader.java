package com.example.safe_ride.locationInfor.service;

import com.example.safe_ride.locationInfor.entity.LocationInfor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CsvDataReader {
    public List<LocationInfor> readCsvData(String filePath) {
        List<LocationInfor> addresses = new ArrayList<>();
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord csvRecord : csvParser) {
                Long id = Long.valueOf(csvRecord.get("id"));
                String sido = csvRecord.get("시도명");
                String sigungu = csvRecord.get("시군구명");
                String eubmyundong = csvRecord.get("읍면동명");
                String addressCode = csvRecord.get("행정동코드");

                addresses.add(new LocationInfor(id, sido, sigungu, eubmyundong, addressCode));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return addresses;
    }
}