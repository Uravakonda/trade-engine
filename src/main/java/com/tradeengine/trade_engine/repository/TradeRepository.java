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

    List<Trade> findByUserId(String userId);


    List<Trade> findByStatus(TradeStatus status);


    Page<Trade> findByStatus(TradeStatus status, Pageable pageable);

    @Query("SELECT t FROM Trade t WHERE t.status = 'EXECUTED' " +
            "AND t.createdAt >= :startOfDay AND t.createdAt < :endOfDay")
    List<Trade> findExecutedTradesForDay(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
}