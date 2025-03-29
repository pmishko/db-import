package com.example.DBImport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DataRecordRepository extends JpaRepository<DataRecord, Long> {

    @Query("SELECT MIN(d.dateInsert) FROM DataRecord d")
    LocalDateTime findMinDateInsert();

    @Query("SELECT MAX(d.dateInsert) FROM DataRecord d")
    LocalDateTime findMaxDateInsert();

    @Query("SELECT MAX(d.sequenceNumber) FROM DataRecord d WHERE d.matchId = ?1")
    Long findMaxSequenceNumberByMatchId(String matchId);

}
