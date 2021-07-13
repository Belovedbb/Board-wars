package com.board.wars.payload.response;

import java.util.List;

public class CumulativeFlowDiagramResponsePayload {
    private long projectCode;
    private List<FlowDiagramResponsePayload> flows;

    public long getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(long projectCode) {
        this.projectCode = projectCode;
    }

    public List<FlowDiagramResponsePayload> getFlows() {
        return flows;
    }

    public void setFlows(List<FlowDiagramResponsePayload> flows) {
        this.flows = flows;
    }
}
