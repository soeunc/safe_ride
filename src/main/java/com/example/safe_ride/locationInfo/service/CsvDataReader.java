package com.example.safe_ride.locationInfo.service;

import com.example.safe_ride.locationInfo.entity.LocationInfo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CsvDataReader {
    public List<LocationInfo> readCsvData(String filePath) {
        List<LocationInfo> addresses = new ArrayList<>();
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord csvRecord : csvParser) {
                Long id = Long.valueOf(csvRecord.get("id"));
                String sido = csvRecord.get("sido");
                String sigungu = csvRecord.get("sigungu");
                String eupmyundong = csvRecord.get("eupmyundong");
                String addressCode = csvRecord.get("addressCode");

                addresses.add(new LocationInfo(id, sido, sigungu, eupmyundong, addressCode));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return addresses;
    }
}