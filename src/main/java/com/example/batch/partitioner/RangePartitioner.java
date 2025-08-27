package com.example.batch.partitioner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RangePartitioner implements Partitioner {
    
    private final JdbcTemplate jdbcTemplate;
    
    public RangePartitioner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        log.info("パーティション作成開始。グリッドサイズ: {}", gridSize);
        
        // データの総件数を取得
        Integer totalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM employees", Integer.class);
        
        if (totalCount == null || totalCount == 0) {
            log.warn("処理対象データがありません");
            return new HashMap<>();
        }
        
        // 各パーティションのサイズを計算
        int partitionSize = (totalCount + gridSize - 1) / gridSize;
        
        Map<String, ExecutionContext> partitions = new HashMap<>();
        
        for (int i = 0; i < gridSize; i++) {
            int minValue = i * partitionSize;
            int maxValue = Math.min((i + 1) * partitionSize - 1, totalCount - 1);
            
            ExecutionContext context = new ExecutionContext();
            context.putInt("minValue", minValue);
            context.putInt("maxValue", maxValue);
            context.putInt("partitionNumber", i);
            context.putString("name", "partition" + i);
            
            partitions.put("partition" + i, context);
            
            log.info("パーティション {} を作成: 範囲 [{} - {}]", i, minValue, maxValue);
        }
        
        return partitions;
    }
}