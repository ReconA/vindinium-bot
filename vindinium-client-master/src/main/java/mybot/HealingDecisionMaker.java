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

    private final static Logger logger = LogManager.getLogger(HealingDecisionMaker.class);

    private final int healthThreshold = 50;
    private final int almostFull = 90;

    /**
     * Heal if health is less than 50, or standing next to a pub and health is
     * less than 80.
     *
     * @param pathfinder
     * @return True if we want to heal, false otherwise.
     */
    @Override
    public boolean wantsToAct(Pathfinder pathfinder) {
        AdvancedGameState gameState = pathfinder.getGameState();

        int myHealth = gameState.getMe().getLife();
        if (myHealth < almostFull && pathfinder.standingAdjacentToPub()) {
            logger.info("Healing to full HP.");
            return true;
        } else if (myHealth < this.healthThreshold) {
            logger.info("Low HP. Healing.");
            return false;
        } else {
            logger.info("HP at an acceptable level");
            return false;
        }
    }

    /**
     * Simply go to the closest pub.
     *
     * @return A move towards the closest pub.
     */
    @Override
    public BotMove takeAction(Pathfinder pathfinder) {
        Vertex goal = pathfinder.getClosestPub();
        return pathfinder.moveTowards(goal);
    }

    @Override
    public String getName() {
        return "Healing Decision Maker";
    }

}
