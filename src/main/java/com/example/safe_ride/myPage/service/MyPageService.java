package com.example.safe_ride.myPage.service;

import com.example.safe_ride.myPage.repo.MyPageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final MyPageRepo myPageRepo;
}
