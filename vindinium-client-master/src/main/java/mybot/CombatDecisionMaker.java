package mybot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.advanced.*;
import com.brianstempin.vindiniumclient.dto.GameState.*;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 */
public class CombatDecisionMaker implements DecisionMaker {
    
    private static final Logger logger = LogManager.getLogger(CombatDecisionMaker.class);
    
    private final int DEALT_DMG_VALUE = 21;
    private final int RECEIVED_DMG_VALUE = -20;
    private final int INVALID_VERTEX = -999;
    
    private Pathfinder pathfinder;
    private AdvancedGameState gameState;
    private Hero me;
    private Hero closestEnemy;
    
    @Override
    public boolean wantsToAct(Pathfinder pathfinder) {
        this.gameState = pathfinder.getGameState();
        this.pathfinder = pathfinder;
        
        Hero closest = this.pathfinder.getClosestEnemy();
        Vertex closestPos = pathfinder.positionToVertex(closest.getPos());
        if (pathfinder.movesToReach(closestPos) < 2) {
            this.closestEnemy = closest;
            return true;
        }
        return false;
    }
    
    @Override
    public BotMove takeAction(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
        this.gameState = pathfinder.getGameState();
        this.me = this.gameState.getMe();
        
        if (pathfinder.standsAdjacentToInn(me) && pathfinder.standsAdjacentToInn(closestEnemy)) {
            if (me.getLife() > 90 ) {
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
        
        if (hasMoreHpThanMe(this.closestEnemy) || pathfinder.standsAdjacentToInn(this.closestEnemy)) {
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
     * @return Move towards closest pub. 
     */
    private BotMove flee() {
        return pathfinder.goToClosestPub();
    }
    
    /**
     * Check if hero survives more hits than me. 
     * @param h
     * @return 
     */
    private boolean hasMoreHpThanMe(Hero h) {
        int hitDmg = 20;
        return h.getLife() / hitDmg > me.getLife() / hitDmg;
    }

    /**
     * Assigns a value to a vertex. This value describes how good a move to this
     * vertex is. 
     * <p>
     * Dealing damage and going closer to an enemy increase value, and receiving damage reduces it. 
     *
     * @param v Vertex to be evaluated.
     * @return
     */
    private int evaluateVertex(Vertex v) {
        logger.info("Evaluating vertex " + v);
        int value = 0;
        Map<Position, Hero> heroes = this.gameState.getHeroesByPosition();
        
        Hero h = heroes.get(v.getPosition());
        if (h != null && h.getId() != me.getId()) {
            logger.info("Vertex is already occupied. Invalid vertex.");
            return INVALID_VERTEX;
        }
        
        for (Vertex adj : v.getAdjacentVertices()) {
            h = heroes.get(adj.getPosition());
            if (h != null) {
                value += DEALT_DMG_VALUE;
            }
            
        }
        
        for (Hero hero : gameState.getHeroesById().values()) {
            if (hero.getId() != me.getId()) {
                Set<Vertex> threatened = pathfinder.threatenedVertices(hero);
                if (threatened.contains(v)) {
                    value += RECEIVED_DMG_VALUE;
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
