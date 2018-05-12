package com.mycompany.john.pickaplace.eventBusModels;

import com.fasterxml.jackson.databind.JsonNode;

public class DeleteBroadcastedLiveLocationRecordEvent {
    private JsonNode node;

    public DeleteBroadcastedLiveLocationRecordEvent(JsonNode node) {
        this.node = node;
    }

    public JsonNode getNode() {
        return node;
    }

    public void setNode(JsonNode node) {
        this.node = node;
    }
}
