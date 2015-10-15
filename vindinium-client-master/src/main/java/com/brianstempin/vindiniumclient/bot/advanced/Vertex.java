package com.brianstempin.vindiniumclient.bot.advanced;

import com.brianstempin.vindiniumclient.dto.GameState;
import java.util.List;

/**
 * Represents some traversable tile on the board
 */
public class Vertex {

    private final GameState.Position position;
    private final List<Vertex> adjacentVertices;
    private Vertex parent;
    private int distance;

    public Vertex(GameState.Position position, List<Vertex> adjacentVertices) {
        this.position = position;
        this.adjacentVertices = adjacentVertices;
    }

    public GameState.Position getPosition() {
        return position;
    }

    public List<Vertex> getAdjacentVertices() {
        return adjacentVertices;
    }

    public Vertex getParent() {
        return parent;
    }

    public void setParent(Vertex parent) {
        this.parent = parent;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return this.position.toString();
    }

}
