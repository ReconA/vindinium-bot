package mybot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.advanced.*;
import com.brianstempin.vindiniumclient.dto.GameState.*;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Decides whether we want to go to combat mode and which moves to make in combat. 
 */
public class CombatDecisionMaker implements DecisionMaker {

    private static final Logger logger = LogManager.getLogger(CombatDecisionMaker.class);

    /**
     * Contains map data.
     */
    private Pathfinder pathfinder;
    /**
     * Contains information about the state of the game.
     */
    private AdvancedGameState gameState;
    /**
     * The hero that I control.
     */
    private Hero me;
    /**
     * Closest enemy hero.
     */
    private Hero closestEnemy;

    /**
     * Act if there is an enemy nearby.
     *
     * @param pathfinder Contains current map data.
     * @return True if there is an nearby enemy.
     */
    @Override
    public boolean wantsToAct(Pathfinder pathfinder) {
        this.gameState = pathfinder.getGameState();
        this.pathfinder = pathfinder;

        int combatRadius = 2;

        Hero closest = this.pathfinder.getClosestEnemy();
        Vertex closestPos = pathfinder.positionToVertex(closest.getPos());
        if (pathfinder.movesToReach(closestPos) < combatRadius) {
            this.closestEnemy = closest;
            return true;
        }
        return false;
    }

    /**
     * Decides which move to make in combat. 
     * <p>
     * If both me and the enemy are next to a pub, heal and disengage towards the closest mine to avoid a loop.
     * <p>
     * If only I am next to a pub, stay still.
     * <p>
     * If only enemy is next to pub or has more HP than me, flee towards a pub.
     * <p>
     * Else evaluate all possible moves and make the best one. 
     * 
     * @param pathfinder Contains current map data.
     * @return A move to the best direction. 
     */
    @Override
    public BotMove takeAction(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
        this.gameState = pathfinder.getGameState();
        this.me = this.gameState.getMe();

        if (pathfinder.standsAdjacentToInn(me) && pathfinder.standsAdjacentToInn(closestEnemy)) {
            if (me.getLife() > 90) {
                return pathfinder.goToClosestMine();
            } else {
                return pathfinder.goToClosestPub();
            }
        }

        if (pathfinder.calcDistance(me.getPos(), pathfinder.getClosestPub().getPosition()) == 1) {
            if (me.getLife() < 50) {
                return pathfinder.moveTowards(pathfinder.getClosestPub());
            } else {
                return BotMove.STAY;
            }
        }

        if ((hasMoreHpThanMe(this.closestEnemy) && pathfinder.movesToReach(this.closestEnemy.getPos()) >= 2) 
                || pathfinder.standsAdjacentToInn(this.closestEnemy)) {
            return flee();
        }

        Vertex best = pathfinder.getCurrentVertex();
        int bestVal = evaluateVertex(best);

        for (Vertex adj : best.getAdjacentVertices()) {
            int val = evaluateVertex(adj);
            if (val > bestVal) {
                best = adj;
                bestVal = val;
            }
        }

        return pathfinder.moveTowards(best);
    }

    /**
     * Try to go to a pub.
     *
     * @return Move towards closest pub.
     */
    private BotMove flee() {
        return pathfinder.goToClosestPub();
    }

    /**
     * Check if hero survives more hits than me.
     *
     * @param h Hero whose HP is checked.
     * @return True if the hero survives more hits than me.
     */
    private boolean hasMoreHpThanMe(Hero h) {
        int hitDmg = 20;
        return h.getLife() / hitDmg > me.getLife() / hitDmg;
    }

    /**
     * Assigns a value to a vertex. This value describes how good a move to this
     * vertex is.
     * <p>
     * Dealing damage and going closer to an enemy increase value, and receiving
     * damage reduces it.
     *
     * @param v Vertex to be evaluated.
     * @return
     */
    private int evaluateVertex(Vertex v) {
        logger.info("Evaluating vertex " + v);
        int value = 0;
        Map<Position, Hero> heroes = this.gameState.getHeroesByPosition();

        int invalidVertex = -999;
        int dealtDmgValue = 21;
        int receivedDmgValue = -20;
        
        Hero h = heroes.get(v.getPosition());
        if (h != null && h.getId() != me.getId()) {
            logger.info("Vertex is already occupied. Invalid vertex.");
            return invalidVertex;
        }

        for (Vertex adj : v.getAdjacentVertices()) {
            h = heroes.get(adj.getPosition());
            if (h != null) {
                value += dealtDmgValue;
            }

        }

        for (Hero hero : gameState.getHeroesById().values()) {
            if (hero.getId() != me.getId()) {
                Set<Vertex> threatened = pathfinder.threatenedVertices(hero);
                if (threatened.contains(v)) {
                    value += receivedDmgValue;
                }
            }
        }
        Position enemyPos = this.closestEnemy.getPos();
        if (pathfinder.calcDistance(v.getPosition(), enemyPos) < pathfinder.calcDistance(pathfinder.getCurrentPosition(), enemyPos)) {
            value++; //If two vertices are otherwise equal, go towards closest enemy. 
        }

        logger.info("Value of v=" + value);

        return value;

    }

    @Override
    public String getName() {
        return "Combat Decision Maker";
    }

}
