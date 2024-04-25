package com.example.safe_ride.locationInfo.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StationMapManager {
    private Map<String, JSONObject> stationMap = new HashMap<>();

    // 도로명 주소 일치 or 위도, 경도 비슷할 때 업데이트 됨
    public synchronized void updateMap(String rntstnId, JSONObject item) {
        stationMap.put(rntstnId, item);
    }

    // Data Fetch 시작할 때 삭제 됨
    public synchronized void clearMap() {
        stationMap.clear();
    }

    // 현재 맵의 복사본을 반환 :: Station Data Fetch 시작할 때 실행됨
    public synchronized Map<String, JSONObject> getStationMap() {
        return new HashMap<>(stationMap);
    }
}