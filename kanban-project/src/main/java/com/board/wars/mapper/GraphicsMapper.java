package com.board.wars.mapper;

import com.board.wars.domain.ActivityFrequencyData;
import com.board.wars.domain.Column;
import com.board.wars.domain.CumulativeFlowDiagram;
import com.board.wars.domain.FlowDiagram;
import com.board.wars.payload.response.ActivityFrequencyDataPointResponsePayload;
import com.board.wars.payload.response.ActivityFrequencyDataResponsePayload;
import com.board.wars.payload.response.CumulativeFlowDiagramResponsePayload;
import com.board.wars.payload.response.FlowDiagramResponsePayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.CollectionUtils;

@Mapper(uses = {ColumnMapper.class, TaskMapper.class})
public interface GraphicsMapper {

    CumulativeFlowDiagramResponsePayload mapCumulativeFlowDiagramDomainToResponsePayload(CumulativeFlowDiagram cumulativeFlowDiagram);

    @Mapping(target = "taskSize", source = "column",  qualifiedByName = "flowTaskSize")
    FlowDiagramResponsePayload mapFlowDiagramDomainToResponsePayload(FlowDiagram flowDiagram);

    @Named("flowTaskSize")
    default Long flowTaskSize(Column column){
        return column == null || column.getTasks() == null || CollectionUtils.isEmpty(column.getTasks()) ? 0L : column.getTasks().size();
    }

    ActivityFrequencyDataResponsePayload mapActivityFrequencyDataDomainToResponsePayload(ActivityFrequencyData activityFrequencyData);

}
