package com.example.safe_ride.matching.service;

import com.example.safe_ride.article.entity.Region;
import com.example.safe_ride.article.repository.RegionRepository;
import com.example.safe_ride.matching.dto.MatchingDto;
import com.example.safe_ride.matching.entity.Matching;
import com.example.safe_ride.matching.entity.MatchingStatus;
import com.example.safe_ride.matching.repository.MatchingApplicationRepository;
import com.example.safe_ride.matching.repository.MatchingRepository;
import com.example.safe_ride.member.entity.Member;
import com.example.safe_ride.member.repo.MemberRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {
    private final MatchingRepository matchingRepository;
    private final MemberRepo memberRepository;
    private final RegionRepository regionRepository;
    private final MatchingApplicationRepository matchingApplicationRepository;

    @Transactional
    public MatchingDto createMatching(MatchingDto matchingDto) {
        Member currentUser = getUserEntity();

        // 게시글에 선택된 지역 정보 가져오기
        if (matchingDto.getMetropolitanCity() == null || matchingDto.getCity() == null) {
            throw new IllegalArgumentException("Metropolitan city and city must not be null");
        }

        // 광역자치구와 도시에 해당하는 Region ID 가져오기
        Long regionId = regionRepository.findByMetropolitanCityAndCity(matchingDto.getMetropolitanCity(), matchingDto.getCity())
                .map(Region::getId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid metropolitan city or city"));

        // 현재 시간 가져오기
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        // Matching 엔티티 생성시 Member 엔티티를 설정
        Matching matching = Matching.builder()
                .region(new Region(regionId)) // 광역자치구와 도시에 해당하는 Region ID 설정
                .member(currentUser) // 작성자
                .title(matchingDto.getTitle())
                .ridingTime(matchingDto.getRidingTime()) // 라이딩 시간
                .kilometer(matchingDto.getKilometer())
                .comment(matchingDto.getComment())
                .createTime(currentTime)
                .status(MatchingStatus.PENDING)
                .build();

        // 생성된 Matching 엔티티 저장
        Matching savedMatching = matchingRepository.save(matching);
        return MatchingDto.fromEntity(savedMatching);
    }


    private Member getUserEntity() {
        UserDetails userDetails =
                (UserDetails) (SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        return memberRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // 특정 ID의 매칭글 조회
    public MatchingDto getMatchingById(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new IllegalArgumentException("Matching not found with id: " + matchingId));
        return MatchingDto.fromEntity(matching);
    }

    // 게시글 전체 조회
    public Page<MatchingDto> readPage(Pageable pageable) {
        Page<Matching> matchings = matchingRepository.findAll(pageable);
        return matchings.map(MatchingDto::fromEntity);
    }

    // 매칭글 수정
    public MatchingDto updateMatching(Long id, MatchingDto dto) {
        // 해당 ID의 매칭글을 조회
        Matching matching = matchingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));

        // 매칭글에 변경된 내용 적용
        matching.setComment(dto.getComment());
        matching.setKilometer(dto.getKilometer());
        matching.setRidingTime(dto.getRidingTime());
        matching.setTitle(dto.getTitle());
        matching.setStatus(MatchingStatus.PENDING);

        // 광역자치구와 도시에 해당하는 Region ID 가져오기
        Long regionId = regionRepository.findByMetropolitanCityAndCity(dto.getMetropolitanCity(), dto.getCity())
                .map(Region::getId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid metropolitan city or city"));

        matching.setRegion(new Region(regionId)); // 광역자치구와 도시에 해당하는 Region ID 설정

        // 수정된 게시글 저장
        return MatchingDto.fromEntity(matchingRepository.save(matching));
    }

    // 매칭글 삭제
    public void deleteMatching(Long matchingId) {
        matchingRepository.deleteById(matchingId);
    }


    // 광역자치구에 따라 매칭글 페이지로 읽어오기
    public Page<MatchingDto> readPageByMetropolitanCity(Pageable pageable, String metropolitanCity) {
        Page<Matching> matchingPage = matchingRepository.findByRegionMetropolitanCity(pageable, metropolitanCity);
        return matchingPage.map(MatchingDto::fromEntity);
    }


    // 광역자치구와 도시에 따라 매칭글 페이지로 읽어오기
    public Page<MatchingDto> readPageByMetropolitanCityAndCity(Pageable pageable, String metropolitanCity, String city) {
        Page<Matching> matchingPage = matchingRepository.findByRegionMetropolitanCityAndRegionCity(pageable, metropolitanCity, city);
        return matchingPage.map(MatchingDto::fromEntity);
    }

    // 매칭 상태를 END로 변경
    public void endMatching(Long id) {
        Matching matching = matchingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Matching not found with id: " + id));
        matching.setStatus(MatchingStatus.END); // 매칭 상태를 END로 설정
        matchingRepository.save(matching);
    }
}