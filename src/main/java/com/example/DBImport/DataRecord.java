package com.example.DBImport;


import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "match_id", nullable = false)
    private String matchId;
    @Column(name = "market_id", nullable = false)
    private String marketId;
    @Column(name = "outcome_id", nullable = false)
    private String outcomeId;
    @Column(name = "specifiers")
    private String specifiers;
    @Column(name = "date_insert", nullable = false)
    private LocalDateTime dateInsert;
    @Column(name = "sequence_number", nullable = false)
    private Long sequenceNumber;
    @Column(name = "original_order", nullable = false)
    private Long originalOrder;

}
