package com.board.wars.domain.marker.parts;

public class MarkerPart {

    private String partId;

    private boolean complete;

    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getMarkerType() {
        return "empty";
    }

}
