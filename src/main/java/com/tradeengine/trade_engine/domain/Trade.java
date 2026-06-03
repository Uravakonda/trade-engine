package com.tradeengine.trade_engine.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * Theory — @Entity and table mapping:
 *
 * @Entity tells JPA this class represents a database table.
 * @Table(name = "trades") sets the table name explicitly — without it
 * Hibernate would use the class name "Trade" as the table name, which
 * works but being explicit is better practice.
 *
 * @Id marks the primary key field.
 * @GeneratedValue(strategy = IDENTITY) means PostgreSQL's auto-increment
 * (SERIAL / BIGSERIAL) generates the ID value — you never set it manually.
 *
 * @Column(nullable = false) adds a NOT NULL constraint to that column.
 * precision and scale on BigDecimal control decimal storage:
 *   precision = total number of digits, scale = digits after the decimal.
 *   precision=18, scale=8 supports values up to 9,999,999,999.99999999 —
 *   appropriate for financial quantities and prices.
 *
 * @Enumerated(EnumType.STRING) stores enums as their string name ("BUY",
 * "SELL") rather than an integer index. Always use STRING — if you later
 * reorder the enum, integer indexes break silently.
 *
 * @PrePersist runs automatically just before Hibernate inserts a new row,
 * setting createdAt to the current time. You never set it manually.
 *
 * Lombok annotations:
 *   @Data       = getters + setters + equals + hashCode + toString
 *   @Builder    = Trade.builder().ticker("AAPL").quantity(...).build()
 *   @NoArgsConstructor  = required by JPA (it needs a no-arg constructor)
 *   @AllArgsConstructor = used by @Builder internally
 */
@Entity
@Table(name = "trades")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String ticker;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TradeType type;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal totalValue;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TradeStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}