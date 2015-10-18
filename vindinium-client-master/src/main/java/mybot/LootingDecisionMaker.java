package mybot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.advanced.Vertex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Decides whether to loot a mine and which mine to loot.
 */
public class LootingDecisionMaker implements DecisionMaker {

    private static final Logger logger = LogManager.getLogger(LootingDecisionMaker.class);

    /**
     * This is the last in the decision tree, so it will always want to act. 
     * @param pathfinder Contains map data. 
     * @return Always true.
     */
    @Override
    public boolean wantsToAct(Pathfinder pathfinder) {
        logger.info("Looting a mine.");
        return true; 
    }

    /**
     * Go to the closest mine. If health would drop below healing threshold, go to the closest pub. 
     * @param pathfinder Contains map data.
     * @return A move towards the goal. 
     */
    @Override
    public BotMove takeAction(Pathfinder pathfinder) {
        Vertex goal = pathfinder.getClosestMine();
        if (goal == null) {
            return pathfinder.moveTowards(pathfinder.getClosestPub());
        }
        logger.info("Going to mine at " + goal + ", distance " + goal.getDistance());

        int healThreshold = 50;
        int myHealth = pathfinder.getGameState().getMe().getLife();

        if (myHealth - pathfinder.movesToReach(goal) < healThreshold) {
            logger.info("moves to reach goal=" + pathfinder.movesToReach(goal));
            logger.info("Would go to a mine, but HP will drain below the health threshold. Going to pub instead.");
            return pathfinder.goToClosestPub();
        } else {
            return pathfinder.goToClosestMine();
        }
    }

    @Override
    public String getName() {
        return "Looting Decision Maker";
    }

}
