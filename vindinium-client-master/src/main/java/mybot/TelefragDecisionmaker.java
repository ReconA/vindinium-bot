package mybot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.advanced.AdvancedGameState;
import com.brianstempin.vindiniumclient.dto.GameState.Hero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Tries to telefrag an enemy standing on my spawn point. Will probably never be
 * used in real combat, but will be awesome when it works.
 *
 */
public class TelefragDecisionmaker implements DecisionMaker {

    private static final Logger logger = LogManager.getLogger(TelefragDecisionmaker.class);
    private AdvancedGameState gameState;
    private Pathfinder pathfinder;

    /**
     * If an enemy with more mines than me is standing on my spawn point, check if there a way to commit suicide this turn. 
     * @param pathfinder
     * @return 
     */
    @Override
    public boolean wantsToAct(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
        this.gameState = pathfinder.getGameState();
        
        Hero me = gameState.getMe();
        Hero target = gameState.getHeroesByPosition().get(me.getSpawnPos());
        int hitDamage = 20;
        if (target == null || target.getMineCount() < me.getMineCount()) { //Check if there is a suitable target
            return false;
        } else if (pathfinder.standingAdjacentToMine() && gameState.getMe().getLife() <= hitDamage) { //Check if there's a way to commit suicide. 
            return true;
        }
        
        return false;
    }

    /**
     * Commit suicide to telefrag an enemy. 
     * @return 
     */
    @Override
    public BotMove takeAction() {
        return pathfinder.goToClosestMine();
    }

    @Override
    public String getName() {
        return "Telefrag Decision Maker";
    }

}
