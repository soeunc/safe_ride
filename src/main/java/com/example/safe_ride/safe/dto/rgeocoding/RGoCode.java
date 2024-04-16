package com.example.safe_ride.safe.dto.rgeocoding;

import lombok.Data;

@Data
public class RGoCode {
    private String id; // 코드값 (법정동 코드)
    private String type; // 코드 타입 (L: 법정동, A: 행정동, S: 동일법정동 이름 존재하는 행정동)
    private String mappingId; // id와 관련된 매핑 코드
}
