package com.arthmate.enachapi.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.arthmate.enachapi.model.EnachReqResLog;
import com.arthmate.enachapi.repo.EnachReqResLogRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogToS3Service {

    private final AwsS3Service awsS3Service;
    private final EnachReqResLogRepo enachReqResLogRepo;

    public static StringBuilder writeLogToString(StringBuilder stringBuilder, String logs, String logHeader) {
        if (stringBuilder == null) stringBuilder = new StringBuilder();
        return stringBuilder.append("\n").append(logHeader).append("\n").append(logs).append("\n");
    }

    public String uploadLogsToS3(String fileName, String logs) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(logs.getBytes());
            byte[] byteArray = outputStream.toByteArray();
            InputStream inputStream = new ByteArrayInputStream(byteArray);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(byteArray.length);
            return awsS3Service.uploadStreamToS3Bucket(inputStream, fileName, metadata);

        } catch (Exception e) {
            log.error("Error occurred while uploading logs to s3: {}", e.getMessage());
            return null;
        }
    }

    public static EnachReqResLog buildEnachReqResLog(String requestId, String s3Url, String createdBy) {
        return EnachReqResLog.builder()
                .requestId(requestId)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .reqResS3Url(s3Url)
                .build();
    }

    public EnachReqResLog saveLogDetails(String requestId, String s3Url, String createdBy) {
        try {
            return enachReqResLogRepo.insert(buildEnachReqResLog(requestId, s3Url, createdBy));
        } catch (Exception e) {
            log.error("Exception occured while saving req-res log for request id: {} :: {}", requestId, e.getMessage());
            return null;
        }
    }

}
