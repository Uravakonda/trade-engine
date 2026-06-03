package com.tradeengine.trade_engine.repository;

import com.tradeengine.trade_engine.domain.Trade;
import com.tradeengine.trade_engine.domain.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    // Used by service layer — single argument
    List<Trade> findByUserId(String userId);

    // Used by service layer — single argument
    List<Trade> findByStatus(TradeStatus status);

    /*
     * Used by RepositoryItemReader in the batch job.
     * RepositoryItemReader appends a Pageable argument when calling the method,
     * so the signature must accept Pageable as a second parameter.
     * Spring Data derives: SELECT * FROM trades WHERE status = ? with pagination.
     */
    Page<Trade> findByStatus(TradeStatus status, Pageable pageable);

    @Query("SELECT t FROM Trade t WHERE t.status = 'EXECUTED' " +
            "AND t.createdAt >= :startOfDay AND t.createdAt < :endOfDay")
    List<Trade> findExecutedTradesForDay(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
}