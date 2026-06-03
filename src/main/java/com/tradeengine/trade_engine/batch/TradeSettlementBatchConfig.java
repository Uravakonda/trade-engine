package com.tradeengine.trade_engine.batch;

import com.tradeengine.trade_engine.domain.Trade;
import com.tradeengine.trade_engine.domain.TradeStatus;
import com.tradeengine.trade_engine.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.skip.LimitCheckingExceptionHierarchySkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemReaderBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TradeSettlementBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TradeRepository tradeRepository;
    private final JobOperator jobOperator;


    @Bean
    @StepScope
    public RepositoryItemReader<Trade> executedTradesReader() {
        return new RepositoryItemReaderBuilder<Trade>()
                .name("executedTradesReader")
                .repository(tradeRepository)
                .methodName("findByStatus")
                .arguments(List.of(TradeStatus.EXECUTED))
                .pageSize(10)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }


    @Bean
    public ItemProcessor<Trade, Trade> settleTradeProcessor() {
        return trade -> {
            log.debug("Settling trade ID={} User={}", trade.getId(), trade.getUserId());
            trade.setStatus(TradeStatus.SETTLED);
            return trade;
        };
    }


    @Bean
    public ItemWriter<Trade> settledTradesWriter() {
        return chunk -> {
            tradeRepository.saveAll(chunk.getItems());
            log.info("Settled {} trades in this chunk", chunk.size());
        };
    }


    @Bean
    public Step settlementStep() {

        SkipPolicy skipPolicy = new LimitCheckingExceptionHierarchySkipPolicy(
                Set.of(Exception.class),
                5
        );


        return new ChunkOrientedStepBuilder<Trade, Trade>(
                "settlementStep",
                jobRepository,
                10)
                .transactionManager(transactionManager)
                .reader(executedTradesReader())
                .processor(settleTradeProcessor())
                .writer(settledTradesWriter())
                .faultTolerant()
                .skipPolicy(skipPolicy)
                .build();
    }


    @Bean
    public Job tradeSettlementJob() {
        return new JobBuilder("tradeSettlementJob", jobRepository)
                .start(settlementStep())
                .listener(new JobExecutionListener() {
                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        log.info("=== Settlement job STARTED at {} ===",
                                LocalDateTime.now());
                    }
                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        log.info("=== Settlement job FINISHED. Status={} ===",
                                jobExecution.getStatus());
                    }
                })
                .build();
    }


    @Scheduled(cron = "0 0 1 * * *")
    public void runSettlementJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("runDate", LocalDateTime.now().toString())
                    .toJobParameters();
            JobExecution execution = jobOperator.start(tradeSettlementJob(), params);
            log.info("Scheduled settlement completed. Status={}", execution.getStatus());
        } catch (Exception e) {
            log.error("Scheduled settlement failed: {}", e.getMessage(), e);
        }
    }
}