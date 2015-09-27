package mybot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.advanced.AdvancedGameState;
import com.brianstempin.vindiniumclient.bot.advanced.Mine;
import com.brianstempin.vindiniumclient.bot.advanced.Vertex;
import com.brianstempin.vindiniumclient.dto.GameState.*;
import java.util.ArrayDeque;
import java.util.Queue;
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
     * current vertex have a distance from current vertex and a parent vertex.
     * <p>
     * On the first turn makes a list of vertices with an adjacent pub or mine.
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
        return gameState.getMe().getPos();
    }

    /**
     * Return closest mine not owned by me.
     *
     * @return Vertex of the closest mine. Null if no such mine is found.
     */
    private Vertex getClosestMine() {
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
        logger.info("Heading to the closest pub.");
        return this.moveTowards(pubs[0]);
    }

    public BotMove goToClosestMine() {
        Vertex closestMine = this.getClosestMine();
        logger.info("Heading to closest mine at " + closestMine);
        return this.moveTowards(closestMine);
    }

}
