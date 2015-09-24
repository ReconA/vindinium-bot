package mybot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.advanced.AdvancedGameState;
import com.brianstempin.vindiniumclient.bot.advanced.Vertex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Decides if we need healing. 
 */
public class HealingDecisionMaker implements DecisionMaker {

    private Pathfinder pathfinder;
    private AdvancedGameState gameState;
    
    private final static Logger logger = LogManager.getLogger(HealingDecisionMaker.class);

    /**
     * Heal if health is less than 50, or standing next to a pub and health is less than 80.
     * @param pathfinder
     * @return 
     */
    @Override
    public boolean wantsToAct(Pathfinder pathfinder) {
        logger.info("Healing Decision Maker thinks...");
        this.pathfinder = pathfinder;
        this.gameState = this.pathfinder.getGameState();
        return (gameState.getMe().getLife() < 50
                || (gameState.getMe().getLife() < 80 && pathfinder.standingAdjacentToPub()));

    }

    /**
     * Simply go to the closest pub.
     * @return A move towards the closest pub. 
     */
    @Override
    public BotMove takeAction() {
        Vertex goal = this.pathfinder.goToClosestPub();
        return pathfinder.moveTowards(goal);
    }

    @Override
    public String getType() {
        return "Healing Decision Maker";
    }

}
