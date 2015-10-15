package mybot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.advanced.AdvancedGameState;
import com.brianstempin.vindiniumclient.bot.advanced.Mine;
import com.brianstempin.vindiniumclient.bot.advanced.Vertex;
import com.brianstempin.vindiniumclient.dto.GameState.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Processes an advanced game state to find whatever needs to be found.
 */
public class Pathfinder {

    private final List<Vertex> mines;
    private final List<Vertex> pubs;
    private final Hero me;
    private final AdvancedGameState gameState;

    private final int SPAWN_POINT_COST = 15;
    private final int THREAT_COST = 8;
    private final int THREAT_RADIUS = 4;
    private final int UNPASSABLE = 1000;

    private static final Logger logger = LogManager.getLogger(Pathfinder.class);

    public Pathfinder(AdvancedGameState gameState) {
        this.gameState = gameState;
        this.me = gameState.getMe();
        this.mines = new ArrayList<>();
        this.pubs = new ArrayList<>();

        resetVertices();
        for (Hero h : gameState.getHeroesById().values()) {
            if (h.getId() != me.getId()) {
                initVertexCosts(h);
            }
        }
        dijkstra();
        logger.info("Current position " + gameState.getMe().getPos());
    }

    private void dijkstra() {
        List<Vertex> q = new ArrayList<>();
        Vertex source = gameState.getBoardGraph().get(me.getPos());

        for (Vertex v : gameState.getBoardGraph().values()) {
            if (v.getPosition().equals(source.getPosition())) {
                v.setDistance(0);
            } else {
                v.setDistance(Integer.MAX_VALUE);
            }
            v.setParent(null);
            q.add(v);
        }

        while (!q.isEmpty()) {
            Vertex u = minDistVertex(q);
            if (gameState.getMines().containsKey(u.getPosition())) {
                mines.add(u);
            }
            if (gameState.getPubs().containsKey(u.getPosition())) {
                pubs.add(u);
            }

            for (Vertex v : u.getAdjacentVertices()) {
                int alt = u.getDistance() + v.getCost();
                if (alt < v.getDistance()) {
                    v.setDistance(alt);
                    v.setParent(u);
                }
            }

        }
    }

    private Vertex minDistVertex(List<Vertex> list) {
        Vertex min = null;
        int minDist = Integer.MAX_VALUE;

        for (Vertex v : list) {
            if (v.getDistance() < minDist) {
                min = v;
                minDist = v.getDistance();
            }
        }
        list.remove(min);

        return min;
    }

    /**
     * Give vertices a movement cost based on enemy locations and spawn points.
     */
    private void initVertexCosts(Hero h) {
        gameState.getBoardGraph().get(h.getPos()).addCost(SPAWN_POINT_COST);

        Vertex enemyPos = gameState.getBoardGraph().get(h.getPos());
        enemyPos.addCost(UNPASSABLE);

        Queue<Vertex> q = new ArrayDeque<>();
        Set<Vertex> visited = new HashSet<>();
        visited.add(enemyPos);
        q.add(enemyPos);

        int currentDepth = 0;
        int elementsToDepthIncrease = 1;
        int nextElementsToDepthIncrease = 0;

        while (!q.isEmpty()) {
            Vertex current = q.poll();
            current.addCost(THREAT_COST - currentDepth);
            nextElementsToDepthIncrease += current.getAdjacentVertices().size();
            if (--elementsToDepthIncrease == 0) {
                if (++currentDepth > THREAT_RADIUS) {
                    return;
                }
                elementsToDepthIncrease = nextElementsToDepthIncrease;
                nextElementsToDepthIncrease = 0;
            }

            for (Vertex adj : current.getAdjacentVertices()) {
                if (!visited.contains(adj)) {
                    q.add(adj);
                    visited.add(adj);
                }

            }
        }

    }

    /**
     * Calculate Manhattan distance between positions a and b.
     *
     * @param a
     * @param b
     * @return Manhattan distance between a and b.
     */
    public int calcDistance(Position a, Position b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    public int calcDistance(Vertex a, Vertex b) {
        return calcDistance(a.getPosition(), b.getPosition());
    }

    /**
     * Check if hero owns a mine.
     *
     * @param mine
     * @param hero
     * @return
     */
    public boolean heroOwns(Mine mine, Hero hero) {
        if (mine.getOwner() == null) {
            return false;
        }
        return mine.getOwner().getId() == hero.getId();
    }

    /**
     * Check if a mine is owned by me.
     *
     * @param mine Vertex in which the mine is located.
     * @return True if the mine is owned by me, false otherwise.
     */
    public boolean isMyMine(Mine mine) {
        return this.heroOwns(mine, me);
    }

    public boolean isMyMine(Vertex v) {
        return isMyMine(gameState.getMines().get(v.getPosition()));
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

        Position currentPos = getCurrentPosition();
        if (calcDistance(currentPos, goal.getPosition()) == 0) {
            logger.info("Goal is the current position. Staying still.");
            return move;
        }

        while (calcDistance(currentPos, goal.getPosition()) != 1) {
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

        logger.info("Move direction:" + move);
        return move;
    }

    public int movesToReach(Vertex goal) {
        if (goal == null) {
            return 0;
        }
        int moves = 0;
        while (calcDistance(getCurrentPosition(), goal.getPosition()) != 1) {
            goal = goal.getParent();
            moves++;
        }

        return moves;
    }

    /**
     * Returns current position.
     *
     * @return Current position
     */
    public Position getCurrentPosition() {
        return me.getPos();
    }

    public Vertex getCurrentVertex() {
        return gameState.getBoardGraph().get(me.getPos());
    }

    /**
     * Returns closest pub.
     *
     * @return
     */
    public Vertex getClosestPub() {
        Vertex closest = null;
        int minDist = Integer.MAX_VALUE;
        for (Vertex v : pubs) {
            if (v.getDistance() < minDist) {
                closest = v;
                minDist = v.getDistance();
            }
        }
        return closest;
    }

    public Vertex getClosestMine() {
        Vertex closest = null;
        int minDist = Integer.MAX_VALUE;
        for (Vertex v : mines) {
            if (!isMyMine(v)) {
                if (v.getDistance() < minDist) {
                    closest = v;
                    minDist = v.getDistance();
                }
            }
        }
        return closest;
    }

    public AdvancedGameState getGameState() {
        return gameState;
    }

    /**
     * Determine if I'm standing next to an enemy mine.
     *
     * @return True is there is an adjacent enemy mine. False otherwise.
     */
    public boolean standingAdjacentToMine() {
        return calcDistance(this.getClosestMine().getPosition(), this.getCurrentPosition()) == 1;
    }

    public BotMove goToClosestPub() {
        logger.info("Heading to the closest pub at " + getClosestPub());
        return this.moveTowards(getClosestPub());
    }

    public BotMove goToClosestMine() {
        Vertex closestMine = this.getClosestMine();
        logger.info("Heading to closest mine at " + closestMine);
        return this.moveTowards(closestMine);
    }

    public Set threatenedVertices(Hero enemy) {
        Set<Vertex> threatened = new HashSet<>();
        Vertex enemyPos = this.gameState.getBoardGraph().get(enemy.getPos());
        threatened.add(enemyPos);

        for (Vertex v : enemyPos.getAdjacentVertices()) {
            threatened.add(v);
            for (Vertex adj : v.getAdjacentVertices()) {
                threatened.add(adj);
            }
        }

        return threatened;
    }

    /**
     * Tries to find a safe path to a pub. If no safe path exists, tries to go
     * the closest pub.
     *
     * @return
     */
    public Vertex findSafePub() {
        for (Vertex pub : this.pubs) {
            Vertex pathNode = pub;
            while (true) {
                if (gameState.getHeroesByPosition().containsKey(pathNode.getPosition())) {
                    break;
                } else if (calcDistance(this.getCurrentPosition(), pathNode.getPosition()) != 1) {
                    return pub;
                }
                pathNode = pathNode.getParent();
            }
        }
        return pubs.get(0);
    }

    public Hero getClosestEnemy() {
        Hero closest = null;
        int minDist = Integer.MAX_VALUE;
        for (Hero h : gameState.getHeroesById().values()) {
            if (h.getId() != gameState.getMe().getId()) {
                Vertex v = gameState.getBoardGraph().get(h.getPos());
                if (movesToReach(v) < minDist) {
                    minDist = movesToReach(v);
                    closest = h;
                }
            }
        }
        return closest;
    }

    /**
     * Check if a hero is standing next to a pub.
     *
     * @param h
     * @return True is hero is standing next to a pub.
     */
    public boolean standsAdjacentToInn(Hero h) {
        Position heroPos = h.getPos();
        for (Vertex pub : this.pubs) {
            if (calcDistance(pub.getPosition(), heroPos) == 1) {
                return true;
            }
        }

        return false;
    }

    private void resetVertices() {
        for (Vertex v : gameState.getBoardGraph().values()) {
            v.setDistance(0);
            v.setCost(0);
        }
    }
    
    public Vertex positionToVertex(Position p) {
        return this.gameState.getBoardGraph().get(p);
    }

}
