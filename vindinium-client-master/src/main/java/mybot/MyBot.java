package mybot;

import com.brianstempin.vindiniumclient.bot.advanced.AdvancedBot;
import com.brianstempin.vindiniumclient.bot.advanced.AdvancedGameState;
import com.brianstempin.vindiniumclient.bot.BotMove;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyBot implements AdvancedBot {

    private DecisionMaker[] decisionMakers = {new HealingDecisionMaker(), new LootingDecisionMaker()};

    private final static Logger logger = LogManager.getLogger(MyBot.class);

    /**
     * Move is called every turn to decide what direction we move this turn.
     *
     *
     * @param gameState Current game state.
     * @return The direction next move will go to.
     */
    @Override
    public BotMove move(AdvancedGameState gameState) {
        logger.info("Creating pathfinder.");
        Pathfinder pathfinder = new Pathfinder(gameState);
        logger.info("Choosing decision maker.");
        for (DecisionMaker dm : decisionMakers) {
            if (dm.wantsToAct(pathfinder)) {
                logger.info(dm.getType() + " shall lead me to glory!");
                return dm.takeAction();
            }
        }

        logger.info("No decision makers chosen. Staying still");
        return BotMove.STAY;
    }

    @Override
    public void setup() {
    }

    @Override
    public void shutdown() {
    }

}
