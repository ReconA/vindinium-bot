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

    /**
     * A list of all mines.
     */
    private final List<Vertex> mines;
    /**
     * A list of all pubs.
     */
    private final List<Vertex> pubs;
    /**
     * The hero that I control.
     */
    private final Hero me;
    /**
     * Contains game data processed from servers JSON response.
     */
    private final AdvancedGameState gameState;

    private static final Logger logger = LogManager.getLogger(Pathfinder.class);

    /**
     * Create a new pathfinder from game state. Adds movement cost based on
     * enemy hero locations and spawn points and calculates distance to every
     * vertex using Dijkstra's algorithm.
     *
     * @param gameState Current game state.
     */
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
        resetInnAndMineCost();
        dijkstra();
        logger.info("Current position " + gameState.getMe().getPos());
    }

    /**
     * Dijkstra's algorithm calculates the distance to every vertex and makes a list of all mines and pubs. 
     */
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
            } else if (gameState.getPubs().containsKey(u.getPosition())) {
                pubs.add(u);
            } else {
                for (Vertex v : u.getAdjacentVertices()) {
                    int alt = u.getDistance() + v.getCost();
                    if (alt < v.getDistance()) {
                        v.setDistance(alt);
                        v.setParent(u);
                    }
                }
            }
        }
    }

    /**
     * Get the vertex with the smallest distance and remove it from the list.
     *
     * @param list A list of unprocessed vertices.
     * @return Vertex with the smallest distance.
     */
    private Vertex minDistVertex(List<Vertex> list) { //This is terrible in every way, but because Vindinium maps 
        Vertex min = null;                            // are so small, it does not matter performance wise. 
        int minDist = Integer.MAX_VALUE;              //Still, should be fixed one day. 

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
     * This is done with a bfs to a certain depth.
     */
    private void initVertexCosts(Hero h) {
        final int SPAWN_POINT_COST = 15;
        final int THREAT_COST = 8;
        final int THREAT_RADIUS = 4;
        final int UNPASSABLE = 1000;

        logger.info("Adding threats from hero at " + h.getPos());
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
     * @param a First position.
     * @param b Second position.
     * @return Manhattan distance between a and b.
     */
    public int calcDistance(Position a, Position b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    /**
     * Calculate Manhattan distance between vertices a and b.
     *
     * @param a First vertex.
     * @param b Second vertex.
     * @return Manhattan distance between a and b.
     */
    public int calcDistance(Vertex a, Vertex b) {
        return calcDistance(a.getPosition(), b.getPosition());
    }

    /**
     * Check if hero owns a mine.
     *
     * @param mine
     * @param hero
     * @return True if the hero owns the mine. False if mine has no owner or
     * some other owner.
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
     * @param v Vertex in which the mine is located.
     * @return True if the mine is owned by me, false otherwise.
     */
    public boolean isMyMine(Vertex v) {
        return heroOwns(gameState.getMines().get(v.getPosition()), me);
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

    /**
     * Count how many moves are required to reach goal vertex.
     *
     * @param goal Goal vertex
     * @return Amount of moves to reach goal.
     */
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
     * Count how many moves are required to reach goal position.
     *
     * @param goal Goal vertex
     * @return Amount of moves to reach goal. 
     */
    public int movesToReach(Position goal) {
        return movesToReach(positionToVertex(goal));
    }

    /**
     * Returns the position I'm standing in.
     *
     * @return Current position
     */
    public Position getCurrentPosition() {
        return me.getPos();
    }

    /**
     * Returns the vertex I'm standing in.
     *
     * @return
     */
    public Vertex getCurrentVertex() {
        return gameState.getBoardGraph().get(me.getPos());
    }

    /**
     * Returns closest pub.
     *
     * @return Vertex in which the closest pub is located.
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
        if (closest != null) {
            logger.info("Closest pub at " + closest + ", distance " + closest.getDistance());
        } else {
            logger.info("Closest pub not found.");
        }
        return closest;
    }

    /**
     * Get cloest mine that I don't own.
     *
     * @return Closest mine.
     */
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

    /**
     * Go towards the closest pub.
     *
     * @return A move towards the closest pub.
     */
    public BotMove goToClosestPub() {
        logger.info("Heading to the closest pub at " + getClosestPub());
        return this.moveTowards(getClosestPub());
    }

    /**
     * Go towards closest mine that I don't own.
     *
     * @return A move towards the closest mine.
     */
    public BotMove goToClosestMine() {
        Vertex closestMine = this.getClosestMine();
        logger.info("Heading to closest mine at " + closestMine);
        return this.moveTowards(closestMine);
    }

    /**
     * Returns a set of all vertices an enemy can deal damage to in the next
     * turn.
     *
     * @param enemy Enemy whose threatened vertices are returned.
     * @return Vertices in which the enemy can deal damage to.
     */
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
     * Get closest enemy hero. The distance is measured by how many moves it takes to reach target. 
     * @return Closest enemy hero. 
     */
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
                logger.info("Hero " + h.getName() + " is standing next to an inn at " + pub);
                return true;
            }
        }

        return false;
    }

    /**
     * Reset the distance and cost of all vertices.
     */
    private void resetVertices() {
        for (Vertex v : gameState.getBoardGraph().values()) {
            v.setDistance(0);
            v.setCost(1);
        }
    }

    /**
     * Returns the vertex at the given position.
     *
     * @param p Position of the requested vertex.
     * @return Vertex with the given position.
     */
    public Vertex positionToVertex(Position p) {
        return this.gameState.getBoardGraph().get(p);
    }

    /**
     * Set the movement cost of vertices that contain a mine or a pub to 1.
     */
    private void resetInnAndMineCost() {
        for (Vertex v : mines) {
            v.setCost(1);
        }

        for (Vertex v : pubs) {
            v.setCost(1);
        }
    }

}
