package com.example.safe_ride.locationInfo.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MatchingBicycleService {
    // 대여소 정보에서 대여소 ID를 추출하고 Map에 저장
    public Map<String, JSONObject> parseStationWithMap(String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.getJSONObject("body").getJSONArray("item");
        Map<String, JSONObject> stationMap = new HashMap<>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String rntstnId = item.getString("rntstnId"); // 대여소 아이디
            stationMap.put(rntstnId, item); // 대여소 ID를 키로 사용하여 아이템 저장
        }

        return stationMap;
    }


    // 추출된 대여소 ID로 일치하는 자전겨 현황을 API 응답으로 부터 필터링
    // 필터링 된 데이터 문자열 리스트 (Client에게 전달할 Result Data Table)
    public List<String> filterBicycleByStationId(Map<String, JSONObject> stationMap, String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.getJSONObject("body").getJSONArray("item");
        List<String> results = new ArrayList<>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String rntstnId = item.getString("rntstnId");

            // 대여소Id가 일치하는지 확인
            if (stationMap.containsKey(rntstnId)) {
                JSONObject stationData = stationMap.get(rntstnId);
                String formattedData = formatBicycle(stationData);
                results.add(formattedData);
            }
        }

        return results;
    }

    // 필터링 된 데이터 문자열 생성 (Client에게 전달할 Result Data)
    public String formatBicycle(JSONObject item) throws JSONException {
        String lcgvmnInstCd = item.getString("lcgvmnInstCd"); // 지자체 코드(1100000000)
        String lcgvmnInstNm = item.getString("lcgvmnInstNm"); // 지자체명(서울특별시)
        String rntstnId = item.getString("rntstnId"); // 대여소 아이디(ST-10)
        String rntstnNm = item.getString("rntstnNm"); // 자전거대여소명(108. 서교동 사거리)
        String lat = item.getString("lat"); // 위도(37.5527458200)
        String lot = item.getString("lot"); // 경도(126.9186172500)
        String bcyclTpkctNocs = item.optString("bcyclTpkctNocs", "0"); //자전거 주차 총 건수(12)

        return String.format
                ("%s, %s, %s, %s, %s, %s, %s",
                        lcgvmnInstCd, lcgvmnInstNm, rntstnId, rntstnNm, lat, lot, bcyclTpkctNocs);
    }
}
