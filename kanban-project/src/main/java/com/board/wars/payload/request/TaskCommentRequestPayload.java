package com.board.wars.payload.request;

import com.board.wars.payload.TeamUserPayload;

import java.time.LocalDateTime;
import java.util.List;

public class TaskCommentRequestPayload {
    private String comment;
    private TeamUserPayload teamUser;
    private LocalDateTime timeCreated;
    private List<FileAttachmentRequestPayload> attachments;

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

    public List<FileAttachmentRequestPayload> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<FileAttachmentRequestPayload> attachments) {
        this.attachments = attachments;
    }
}
