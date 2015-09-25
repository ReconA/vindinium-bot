package mybot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.advanced.AdvancedGameState;
import com.brianstempin.vindiniumclient.bot.advanced.Vertex;
import com.brianstempin.vindiniumclient.dto.GameState.Hero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CombatDecisionMaker implements DecisionMaker {
    private static final Logger logger = LogManager.getLogger(CombatDecisionMaker.class);
    private AdvancedGameState gameState;
    private Pathfinder pathfinder;
    
    private final int HIT_DMG = 20;
    
    @Override
    public boolean wantsToAct(Pathfinder pathfinder) {
        this.gameState = pathfinder.getGameState();
        this.pathfinder = pathfinder;
        
        for (Hero h : gameState.getHeroesById().values()) {
            if (h.getId() != gameState.getMe().getId()
                    && pathfinder.calcDistance(pathfinder.getCurrentPosition(), h.getPos()) < 4) {
                logger.info("Found nearby enemy at " + h.getPos());
                return true;
            }
        }
        
        return false;
    }

    @Override
    public BotMove takeAction() {
        logger.info("Bot is frozen with fear.");
        return BotMove.STAY;
    }
    
    private BotMove flee() {
        return BotMove.STAY;
    }
    
    private void evaluateVertex(Vertex v) {
        
    }

    @Override
    public String getType() {
        return "Combat Decision Maker";
    }

}
