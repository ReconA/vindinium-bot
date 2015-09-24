package mybot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.advanced.AdvancedGameState;
import com.brianstempin.vindiniumclient.bot.advanced.Vertex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Decides whether to loot a mine and which mine to loot. 
 */
public class LootingDecisionMaker implements DecisionMaker {

    private static final Logger logger = LogManager.getLogger(LootingDecisionMaker.class);
    private AdvancedGameState gameState;
    private Pathfinder pathfinder;

    @Override
    public boolean wantsToAct(Pathfinder pathfinder) {
        logger.info("Looting Decision Maker thinks...");

        this.pathfinder = pathfinder;
        this.gameState = this.pathfinder.getGameState();

        return true; //Last one in the decision tree.
    }

    @Override
    public BotMove takeAction() {
        Vertex goal = pathfinder.goToClosestMine();
        logger.info(this.getType() + " takes action and heads towards " + goal + "!");
        return pathfinder.moveTowards(goal);
    }

    @Override
    public String getType() {
        return "Looting Decision Maker";
    }

}
