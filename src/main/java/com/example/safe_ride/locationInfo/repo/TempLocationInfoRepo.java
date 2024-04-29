package com.example.safe_ride.locationInfo.repo;

import com.example.safe_ride.locationInfo.entity.TempLocationInfoResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempLocationInfoRepo extends JpaRepository<TempLocationInfoResponse, Long> {

}
