package mybot;

import com.brianstempin.vindiniumclient.bot.advanced.AdvancedBot;
import com.brianstempin.vindiniumclient.bot.advanced.AdvancedGameState;
import com.brianstempin.vindiniumclient.bot.BotMove;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyBot implements AdvancedBot {

    private final DecisionMaker[] decisionMakers = {
        new InnCamperDecisionMaker(),
        new TelefragDecisionmaker(),
//        new CombatDecisionMaker(),
        new HealingDecisionMaker(),
        new LootingDecisionMaker()};

    private final static Logger logger = LogManager.getLogger(MyBot.class);

    /**
     * Goes through decision makers until one of them wants to act. Then control
     * is passed to that DM.
     *
     * @param gameState Current game state.
     * @return The direction next move will go to.
     */
    @Override
    public BotMove move(AdvancedGameState gameState) {
        long startTime = System.nanoTime();

        logger.info("Creating pathfinder.");
        Pathfinder pathfinder = new Pathfinder(gameState);

        logger.info("Choosing decision maker.");
        BotMove move = null;
        for (DecisionMaker dm : decisionMakers) {
            logger.info(dm.getName() + " thinks...");
            if (dm.wantsToAct(pathfinder)) {
                logger.info(dm.getName() + " shall lead me to glory!");
                move = dm.takeAction();
                break;
            }
        }

        if (move == null) {
            logger.info("No decision makers chosen. Staying still");
            move = BotMove.STAY;
        }
        
        long stopTime = System.nanoTime();
        long turnTime = (stopTime - startTime);
        logger.info("Turn time " + turnTime + "ns");
        return move;
    }

    @Override
    public void setup() {
    }

    @Override
    public void shutdown() {
    }

}
