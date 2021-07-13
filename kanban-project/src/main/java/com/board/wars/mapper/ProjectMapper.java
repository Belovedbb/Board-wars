package com.board.wars.mapper;

import com.board.wars.domain.Project;
import com.board.wars.payload.request.ProjectRequestPayload;
import com.board.wars.payload.response.ProjectResponsePayload;
import org.mapstruct.*;

import java.time.LocalDateTime;

@Mapper(uses = {ColumnMapper.class, TaskMapper.class})
public interface ProjectMapper {

    @Mapping(target = "tags.values", source = "tags")
    @Mapping(target = "status",  defaultExpression = "java(Status.ACTIVE)")
    @Mapping(target = "member.teamUser.id", source = "teamUser.identity")
    @Mapping(target = "member.teamUser.type", source = "teamUser.type")
    Project mapRequestPayloadToProjectDomain(ProjectRequestPayload payload);

    @AfterMapping
    default void afterMapping(Project project) {
        project.setStartPeriod(project.getStartPeriod() == null ? LocalDateTime.now() : project.getStartPeriod());
    }

    @Mapping(target = "columns", ignore = true)
    @Mapping(target = "teamUser.identity", source = "member.teamUser.id")
    @Mapping(target = "teamUser.type", source = "member.teamUser.type")
    @Mapping(target = "tags", source = "tags.values")
    ProjectResponsePayload mapProjectDomainToResponsePayload(Project project);

    @Mapping(target = "tags.values", source = "tags")
    @Mapping(target = "member.teamUser.id", source = "teamUser.identity")
    @Mapping(target = "member.teamUser.type", source = "teamUser.type")
    @BeanMapping(nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE)
    Project updateProjectFromRequestPayload(@MappingTarget Project savedProject, ProjectRequestPayload payload);
}
