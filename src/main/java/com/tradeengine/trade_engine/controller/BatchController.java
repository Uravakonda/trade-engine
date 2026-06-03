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


    private final JobOperator jobOperator;
    private final TradeSettlementBatchConfig batchConfig;

    @PostMapping("/run-settlement")
    public ResponseEntity<String> runSettlement() {
        try {
            Job job = batchConfig.tradeSettlementJob();

            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();


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