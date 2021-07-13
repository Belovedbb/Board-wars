package com.board.wars.mapper;

import com.board.wars.domain.Task;
import com.board.wars.payload.request.TaskRequestPayload;
import com.board.wars.payload.response.TaskResponsePayload;
import org.mapstruct.*;

@Mapper(uses = {SubTaskMapper.class, TaskCommentMapper.class})
public interface TaskMapper {

    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "subTasks", ignore = true)
    @Mapping(target = "tags", source = "tags.values")
    @Mapping(target = "assignee.identity", source = "assignee.teamUser.id")
    @Mapping(target = "assignee.type", source = "assignee.teamUser.type")
    @Mapping(target = "reporter.identity", source = "reporter.teamUser.id")
    @Mapping(target = "reporter.type", source = "reporter.teamUser.type")
    TaskResponsePayload mapTaskDomainToResponsePayload(Task task);

    @Mapping(target = "assignee.teamUser.id", source = "assignee.identity")
    @Mapping(target = "assignee.teamUser.type", source = "assignee.type")
    @Mapping(target = "tags.values", source = "tags")
    Task mapRequestPayloadToTaskDomain(TaskRequestPayload payload);

    @Mapping(target = "tags.values", source = "tags")
    @Mapping(target = "assignee.teamUser.id", source = "assignee.identity")
    @Mapping(target = "assignee.teamUser.type", source = "assignee.type")
    @BeanMapping(nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE)
    Task updateTaskFromRequestPayload(@MappingTarget Task savedTask, TaskRequestPayload payload);
}
