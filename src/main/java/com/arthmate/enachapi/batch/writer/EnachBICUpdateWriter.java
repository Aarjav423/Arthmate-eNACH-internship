package com.arthmate.enachapi.batch.writer;

import com.arthmate.enachapi.client.EnachBICUpdateApiClient;
import com.arthmate.enachapi.dto.BICReqDto;
import com.arthmate.enachapi.dto.EnachDtlRqstBdy;
import com.arthmate.enachapi.repo.EnachDetailsRepo;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class EnachBICUpdateWriter implements ItemWriter<BICReqDto> {

    @Autowired
    private EnachDetailsRepo enachDetailsRepo;

    @Autowired
    private EnachBICUpdateApiClient enachBICUpdateApiClient;

    @Override
    public void write(List<? extends BICReqDto> items) throws Exception {
        if(!items.isEmpty()){
        for (BICReqDto item : items) {
            try {
              var apiResponse =  enachBICUpdateApiClient.updateBICApi(item);
              if("success".equalsIgnoreCase(apiResponse.getStatus())){
                  UpdateResult result = enachDetailsRepo.updateStatusToNotified(item.getId());
                  log.info("enach details updated with status {} for external_ref_num:{}",apiResponse.getMessage(),item.getExternalRefNum());
              }
                if("fail".equalsIgnoreCase(apiResponse.getStatus())){
                    UpdateResult result = enachDetailsRepo.updateStatusToFailed(item.getId());
                    log.info("enach details update failed with status {} for external_ref_num:{}",apiResponse.getMessage(),item.getExternalRefNum());
                }
            }
            catch(Exception e){
                log.info("exception from patch api {}",e);
            };
        }}
    }
}
