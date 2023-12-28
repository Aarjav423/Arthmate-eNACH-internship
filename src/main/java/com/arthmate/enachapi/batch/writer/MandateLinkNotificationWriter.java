package com.arthmate.enachapi.batch.writer;

import com.arthmate.enachapi.dto.MandateLinkNotificationDto;
import com.arthmate.enachapi.service.MandateLinkNotificationService;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class MandateLinkNotificationWriter extends MongoItemWriter<MandateLinkNotificationDto> {

    @Autowired
    private MandateLinkNotificationService mandateLinkNotificationService;

    @Override
    public void write(List<? extends MandateLinkNotificationDto> items) throws Exception {
        mandateLinkNotificationService.updateMandateLinkNotificationStatus(items);
    }

}
