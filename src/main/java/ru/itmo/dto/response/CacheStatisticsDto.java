package ru.itmo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheStatisticsDto {
    private long secondLevelCacheHits;
    private long secondLevelCacheMisses;
    private long secondLevelCachePuts;
    private long queryCacheHits;
    private long queryCacheMisses;
    private long queryCachePuts;

    public double getSecondLevelCacheHitRatio() {
        long total = secondLevelCacheHits + secondLevelCacheMisses;
        return total > 0 ? (double) secondLevelCacheHits / total * 100 : 0.0;
    }

    public double getQueryCacheHitRatio() {
        long total = queryCacheHits + queryCacheMisses;
        return total > 0 ? (double) queryCacheHits / total * 100 : 0.0;
    }
}
