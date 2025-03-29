package com.example.DBImport;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataProcessingService {

    private final DataRecordRepository dataRecordRepository;
    private final ResourceLoader resourceLoader;

    @Value("${app.data.file-path}")
    private String dataFilePath;

    @Value("${app.processing.batch-size}")
    private int batchSize;

    @Value("${app.processing.thread-pool-size}")
    private int threadPoolSize;

    // process the data on app start
    @PostConstruct
    public void init() {
        try {
            processDataFile();
            printDateInsertRange();
        } catch (Exception e) {
            log.error("Error processing data file", e);
        }
    }

    // reads the data file, not okay for a real world scenario (steady data flow)
    public void processDataFile() throws IOException, InterruptedException, ExecutionException {
        log.info("Starting to process data file: {}", dataFilePath);
        Resource resource = resourceLoader.getResource(dataFilePath);
        List<String> allLines = readAllLines(resource);
        log.info("Read {} lines from file", allLines.size());

        Map<String, List<String>> dataByMatchId = groupByMatchId(allLines);
        log.info("Grouped data into {} match_ids", dataByMatchId.size());

        processMatchGroups(dataByMatchId);
        log.info("Completed processing data file");
    }


     //print min and max date_insert
    public void printDateInsertRange() {
        LocalDateTime minDate = dataRecordRepository.findMinDateInsert();
        LocalDateTime maxDate = dataRecordRepository.findMaxDateInsert();

        log.info("Min date_insert: {}", minDate);
        log.info("Max date_insert: {}", maxDate);
    }

    private List<String> readAllLines(Resource resource) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("MATCH_ID")) { // Skip header
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private Map<String, List<String>> groupByMatchId(List<String> lines) {
        Map<String, List<String>> dataByMatchId = new HashMap<>();

        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 3) { // specifiers can be null
                String matchId = parts[0].trim().replace("'", "");
                dataByMatchId.computeIfAbsent(matchId, k -> new ArrayList<>()).add(line);
            }
        }

        return dataByMatchId;
    }

    private void processMatchGroups(Map<String, List<String>> dataByMatchId)
            throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<?>> futures = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : dataByMatchId.entrySet()) {
            String matchId = entry.getKey();
            List<String> matchLines = entry.getValue();

            futures.add(executor.submit(() -> processMatchIdGroup(matchId, matchLines)));
        }

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();
    }

    @Transactional
    public void processMatchIdGroup(String matchId, List<String> lines) {
        log.info("Processing match_id: {} with {} records", matchId, lines.size());

        // get the current sequence number for this match_id
        Long currentSequence = dataRecordRepository.findMaxSequenceNumberByMatchId(matchId);
        if (currentSequence == null) {
            currentSequence = 0L;
        }

        // process data for this match_id
        List<DataRecord> batch = new ArrayList<>(batchSize);
        long originalOrder = 0;

        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 3) {
                String marketId = parts[1].trim().replace("'", "");
                String outcomeId = parts[2].trim().replace("'", "");

                // check and clean specifiers if present
                String specifiers = "";
                if (parts.length > 3 && parts[3] != null && !parts[3].trim().isEmpty()) {
                    specifiers = parts[3].trim().replace("'", "");
                }
                currentSequence++;

                // create record with ordered sequence
                DataRecord record = DataRecord.builder()
                        .matchId(matchId)
                        .marketId(marketId)
                        .outcomeId(outcomeId)
                        .specifiers(specifiers)
                        .dateInsert(LocalDateTime.now()) // Current timestamp
                        .sequenceNumber(currentSequence)
                        .originalOrder(++originalOrder)
                        .build();

                batch.add(record);

                // save in batches
                if (batch.size() >= batchSize) {
                    dataRecordRepository.saveAll(batch);
                    batch.clear();
                }

                // this should not be here in a real world scenario
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // save any remaining records
        if (!batch.isEmpty()) {
            dataRecordRepository.saveAll(batch);
        }
        log.info("Completed processing match_id: {}", matchId);
    }
}
