package com.board.wars.mapper;

import com.board.wars.domain.Column;
import com.board.wars.payload.request.ColumnRequestPayload;
import com.board.wars.payload.response.ColumnResponsePayload;
import org.mapstruct.*;

@Mapper(uses = {TaskMapper.class})
public interface ColumnMapper {

    @Mapping(target = "colorCode", source = "color")
    @Mapping(target = "limit", source = "taskLimit")
    Column mapRequestPayloadToColumnDomain(ColumnRequestPayload payload);

    @Mapping(source = "colorCode", target = "color")
    @Mapping(source = "limit", target = "taskLimit")
    @Mapping(target = "tasks", ignore = true)
    ColumnResponsePayload mapColumnDomainToResponsePayload(Column column);

    @Mapping(target = "colorCode", source = "color")
    @Mapping(target = "limit", source = "taskLimit")
    @BeanMapping(nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE)
    Column updateColumnFromRequestPayload(@MappingTarget Column savedColumn, ColumnRequestPayload payload);
}
