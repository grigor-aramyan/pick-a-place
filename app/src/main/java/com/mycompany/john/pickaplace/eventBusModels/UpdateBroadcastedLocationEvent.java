package com.mycompany.john.pickaplace.eventBusModels;

import com.fasterxml.jackson.databind.JsonNode;

public class UpdateBroadcastedLocationEvent {
    private JsonNode node;

    public UpdateBroadcastedLocationEvent(JsonNode node) {
        this.node = node;
    }

    public JsonNode getNode() {
        return node;
    }

    public void setNode(JsonNode node) {
        this.node = node;
    }
}
