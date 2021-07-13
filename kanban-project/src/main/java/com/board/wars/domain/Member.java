package com.board.wars.domain;

public class Member {

    private TeamUser teamUser;

    public boolean isTeam() {
        return this.teamUser != null && teamUser.getType() == TeamUser.MemberType.TEAM;
    }

    public boolean isPrivate() {
        return this.teamUser != null && teamUser.getType() == TeamUser.MemberType.USER;
    }

    public TeamUser getTeamUser() {
        return teamUser;
    }

    public void setTeamUser(TeamUser teamUser) {
        this.teamUser = teamUser;
    }
}
