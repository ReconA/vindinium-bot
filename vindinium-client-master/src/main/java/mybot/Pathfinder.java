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

    private final Vertex[] mines;
    private final Vertex[] pubs;
    private final Hero me;
    private final AdvancedGameState gameState;

    private static final Logger logger = LogManager.getLogger(Pathfinder.class);

    public Pathfinder(AdvancedGameState gameState) {
        this.gameState = gameState;
        this.me = gameState.getMe();
        this.mines = new Vertex[this.gameState.getMines().size()];
        this.pubs = new Vertex[this.gameState.getPubs().size()];

        bfs(gameState.getBoardGraph().get(me.getPos()));
    }

    /**
     * Do a breadth first search on the current map. All vertices reachable from
     * the current vertex have a distance from current vertex, and a parent
     * vertex.
     * <p>
     * Also makes an array of all vertices with a pub or mine.
     *
     * @param start The root of the search.
     */
    private void bfs(Vertex start) {
        logger.info("Doing a breadth first search.");
        logger.info("Current position " + me.getPos());
        for (Vertex v : gameState.getBoardGraph().values()) {
            v.setDistance(Integer.MAX_VALUE);
            v.setParent(null);
        }

        Queue<Vertex> q = new ArrayDeque<>();
        start.setDistance(0);
        q.add(start);

        int m = 0;
        int p = 0;
        while (!q.isEmpty()) {
            Vertex v = q.poll();

            if (gameState.getMines().containsKey(v.getPosition())) {
                mines[m] = v;
                m++;
            } else if (gameState.getPubs().containsKey(v.getPosition())) {
                pubs[p] = v;
                p++;
            } else {
                for (Vertex adj : v.getAdjacentVertices()) {
                    if (adj.getDistance() == Integer.MAX_VALUE) {
                        adj.setDistance(v.getDistance() + 1);
                        adj.setParent(v);
                        q.add(adj);
                    }
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
     * Return closest mine not owned by me.
     *
     * @return Vertex of the closest mine. Null if no such mine is found.
     */
    public Vertex getClosestMine() {
        for (Vertex v : mines) {
            if (!isMyMine(gameState.getMines().get(v.getPosition()))) {
                return v;
            }
        }
        return null;
    }

    /**
     * Returns closest pub.
     *
     * @return
     */
    public Vertex getClosestPub() {
        return pubs[0];
    }

    public AdvancedGameState getGameState() {
        return gameState;
    }

    /**
     * Determine if I'm standing next to a pub.
     *
     * @return True is there is an adjacent pub. False otherwise.
     */
    public boolean standingAdjacentToPub() {
        return calcDistance(pubs[0].getPosition(), this.getCurrentPosition()) == 1;
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
        logger.info("Heading to the closest pub at " + pubs[0]);
        return this.moveTowards(pubs[0]);
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
        return pubs[0];
    }

    public Hero getClosestEnemy() {
        Hero closest = null;
        int minDist = Integer.MAX_VALUE;
        for (Hero h : gameState.getHeroesById().values()) {
            if (h.getId() != gameState.getMe().getId()) {
                Vertex v = gameState.getBoardGraph().get(h.getPos());
                if (v.getDistance() < minDist) {
                    minDist = v.getDistance();
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

}
