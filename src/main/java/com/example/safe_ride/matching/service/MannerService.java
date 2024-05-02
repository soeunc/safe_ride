package com.example.safe_ride.matching.service;

import com.example.safe_ride.matching.entity.Manner;
import com.example.safe_ride.matching.entity.Matching;
import com.example.safe_ride.matching.entity.MatchingApplication;
import com.example.safe_ride.matching.entity.MatchingStatus;
import com.example.safe_ride.matching.repository.MannerRepository;
import com.example.safe_ride.matching.repository.MatchingApplicationRepository;
import com.example.safe_ride.matching.repository.MatchingRepository;
import com.example.safe_ride.member.entity.Member;
import com.example.safe_ride.member.repo.MemberRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MannerService {
    private final MannerRepository mannerRepository;
    private final MemberRepo memberRepository;
    private final MatchingRepository matchingRepository;
    private final MatchingApplicationRepository matchingApplicationRepository;

    private Member getUserEntity() {
        UserDetails userDetails =
                (UserDetails) (SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        return memberRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public void rateManner(Long matchingId, int score, String comment) {
        Member rater = getUserEntity();
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (matching.getStatus() != MatchingStatus.END) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "매칭이 종료되지 않았습니다.");
        }
        Member ratedMember = null;
        // 매칭글 작성자가 평가를 할 경우
        if (matching.getMember().getId().equals(rater.getId())) {
            // 해당 매칭글에 대한 매칭 신청이 있는 경우에만 신청자를 평가 대상으로 설정
            Optional<MatchingApplication> application = matchingApplicationRepository.findByMatchingId(matchingId);
            if (!application.isEmpty()) {
                ratedMember = application.get().getApplicant();
            }
        } else {
            // 매칭글 작성자가 아닌 경우에는 매칭글 작성자를 평가 대상으로 설정
            ratedMember = matching.getMember();

        }


        Manner manner = Manner.builder()
                .ratedMember(ratedMember) // 매칭글 작성자가 평가 받는 회원
                .raterMember(rater) // 평가하는 회원
                .score(score) // 매너 점수
                .comment(comment) // 매너 후기
                .matching(matching) // 해당 매칭 정보
                .build();

        mannerRepository.save(manner);
        log.info("매너 평가가 완료되었습니다.");
    }

}
