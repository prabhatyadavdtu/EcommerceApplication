package com.example.ecommerceapplication.repository;

import com.example.ecommerceapplication.model.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, Long> {
    OtpEntity findByPhoneNumber(String phoneNumber);
}