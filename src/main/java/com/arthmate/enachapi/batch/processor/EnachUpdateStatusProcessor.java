package com.arthmate.enachapi.batch.processor;

import com.arthmate.enachapi.dto.EnachUpdateStatusDto;
import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.model.EnachDetailHistory;
import com.arthmate.enachapi.service.EnachDetailsHistoryService;
import com.arthmate.enachapi.service.EnachDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnachUpdateStatusProcessor implements ItemProcessor<EnachDetail, EnachUpdateStatusDto> {
    private final EnachDetailsService enachDetailsService;
    private final EnachDetailsHistoryService enachDetailsHistoryService;

    @Value("${enach.update.status}")
    private String ActiveStatus;

    @Override
    public EnachUpdateStatusDto process(EnachDetail enachDetailCln) throws Exception{
        EnachDetailHistory enachDetailHistory = enachDetailsHistoryService.getEnachDetailStatusObject(enachDetailCln);
        enachDetailCln.setStatus(ActiveStatus);

        return EnachUpdateStatusDto.builder()
                .enachDetail(enachDetailCln)
                .enachDetailHistory(enachDetailHistory)
                .build();
    }

}
