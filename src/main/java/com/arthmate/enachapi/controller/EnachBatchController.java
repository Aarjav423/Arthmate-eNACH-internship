package com.arthmate.enachapi.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EnachBatchController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("enachRequestStatusJob")
    private Job enachRequestStatusJob;

    @Autowired
    @Qualifier("enachUpdateStatusJob")
    private Job enachUpdateStatusJob;

    @Autowired
    @Qualifier("mandateLinkNotificationJob")
    private Job mandateLinkNotificationJob;

    @Autowired
    @Qualifier("enachBICUpdateStatusJob")
    private Job enachBICUpdateStatusJob;

    @Autowired
    @Qualifier("scheduleNachTransactionJob")
    private Job scheduleNachTransactionJob;

    @Autowired
    @Qualifier("scheduleNachTxnStatusJob")
    private Job scheduleNachTxnStatusJob;

    @Autowired
    @Qualifier("transactionSuccessSmsJob")
    private Job transactionSuccessSmsJob;


    @Scheduled(cron = "${enach.request.status.cron}")
    public void launchNpciRequestStatusJob() throws Exception {
        JobParameters jobParams = new JobParametersBuilder()
                .addString("JobId", "Npci_Request_Status_Job_" + System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(enachRequestStatusJob, jobParams);
    }

    @Scheduled(cron = "${enach.bic.status.cron}")
    public void launchEnachBICUpdateStatusJob() throws Exception {
        JobParameters jobParams = new JobParametersBuilder()
                .addString("JobId", "Enach_BIC_Status_Job_" + System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(enachBICUpdateStatusJob, jobParams);
    }

    @Scheduled(cron = "${enach.update.status.cron}")
    public void launchNpciUpdateStatusJob() throws Exception {
        JobParameters jobParams = new JobParametersBuilder()
                .addString("JobId", "Npci_Update_Status_Job_" + System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(enachUpdateStatusJob, jobParams);
    }

    @Scheduled(cron = "${mandate.link.notification.cron}")
    public void launchMandateLinkNotificationJob() throws Exception {
        JobParameters jobParams = new JobParametersBuilder()
                .addString("JobId", "Mandate_Link_Notification_Job_" + System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(mandateLinkNotificationJob, jobParams);
    }

    @Scheduled(cron = "${schedule.nach.transaction.cron}")
    public void launchScheduleNachTransactionJob() throws Exception {
        JobParameters jobParams = new JobParametersBuilder()
                .addString("JobId", "Schedule_Nach_Transaction_Job_" + System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(scheduleNachTransactionJob, jobParams);
    }

    @Scheduled(cron = "${schedule.nach.transaction.status.cron}")
    public void launchScheduleNachTxnStatusJob() throws Exception {
        JobParameters jobParams = new JobParametersBuilder()
                .addString("JobId", "Schedule_Nach_Transaction_Status_Job_" + System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(scheduleNachTxnStatusJob, jobParams);
    }

    @Scheduled(cron = "${txn.success.sms.batch.cron}")
    public void launchTransactionSuccessSmsJob() throws Exception {
        JobParameters jobParams = new JobParametersBuilder()
                .addString("JobId", "Transaction_Success_Sms_Job_" + System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(transactionSuccessSmsJob, jobParams);
    }

}
