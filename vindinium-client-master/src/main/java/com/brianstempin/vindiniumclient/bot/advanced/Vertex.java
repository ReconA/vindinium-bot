package com.brianstempin.vindiniumclient.bot.advanced;

import com.brianstempin.vindiniumclient.dto.GameState;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents some traversable tile on the board
 */
public class Vertex {

    private final GameState.Position position;
    private final List<Vertex> adjacentVertices;
    private Vertex parent;
    private int distance;
    private List<Mine> adjacentMines;
    private List<Pub> adjacentPubs;

    public Vertex(GameState.Position position, List<Vertex> adjacentVertices) {
        this.position = position;
        this.adjacentVertices = adjacentVertices;
        this.adjacentMines = new ArrayList<>();
        this.adjacentPubs = new ArrayList<>();
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
    
    public void addAdjacentMine(Mine m) {
        this.adjacentMines.add(m);
    }
    
    public void addAdjacentPub(Pub p) {
        this.adjacentPubs.add(p);
    }

    public List<Mine> getAdjacentMines() {
        return adjacentMines;
    }

    public List<Pub> getAdjacentPubs() {
        return adjacentPubs;
    }
    
    public boolean hasAdjacentMines() {
        return !adjacentMines.isEmpty();
    }
    
    public boolean hasAdjacentPubs() {
        return !adjacentPubs.isEmpty();
    }
    
    @Override
    public String toString() {
        return this.position.toString();
    }

}
