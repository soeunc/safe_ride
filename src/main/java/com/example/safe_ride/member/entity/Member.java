package com.example.safe_ride.member.entity;

import com.example.safe_ride.article.entity.Article;
import com.example.safe_ride.matching.entity.Manner;
import com.example.safe_ride.matching.entity.Matching;
import com.example.safe_ride.myPage.entity.MyPage;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Setter
@Entity
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String userName;
    private String password;
    private String email;
    private String nickname;
    private String phoneNumber;
    private String birthday;
    private Authority authority;
    //뱃지
    @Setter
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Badge> badges;
    //마이페이지
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<MyPage> myPages;
    //매칭
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Matching> matchings;
    //매너
    @OneToMany(mappedBy = "raterMember", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Manner> manners;
    //게시글
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Article> articles;

}
