package com.example.safe_ride.member.dto;

import com.example.safe_ride.member.entity.Badge;
import com.example.safe_ride.member.entity.Grade;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeDto {
    private Long id;
    private Long memberId;
    @Enumerated(EnumType.STRING)
    private Grade grade;

    public static BadgeDto fromEntity(Badge entity){
        return BadgeDto.builder()
                .id(entity.getId())
                .memberId(entity.getMember().getId())
                .grade(entity.getGrade())
                .build();
    }
}
