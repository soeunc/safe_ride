package com.example.safe_ride.locationInfo.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingStationService {
    // 필터링 된 데이터 문자열 리스트 (Client에게 전달할 Result Data Table)
    public List<String> filterStationsByDoroJuso(String doroJuso, String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.getJSONObject("body").getJSONArray("item");

        List<String> FilteredResult = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            // 도로명 주소가 포함하는지 확인
            if (item.getString("roadNmAddr").contains(doroJuso)) {
                String formattedData = formatStation(item);
                FilteredResult.add(formattedData);
            }
        }
        return FilteredResult;
    }

    // 필터링 된 데이터 문자열 생성 (Client에게 전달할 Result Data)
    private String formatStation(JSONObject item) throws JSONException {
        String lcgvmnInstCd = item.getString("lcgvmnInstCd"); // 지자체 코드(1100000000)
        String lcgvmnInstNm = item.getString("lcgvmnInstNm"); // 지자체명(서울특별시)
        String rntstnId = item.getString("rntstnId"); // 대여소 아이디(ST-10)
        String rntstnNm = item.getString("rntstnNm"); // 자전거대여소명(108. 서교동 사거리)
        String roadNmAddr = item.getString("roadNmAddr"); // 소재지도로명주소(서울특별시 마포구 양화로 93 427)
        String lotnoAddr = item.getString("lotnoAddr"); // 소재지지번주소(서울특별시 마포구 서교동 378-5)
        String lat = item.getString("lat"); // 위도(37.5527458200)
        String lot = item.getString("lot"); // 경도(126.9186172500)

        return String.format("%s, %s, %s, %s, %s, %s, %s, %s",
                lcgvmnInstCd, lcgvmnInstNm, rntstnId, rntstnNm, roadNmAddr, lotnoAddr, lat, lot);
    }
}
