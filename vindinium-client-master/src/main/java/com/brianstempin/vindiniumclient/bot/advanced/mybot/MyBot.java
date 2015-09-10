package com.brianstempin.vindiniumclient.bot.advanced.mybot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.advanced.*;
import com.brianstempin.vindiniumclient.dto.GameState.Position;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class MyBot implements AdvancedBot {

    private AdvancedGameState gameState;

    @Override
    public BotMove move(AdvancedGameState gameState) {
        this.gameState = gameState;
        return null;
    }

    /**
     * A* search for a path to target vertex.
     *
     * @param start Start vertex.
     * @param goal Goal vertex.
     * @return Returns an array that contains the path. The goal is the last
     * element. Returns null if a path does not exist.
     */
    public Vertex[] searchPath(Vertex start, Vertex goal) {
        AStarResult current = new AStarResult(start, goal, null, 0);

        Set<Vertex> visited = new HashSet<>();
        PriorityQueue<AStarResult> q = new PriorityQueue<>();
        visited.add(current.getVertex());
        q.add(current);

        while (!q.isEmpty()) {
            current = q.poll();
            if (current.getVertex().getPosition() == goal.getPosition()) {
                Vertex v = current.getVertex();
                int pathLen = current.getTravelled();
                int i = pathLen;
                Vertex[] path = new Vertex[pathLen];

                while (current.getPrevious() != start) {
                    path[i] = v;
                    v = current.getPrevious();
                    i--;
                }
                return path;
            }

            for (Vertex v : current.getVertex().getAdjacentVertices()) {
                AStarResult asr = new AStarResult(v, goal, current.getVertex(), current.getTravelled() + 1);
                if (!visited.contains(v)) {
                    q.add(asr);
                    visited.add(v);
                }
            }
        }

        return null;
    }

    private Position getCurrentPosition() {
        return gameState.getMe().getPos();
    }

    @Override
    public void setup() {
    }

    @Override
    public void shutdown() {

    }

    /*
     Stores A* search results. 
     */
    public static class AStarResult implements Comparable<AStarResult> {

        /*
         Prediction of the cost of the path that goes through this vertex. 
         */
        private final int predictedCost;
        /*
         The current vertex. 
         */
        private final Vertex vertex;
        /*
         How many moves are used to arrive to this vertex.
         */
        private final int travelled;
        /*
         The vertex from which we arrived to this vertex.
         */
        private final Vertex previous;

        public AStarResult(Vertex position, Vertex goal, Vertex previous, int distTravelled) {
            this.vertex = position;
            this.travelled = distTravelled;
            this.predictedCost = this.travelled + heuristic(goal);
            this.previous = previous;
        }

        /*
         Heurustic for comparing vertices. Currently uses Manhattan distance as 
         diagonal movement is not possible. 
         */
        private int heuristic(Vertex goal) {
            int dx = Math.abs(goal.getPosition().getX() - vertex.getPosition().getX());
            int dy = Math.abs(goal.getPosition().getY() - vertex.getPosition().getY());
            return dx + dy;
        }

        public Vertex getVertex() {
            return vertex;
        }

        public int getTravelled() {
            return travelled;
        }

        public Vertex getPrevious() {
            return previous;
        }

        @Override
        public int compareTo(AStarResult asr) {
            return this.predictedCost - asr.predictedCost;
        }

    }

}
