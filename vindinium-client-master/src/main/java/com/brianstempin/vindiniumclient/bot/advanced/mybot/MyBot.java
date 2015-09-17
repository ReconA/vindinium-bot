package com.brianstempin.vindiniumclient.bot.advanced.mybot;

import com.brianstempin.vindiniumclient.bot.advanced.Mine;
import com.brianstempin.vindiniumclient.bot.advanced.Vertex;
import com.brianstempin.vindiniumclient.bot.advanced.AdvancedBot;
import com.brianstempin.vindiniumclient.bot.advanced.AdvancedGameState;
import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.advanced.Pub;
import com.brianstempin.vindiniumclient.dto.GameState.Hero;
import com.brianstempin.vindiniumclient.dto.GameState.Position;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyBot implements AdvancedBot {

    private AdvancedGameState gameState;
    private Hero me;
    private boolean isFirstTurn = true;

    //Currently not fully used, but can be useful for later purposes, like combat. 
    private List<Vertex> adjacentToMine = new ArrayList<>();
    private List<Vertex> adjacentToPub = new ArrayList<>();

    private Mine closestMine;
    private Vertex vertexClosestToMine;
    private Pub closestPub;
    private Vertex vertexClosestToPub;

    private final static Logger logger = LogManager.getLogger(MyBot.class);

    @Override
    public BotMove move(AdvancedGameState gameState) {
        this.gameState = gameState;
        this.me = gameState.getMe();

        logger.info("Current position " + me.getPos());
        bfs(gameState.getBoardGraph().get(getCurrentPosition()));

        Vertex goal = null;
        if (me.getLife() < 50) {
            logger.info("Low HP. Going to pub.");
            goal = goToClosestPub();
        } else {
            logger.info("Heading to a mine.");
            goal = goToClosestMine();
        }

        BotMove move = moveTowards(goal);
        logger.info("Move direction:" + move);
        return move;
    }

    /**
     * Move towards the goal vertex by tracing vertex parents until we arrive at
     * the vertex adjacent to current position.
     *
     * @param goal The goal vertex.
     * @return Which direction the next move will go to.
     */
    public BotMove moveTowards(Vertex goal) {
        BotMove move = BotMove.STAY;
        if (goal == null) {
            logger.info("Goal is null. Staying still.");
            return move;
        }

        logger.info("Moving towards " + goal.getPosition());
        logger.info("Current position " + getCurrentPosition());
        Position currentPos = getCurrentPosition();
        if (calcManhattanDistance(currentPos, goal.getPosition()) == 0) {
            logger.info("Goal is the current position. Staying still.");
            return move;
        }

        while (calcManhattanDistance(currentPos, goal.getPosition()) != 1) {
            goal = goal.getParent();
        }

        int goalX = goal.getPosition().getX();
        int goalY = goal.getPosition().getY();

        if (goalX - currentPos.getX() == 1) {
            move = BotMove.SOUTH;
        } else if (goalX - currentPos.getX() == -1) {
            move = BotMove.NORTH;
        } else if (goalY - currentPos.getY() == 1) {
            move = BotMove.EAST;
        } else if (goalY - currentPos.getY() == -1) {
            move = BotMove.WEST;
        }

        return move;
    }

    /**
     * Calculate Manhattan distance between positions a and b.
     *
     * @param a
     * @param b
     * @return Manhattan distance between a and b.
     */
    public int calcManhattanDistance(Position a, Position b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    /**
     * Do a breadth first search on the current map. All vertices reachable from
     * current vertex have a distance from current vertex and a parent vertex.
     * <p>
     * On the first turn makes a list of vertices with an adjacent pub or mine.
     *
     * @param start The root of the search.
     */
    public void bfs(Vertex start) {
        logger.info("Doing a breadth first search.");
        for (Vertex v : gameState.getBoardGraph().values()) {
            v.setDistance(Integer.MAX_VALUE);
            v.setParent(null);
        }

        Queue<Vertex> q = new ArrayDeque<>();
        start.setDistance(0);
        q.add(start);

        while (!q.isEmpty()) {
            Vertex v = q.poll();
            
            if (closestPub == null) {
                for (Pub p : gameState.getPubs().values()) {
                    if (calcManhattanDistance(p.getPosition(), v.getPosition()) == 1) {
                        closestPub = p;
                        vertexClosestToPub = v;
                        logger.info("Closest pub at position " + closestPub.getPosition());
                        logger.info("Vertex adjacent to it is " +  vertexClosestToPub.toString());
                    }
                }
            }
            
            if (closestMine == null) {
                for (Mine m : gameState.getMines().values()) {
                    if (calcManhattanDistance(m.getPosition(), v.getPosition()) == 1 && !isMyMine(m)) {
                        closestMine = m;
                        vertexClosestToPub = v;
                    }
                }
            }
            
            for (Vertex adj : v.getAdjacentVertices()) {
                if (adj.getDistance() == Integer.MAX_VALUE) {
                    adj.setDistance(v.getDistance() + 1);
                    adj.setParent(v);
                    q.add(adj);
                }
            }
        }
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

    private Vertex findClosestPub() {
        int minDistance = Integer.MAX_VALUE;
        Vertex closest = null;

        for (Vertex v : adjacentToPub) {
            if (v.getDistance() < minDistance) {
                closest = v;
            }
        }

        return closest;
    }

    /**
     * Check if any pub is adjacent to this vertex. If there is an adjacent pub,
     * add the vertex to the list of vertices with adjacent pub, and add the pub
     * to the vertices adjacent pubs list.
     *
     * @param v Vertex to be checked.
     */
    private void checkIfAdjacentToPub(Vertex v) {
        for (Pub p : gameState.getPubs().values()) {
            if (calcManhattanDistance(p.getPosition(), v.getPosition()) == 1) {
                adjacentToPub.add(v);
                v.addAdjacentPub(p);
            }
        }
    }

    /**
     * Check if any mine is adjacent to this vertex. If there is an adjacent
     * mine, add the vertex to the list of vertices with adjacent mines, and add
     * the mine to the vertices adjacent mines list.
     *
     * @param v Vertex to be checked.
     */
    private void checkIfAdjacentToMine(Vertex v) {
        for (Mine m : gameState.getMines().values()) {
            if (calcManhattanDistance(m.getPosition(), v.getPosition()) == 1) {
                adjacentToMine.add(v);
                v.addAdjacentMine(m);
            }
        }
    }

    /**
     * Goes towards the closest pub.
     *
     * @return Vertex adjacent to a pub. If already in such a vertex, returns
     * the pub vertex.
     */
    private Vertex goToClosestPub() {
        Vertex currentVertex = gameState.getBoardGraph().get(getCurrentPosition());
        Vertex v = null;

        if (currentVertex.equals(vertexClosestToPub)) {
            v = gameState.getBoardGraph().get(closestPub.getPosition());
        } else {
            v = vertexClosestToPub;
        }

        return v;
    }

    /**
     * Goes towards the closest mine.
     *
     * @return Vertex adjacent to a mine. If already in such a vertex, returns
     * the mine vertex.
     */
    private Vertex goToClosestMine() {
        Vertex currentVertex = gameState.getBoardGraph().get(getCurrentPosition());
        Vertex v = null;
        
        if (currentVertex.equals(vertexClosestToMine)) {
            logger.info("Standing next to a mine.");
            v = gameState.getBoardGraph().get(closestMine.getPosition());
        } else {
            logger.info("Heading to the closest vertex next to a mine.");
            v = vertexClosestToMine;
        }

        return v;
    }

    private Vertex findClosestMine() {
        int minDistance = Integer.MAX_VALUE;
        Vertex closest = null;

        for (Vertex v : this.adjacentToMine) {
            for (Mine m : v.getAdjacentMines()) {
                if (v.getDistance() < minDistance
                        && !isMyMine(m)) {
                    closest = v;
                }
            }
        }

        return closest;
    }

    /**
     * Check if a mine is owned by me.
     *
     * @param mine Vertex in which the mine is located.
     * @return True if the mine is owned by me, false otherwise.
     */
    private boolean isMyMine(Mine mine) {
        if (mine.getOwner() == null) {
            return false;
        }

        return mine.getOwner().getId() == me.getId();
    }

}
