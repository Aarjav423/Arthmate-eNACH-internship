package com.arthmate.enachapi.batch.processor;

import com.arthmate.enachapi.dto.EnachStatusResponseDto;
import com.arthmate.enachapi.service.EnachRequestStatusService;
import com.arthmate.enachapi.model.EnachDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import static com.arthmate.enachapi.utils.ApplicationConstants.NCPI_REQUEST_STATUS_BATCH;

@Component
@RequiredArgsConstructor
public class EnachRequestStatusProcessor implements ItemProcessor<EnachDetail, EnachStatusResponseDto> {

    private final EnachRequestStatusService enachRequestStatusService;

    @Override
    public EnachStatusResponseDto process(EnachDetail item) {
        return enachRequestStatusService.checkEnachRequestStatus(item, NCPI_REQUEST_STATUS_BATCH);
    }
}
