package com.example.safe_ride.matching.repository;

import com.example.safe_ride.matching.entity.Manner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MannerRepository extends JpaRepository<Manner, Long> {
}
