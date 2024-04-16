package com.example.safe_ride.article.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "regions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metropolitan_city")
    private String metropolitanCity;

    @Column(name = "city")
    private String city;

    // 생성자 추가
    public Region(Long id) {
        this.id = id;
    }
}