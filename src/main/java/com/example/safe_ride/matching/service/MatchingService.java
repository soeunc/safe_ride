package com.example.safe_ride.matching.service;

import com.example.safe_ride.article.entity.Region;
import com.example.safe_ride.article.repository.RegionRepository;
import com.example.safe_ride.matching.dto.MatchingApplicationDto;
import com.example.safe_ride.matching.dto.MatchingDto;
import com.example.safe_ride.matching.entity.Matching;
import com.example.safe_ride.matching.entity.MatchingApplication;
import com.example.safe_ride.matching.entity.MatchingStatus;
import com.example.safe_ride.matching.repository.MatchingApplicationRepository;
import com.example.safe_ride.matching.repository.MatchingRepository;
import com.example.safe_ride.member.entity.Member;
import com.example.safe_ride.member.repo.MemberRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {
    private final MatchingRepository matchingRepository;
    private final MemberRepo memberRepository;
    private final RegionRepository regionRepository;
    private final MatchingApplicationRepository matchingApplicationRepository;

    // 매칭글 생성
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
                .comment(matchingDto.getComment())
                .createTime(currentTime)
                .status(null) // 상태를 null로 설정
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

    // 매칭 ID에 해당하는 모든 매칭 신청 가져오기
    public List<MatchingApplication> getApplicationsByMatchingId(Long matchingId) {
        return matchingApplicationRepository.findAllByMatching_Id(matchingId);
    }

    // 매칭글 수정
    public MatchingDto updateMatching(Long id, MatchingDto dto) {
        // 해당 ID의 매칭글을 조회
        Matching matching = matchingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));

        // 매칭글에 변경된 내용 적용
        matching.setComment(dto.getComment());
        matching.setStatus(dto.getStatus());

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

    public ResponseEntity<String> applyForMatching(Long matchingId, MatchingApplicationDto applicationDto) {
        // 매칭글 조회
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new IllegalArgumentException("Matching not found with id: " + matchingId));

        // 현재 사용자 조회
        Member applicant = getUserEntity();

        // 매칭글 작성자인 경우에는 매칭 신청을 거부
        if (matching.getMember().getId().equals(applicant.getId())) {
            String errorMessage = "자신의 매칭글에 매칭 신청을 할 수 없습니다.";
            log.error(errorMessage); // 로그 출력
            return ResponseEntity.badRequest().body(errorMessage);
        }

        // 이미 해당 매칭글에 매칭 신청을 했는지 확인
        boolean hasApplied = matching.getApplications().stream()
                .anyMatch(application -> application.getApplicant().getId().equals(applicant.getId()));
        if (hasApplied) {
            String errorMessage = "중복 매칭 신청이 불가합니다.";
            log.error(errorMessage); // 로그 출력
            return ResponseEntity.badRequest().body(errorMessage);
        }

        // 매칭 신청 생성
        MatchingApplication matchingApplication = MatchingApplication.builder()
                .matching(matching)
                .applicant(applicant)
                .message(applicationDto.getMessage())
                .status(MatchingStatus.PENDING)
                .build();

        // 매칭글에 매칭 신청 추가
        matching.getApplications().add(matchingApplication);

        // 매칭 상태를 PENDING으로 변경
        matching.setStatus(MatchingStatus.PENDING);

        // 매칭 저장
        matchingRepository.save(matching);

        // 매칭 신청 성공 메시지 반환
        return ResponseEntity.ok("매칭 신청이 성공적으로 완료되었습니다.");
    }

    // 특정 사용자가 특정 매칭글에 대한 매칭 신청 취소
    public void cancelMatchingApplication(MatchingApplication application) {
        matchingApplicationRepository.delete(application);
    }
    // 특정 사용자가 특정 매칭에 대한 매칭 신청 조회
    public MatchingApplication getApplicationById(Long matchingId, Long userId) {
        // 매칭 신청 조회
        return matchingApplicationRepository.findByMatching_IdAndApplicant_Id(matchingId, userId);
    }


    // 매칭 신청 수락
    public void acceptMatchingApplication(Long applicationId) {
        // 매칭 신청 엔티티 조회
        MatchingApplication application = matchingApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Matching application not found with id: " + applicationId));

        // 매칭 신청의 상태를 수락으로 변경
        application.setStatus(MatchingStatus.ACCEPTED); // 상태를 수락으로 변경

        // 매칭 신청 엔티티 저장
        matchingApplicationRepository.save(application);
    }

    // 매칭 신청 거절
    public void rejectMatchingApplication(Long applicationId) {
        // 매칭 신청 엔티티 조회
        MatchingApplication application = matchingApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Matching application not found with id: " + applicationId));

        // 매칭 신청의 상태를 거절로 변경
        application.setStatus(MatchingStatus.REJECTED); // 상태를 거절로 변경

        // 매칭 신청 엔티티 저장
        matchingApplicationRepository.save(application);
    }




}