package com.board.wars.mapper;

import com.board.wars.domain.SubTask;
import com.board.wars.payload.request.SubTaskRequestPayload;
import com.board.wars.payload.response.SubTaskResponsePayload;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper
public interface SubTaskMapper {

    SubTask mapRequestPayloadToSubTaskDomain(SubTaskRequestPayload payload);

    SubTaskResponsePayload mapSubTaskDomainToResponsePayload(SubTask subTask);

    @BeanMapping(nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE)
    SubTask updateSubTaskFromRequestPayload(@MappingTarget SubTask savedSubTask, SubTaskRequestPayload payload);
}
