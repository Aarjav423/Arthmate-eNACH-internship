package com.arthmate.enachapi.batch.processor;

import com.arthmate.enachapi.dto.MandateLinkNotificationDto;
import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.service.MandateLinkNotificationService;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MandateLinkNotificationProcessor implements ItemProcessor<EnachDetail, MandateLinkNotificationDto> {

    @Autowired
    private MandateLinkNotificationService mandateLinkNotificationService;

    @Override
    public MandateLinkNotificationDto process(EnachDetail item) throws Exception {
        return mandateLinkNotificationService.sendMandateLinkNotification(item);
    }

}
