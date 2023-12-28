package com.arthmate.enachapi.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Slf4j
@Configuration
public class AwsS3Service {

    @Value("${aws.credentials.access-key}")
    private String awsS3AccessKey;
    @Value("${aws.credentials.secret-key}")
    private String awsS3SecretKey;
    @Value("${aws.s3.region}")
    private String awsS3Region;
    @Value("${aws.s3.bucket}")
    private String awsS3Bucket;

    private AmazonS3 amazonS3Client;

    @PostConstruct
    public void initializeAwsS3() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(awsS3AccessKey, awsS3SecretKey);
        this.amazonS3Client = AmazonS3ClientBuilder.standard()
                .withRegion(awsS3Region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    public String uploadStreamToS3Bucket(InputStream inputStream, String fileName, ObjectMetadata metadata) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(awsS3Bucket, fileName, inputStream, metadata);
        amazonS3Client.putObject(putObjectRequest);
        return ((AmazonS3Client) amazonS3Client).getResourceUrl(awsS3Bucket, fileName);
    }

}
