package mybot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Decides whether to loot a mine and which mine to loot. 
 */
public class LootingDecisionMaker implements DecisionMaker {

    private static final Logger logger = LogManager.getLogger(LootingDecisionMaker.class);
    private Pathfinder pathfinder;

    @Override
    public boolean wantsToAct(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
        return true; //Last one in the decision tree.
    }

    @Override
    public BotMove takeAction() {
        return pathfinder.goToClosestMine();
    }

    @Override
    public String getName() {
        return "Looting Decision Maker";
    }

}
