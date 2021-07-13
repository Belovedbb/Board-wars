package com.board.wars.domain;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document
public class CumulativeFlowDiagram extends BaseEntity {
    @Indexed(unique = true)
    private long projectCode;
    //column1 -> date, no of task at date
    //column1 -> date, no of task at date
    //column2 -> date, no of task at date
    //column2 -> date, no of task at date
    //column2 -> date, no of task at date
    //...
    //column -> date, no of task at date
    private List<FlowDiagram> flows;

    public long getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(long projectCode) {
        this.projectCode = projectCode;
    }

    public List<FlowDiagram> getFlows() {
        return flows;
    }

    public void setFlows(List<FlowDiagram> flows) {
        this.flows = flows;
    }
}
