package com.board.wars.mapper;

import com.board.wars.domain.TaskComment;
import com.board.wars.payload.request.TaskCommentRequestPayload;
import com.board.wars.payload.response.TaskCommentResponsePayload;
import org.mapstruct.*;

@Mapper(uses = {FileAttachmentMapper.class})
public interface TaskCommentMapper {

    @Mapping(target = "member.teamUser.id", source = "teamUser.identity")
    @Mapping(target = "member.teamUser.type", source = "teamUser.type")
    TaskComment mapRequestPayloadToTaskCommentDomain(TaskCommentRequestPayload payload);

    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "teamUser.identity", source = "member.teamUser.id")
    @Mapping(target = "teamUser.type", source = "member.teamUser.type")
    TaskCommentResponsePayload mapTaskCommentDomainToResponsePayload(TaskComment taskComment);

    @Mapping(target = "member.teamUser.id", source = "teamUser.identity")
    @Mapping(target = "member.teamUser.type", source = "teamUser.type")
    @BeanMapping(nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE)
    TaskComment updateTaskCommentFromRequestPayload(@MappingTarget TaskComment savedTaskComment, TaskCommentRequestPayload payload);
}
