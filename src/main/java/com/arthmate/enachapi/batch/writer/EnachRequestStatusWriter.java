package com.arthmate.enachapi.batch.writer;

import com.arthmate.enachapi.dto.EnachStatusResponseDto;
import com.arthmate.enachapi.service.EnachRequestStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.arthmate.enachapi.utils.ApplicationConstants.NCPI_REQUEST_STATUS_BATCH;

@Slf4j
public class EnachRequestStatusWriter extends MongoItemWriter<EnachStatusResponseDto> {

    @Autowired
    private EnachRequestStatusService enachRequestStatusService;

    @Override
    public void write(List<? extends EnachStatusResponseDto> items) {
        enachRequestStatusService.updateEnachStatusDetails(items, NCPI_REQUEST_STATUS_BATCH);
    }

}
