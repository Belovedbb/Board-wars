package com.board.wars.payload.response;

import com.board.wars.payload.TeamUserPayload;
import com.board.wars.payload.request.FileAttachmentRequestPayload;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import java.time.LocalDateTime;

public class TaskCommentResponsePayload {
    private Long code;
    private String comment;
    private TeamUserPayload teamUser;
    private LocalDateTime timeCreated;
    private CollectionModel<EntityModel<FileAttachmentRequestPayload>> attachments;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public TeamUserPayload getTeamUser() {
        return teamUser;
    }

    public void setTeamUser(TeamUserPayload teamUser) {
        this.teamUser = teamUser;
    }

    public LocalDateTime getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(LocalDateTime timeCreated) {
        this.timeCreated = timeCreated;
    }

    public CollectionModel<EntityModel<FileAttachmentRequestPayload>> getAttachments() {
        return attachments;
    }

    public void setAttachments(CollectionModel<EntityModel<FileAttachmentRequestPayload>> attachments) {
        this.attachments = attachments;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }
}
