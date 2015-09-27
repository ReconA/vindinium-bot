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

    @Override
    public boolean wantsToAct(Pathfinder pathfinder) {
        logger.info("Looting a mine.");
        return true; //Last one in the decision tree.
    }

    @Override
    public BotMove takeAction(Pathfinder pathfinder) {
        Vertex goal = pathfinder.getClosestMine();
        int healThreshold = 50;
        int myHealth = pathfinder.getGameState().getMe().getLife();
       
        if (myHealth - goal.getDistance() < healThreshold) {
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
