package com.arthmate.enachapi.utils.security;

import java.util.Optional;

import com.arthmate.enachapi.exception.EnachDetailsNotFoundException;
import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.repo.EnachDetailsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtEnachDetailsTokenService {

    private final EnachDetailsRepo enachDetailsRepo;


    public JwtTokenDetails loadEnachDetailsByRequestId(String requestId) throws EnachDetailsNotFoundException {
        Optional<EnachDetail> enchObj =  enachDetailsRepo.getEnachDetailsByRequestId(requestId);
        if (enchObj.isPresent()) {
            return JwtTokenDetails.builder().reqsuestId(requestId).build();
        } else {
            throw new EnachDetailsNotFoundException("Record not found with request_id: " + requestId);
        }
    }
}