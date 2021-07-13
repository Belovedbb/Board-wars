package com.board.wars.repository.custom;

import com.board.wars.domain.CumulativeFlowDiagram;
import com.board.wars.domain.FlowDiagram;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface CustomCumulativeFlowDiagramRepository {
    Mono<CumulativeFlowDiagram> insertNewFlowDiagramToCumulativeFlowDiagram(Long projectCode, FlowDiagram flowDiagram);
    Mono<CumulativeFlowDiagram> findByFlowDiagram(Long projectCode, Pageable pageable);
    Mono<CumulativeFlowDiagram> findByFlowDiagram(Long projectCode, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
