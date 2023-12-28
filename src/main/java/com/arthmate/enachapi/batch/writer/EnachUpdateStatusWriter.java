package com.arthmate.enachapi.batch.writer;

import com.arthmate.enachapi.dto.EnachUpdateStatusDto;
import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.model.EnachDetailHistory;
import com.arthmate.enachapi.repo.EnachDetailsRepo;
import com.arthmate.enachapi.repo.EnachDetailHistoryRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class EnachUpdateStatusWriter extends MongoItemWriter<EnachUpdateStatusDto> {

    @Autowired
    private EnachDetailsRepo enachDetailsRepo;
    @Autowired
    private EnachDetailHistoryRepo enachDetailHistoryRepo;

    public static Pair<Query, Update> getEnachUpdateStatusQuery(EnachDetail enachDetail){
        return Pair.of(
                Query.query(Criteria.where("request_id").is(enachDetail.getRequestId())),
                Update.update("status",enachDetail.getStatus())
                        .set("updated_at", LocalDateTime.now())
                        .set("status_notified", 0)
        );
    }

    @Override
    public void write(List<? extends EnachUpdateStatusDto> items) throws Exception {
        int result1 = enachDetailsRepo.updateByQueryUpdatePairs(items.stream()
                .filter(obj -> Objects.nonNull(obj) && Objects.nonNull(obj.getEnachDetail()))
                .map(obj -> getEnachUpdateStatusQuery(obj.getEnachDetail()))
                .collect(Collectors.toList()));
        log.info("enach details updated with status : {}", result1);

        Collection<EnachDetailHistory> result2 = enachDetailHistoryRepo.insertAll(items.stream()
                .filter(obj -> Objects.nonNull(obj) && Objects.nonNull(obj.getEnachDetailHistory()))
                .map(EnachUpdateStatusDto::getEnachDetailHistory)
                .collect(Collectors.toList()));
        log.info("enach detail history status inserted: {}", result2.size());
    }
}
