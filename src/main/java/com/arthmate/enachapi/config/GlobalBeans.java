package com.arthmate.enachapi.config;

import com.arthmate.enachapi.dto.EnachDtlAmendRqstBdy;
import com.arthmate.enachapi.dto.EnachDtlRqstBdy;
import com.arthmate.enachapi.dto.EnachTxnRqstBdy;
import com.arthmate.enachapi.model.*;
import com.arthmate.enachapi.repo.SingleDataTranslationRepo;
import com.arthmate.enachapi.service.BankLiveStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class GlobalBeans {

    @Value("${enach.frequency.translation}")
    private String freqFlag;

    @Value("${enach.mandate.purpose.translation}")
    private String purposeFlag;
    private final SingleDataTranslationRepo singleDataTranslationRepo;
    private final BankLiveStatusService bankLiveStatusService;

    @Value("${npci.livebankstatus.url}")
    String liveBankStatusUri;

    @Bean
    public Map<String, String> enachFrequencyMap(){
        Map<String, String> map = new HashMap<String, String>();
        Optional<List<SingleDataTranslation>> optList =  singleDataTranslationRepo.getFrequencyList();
        if(optList.isPresent()){
            List<SingleDataTranslation> lst = optList.get();
            map = lst.stream().collect(Collectors.toMap(SingleDataTranslation::getKey,SingleDataTranslation::getValue));
        }else{
            log.info("No Frequency {Key,value} pair found in SingleDataTranslation collection for type: {}",freqFlag);
        }
        return map;
    }

    @Bean
    public Map<String, String> enachMandatePurposeMap(){
        Map<String, String> map = new HashMap<String, String>();
        Optional<List<SingleDataTranslation>> optList =  singleDataTranslationRepo.getMandatePurposeList();
        if(optList.isPresent()){
            List<SingleDataTranslation> lst = optList.get();
            map = lst.stream().collect(Collectors.toMap(SingleDataTranslation::getKey,SingleDataTranslation::getValue));
        }else{
            log.info("No Purpose of mandate {Key,value} pair found in SingleDataTranslation collection for type: {}",purposeFlag);
        }
        return map;
    }

    @Bean
    public Map<String, LiveBankStatusResponseBody> bankDetails() {
       Map<String, LiveBankStatusResponseBody> map;
        map= bankLiveStatusService.getNPCIBankStatus(liveBankStatusUri);
        if(map.isEmpty()){
           log.info("Bank Details map is empty");
        }
        return map;
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return (mapperBuilder) -> mapperBuilder.modulesToInstall(new JaxbAnnotationModule());
    }

    @Bean
    ModelMapper enachDetailAuditTrailMapper() {
        ModelMapper modelMapper = new ModelMapper();
        try {
            if(modelMapper.getTypeMap(EnachDetail.class, EnachDetailHistory.class) == null) {
                TypeMap<EnachDetail, EnachDetailHistory> propertyMapper = modelMapper.createTypeMap(EnachDetail.class, EnachDetailHistory.class);

                propertyMapper.addMapping(EnachDetail::getRequestId, EnachDetailHistory::setReqId);
                modelMapper.addMappings(new PropertyMap<EnachDetail, EnachDetailHistory>() {
                    @Override
                    protected void configure() {
                        skip(destination.getId());
                    }
                });
            }
        }catch (IllegalStateException e){
            log.error(" Exception : {}",e.getMessage() );
        }
        return modelMapper;
    }

    @Bean
    ModelMapper amendRequstEnachMapper() {
        ModelMapper modelMapper = new ModelMapper();
        try {
            modelMapper.addMappings(new PropertyMap<EnachDtlAmendRqstBdy, EnachDetail>() {
                @Override
                protected void configure() {
                    skip(destination.getStartDate());
                    skip(destination.getEndDate());
                }
            });
        }catch (ConfigurationException e){
            log.error("Exception : {}",e.getMessage() );
        }
        return modelMapper;
    }

    @Bean
    ModelMapper enachRequestMapper() {
        ModelMapper modelMapper = new ModelMapper();
        try {
            if(modelMapper.getTypeMap(EnachDtlRqstBdy.class, EnachDetail.class) == null) {
                TypeMap<EnachDtlRqstBdy, EnachDetail> typeMap = modelMapper.createTypeMap(EnachDtlRqstBdy.class, EnachDetail.class);
                typeMap.addMapping(EnachDtlRqstBdy::getCompanyId, EnachDetail::setCompanyId);
                typeMap.addMapping(EnachDtlRqstBdy::getAmount, EnachDetail::setAmount);
            }
        }catch (IllegalStateException e){
            log.error(" Exception : {}",e.getMessage() );
        }
        return modelMapper;
    }

    @Bean
    ModelMapper enachTransactionMapper() {
        ModelMapper modelMapper = new ModelMapper();
        try {
            if(modelMapper.getTypeMap(EnachTxnRqstBdy.class, NachTransactions.class) == null) {
                TypeMap<EnachTxnRqstBdy, NachTransactions> typeMap = modelMapper.createTypeMap(EnachTxnRqstBdy.class, NachTransactions.class);
                typeMap.addMapping(EnachTxnRqstBdy::getCompanyId, NachTransactions::setCompanyId);
                typeMap.addMapping(EnachTxnRqstBdy::getAmount, NachTransactions::setAmount);
            }
        }catch (IllegalStateException e){
            log.error(" Exception : {}",e.getMessage() );
        }
        return modelMapper;
    }

}
