package com.tradeengine.trade_engine.controller;

import com.tradeengine.trade_engine.batch.TradeSettlementBatchConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
@Slf4j
public class BatchController {

    /*
     * Theory — why JobOperator instead of JobLauncher:
     *
     * JobLauncher was the original Spring Batch API for starting jobs.
     * In Spring Batch 6 it was deprecated and replaced by JobOperator,
     * which provides a richer interface: you can start, stop, restart,
     * and abandon jobs. JobOperator is also the standard used in
     * enterprise scheduling integrations (Quartz, Jenkins, etc.).
     *
     * Spring Boot auto-configures a JobOperator bean automatically
     * when spring-boot-starter-batch is on the classpath, so you
     * do not need to declare it yourself — @RequiredArgsConstructor
     * and the field declaration are all that's needed.
     */
    private final JobOperator jobOperator;
    private final TradeSettlementBatchConfig batchConfig;

    @PostMapping("/run-settlement")
    public ResponseEntity<String> runSettlement() {
        try {
            Job job = batchConfig.tradeSettlementJob();

            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            /*
             * JobOperator.start() accepts the job name as a String
             * and the JobParameters object. It returns a JobExecution
             * which contains the status (STARTED, COMPLETED, FAILED etc.)
             * and metadata about the run (start time, step executions, etc.)
             */
            JobExecution execution = jobOperator.start(job, params);

            log.info("Settlement job launched manually. Status={}", execution.getStatus());
            return ResponseEntity.ok("Job launched. Status: " + execution.getStatus());

        } catch (Exception e) {
            log.error("Failed to launch settlement job: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Job failed to launch: " + e.getMessage());
        }
    }
}