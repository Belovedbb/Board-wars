package com.board.wars.repository.custom;

import com.board.wars.domain.ActivityFrequencyData;
import com.board.wars.domain.ActivityFrequencyDataPoint;
import reactor.core.publisher.Mono;

public interface CustomActivityFrequencyRepository {
    Mono<ActivityFrequencyData> insertDataPoint(ActivityFrequencyData data, ActivityFrequencyDataPoint point);
}
