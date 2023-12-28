package com.arthmate.enachapi.service;


import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.model.EnachDetailHistory;
import com.arthmate.enachapi.repo.EnachDetailHistoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class EnachDetailsHistoryService {


    private final EnachDetailHistoryRepo enachDetailHistoryRepo;

    private final ModelMapper enachDetailAuditTrailMapper;

    public EnachDetailHistory saveEnachDetailHistory(EnachDetail enachDetail) {
        EnachDetailHistory enachDetailHistory = new EnachDetailHistory();
        if(!enachDetailHistoryRepo.isNPCIRequestIdExists(enachDetail.getRequestId(),enachDetail.getNpciRequestId())) {
            log.info("Audit trail request is new for Request-Id {} & Npci-Request-Id {}, so additional node created.",enachDetail.getRequestId(),enachDetail.getNpciRequestId());
            enachDetailHistory = enachDetailHistoryRepo.save(enachDetailAuditTrailMapper.map(enachDetail, EnachDetailHistory.class));
        }else{
            log.info("Audit trail already exists for Request-Id {} & Npci-Request-Id {}, so no additional node created.",enachDetail.getRequestId(),enachDetail.getNpciRequestId());
            enachDetailHistory = getEnachDetailByRequest(enachDetail.getRequestId(),enachDetail.getNpciRequestId());
        }
        log.info("Enach audit trail for Reqest-ID {}. saved in EnachDetailHistory as Object {}",enachDetail.getRequestId(),enachDetailHistory.getId());
        return enachDetailHistory;
    }

    public EnachDetailHistory getEnachDetailByRequest(String requestId, String npciRequestId){
        EnachDetailHistory enachReq = null;
        Optional<EnachDetailHistory> enachOpt =  enachDetailHistoryRepo.getEnachDetailsByRequestId(requestId,npciRequestId);
        if(enachOpt.isPresent())
            enachReq = enachOpt.get();
        else
            log.info("EnachDetailHistory not found for request_id {}",requestId);
        return enachReq;
    }

    public EnachDetailHistory getEnachDetailStatusObject(EnachDetail enachDetail){
        EnachDetailHistory enachDetailHistory = new EnachDetailHistory();
        if(!enachDetailHistoryRepo.isNPCIRequestIdExists(enachDetail.getRequestId(),enachDetail.getNpciRequestId())) {
            log.info("Audit trail request is new for Request-Id {} & Npci-Request-Id {}, so additional node created.",enachDetail.getRequestId(),enachDetail.getNpciRequestId());
            enachDetailHistory = enachDetailAuditTrailMapper.map(enachDetail, EnachDetailHistory.class);
        }else{
            log.info("Audit trail already exists for Request-Id {} & Npci-Request-Id {}, so no additional node created.",enachDetail.getRequestId(),enachDetail.getNpciRequestId());
            enachDetailHistory = getEnachDetailByRequest(enachDetail.getRequestId(),enachDetail.getNpciRequestId());
        }
        log.info("Enach audit trail for Reqest-ID {}. saved in EnachDetailHistory as Object {}",enachDetail.getRequestId(),enachDetailHistory.getId());
        return enachDetailHistory;
    }

    public EnachDetailHistory getStatusBeforeSuspendByRequestId(String requestId){
        EnachDetailHistory enachReq = null;
        List<EnachDetailHistory> lst =  enachDetailHistoryRepo.getDescOrderdListOfEnachDetailHistory(requestId);
        if(!lst.isEmpty())
            enachReq = lst.get(0);
        return enachReq;
    }
}
