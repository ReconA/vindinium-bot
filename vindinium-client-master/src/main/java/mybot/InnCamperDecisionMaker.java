package mybot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.advanced.AdvancedGameState;
import com.brianstempin.vindiniumclient.dto.GameState.Hero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * If we are winning considerably, we won't risk anything and camp at an inn.
 *
 * @author Atte
 */
public class InnCamperDecisionMaker implements DecisionMaker {

    private static final Logger logger = LogManager.getLogger(TelefragDecisionmaker.class);
    private AdvancedGameState gameState;
    private Pathfinder pathfinder;

    /**
     * Check if I'm by far the richest hero.
     *
     * @param pathfinder
     * @return
     */
    @Override
    public boolean wantsToAct(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
        this.gameState = pathfinder.getGameState();

        Hero me = gameState.getMe();
        int myMines = me.getMineCount();
        int myGold = me.getGold();
        int goldMargin = 200;
        int mineMargin = 2;

        for (Hero enemy : gameState.getHeroesById().values()) {
            if (enemy.getId() == me.getId()) {
                continue;
            }
            if (myGold < enemy.getGold() + goldMargin
                    || myMines < enemy.getMineCount() + mineMargin) {
                return false;
            }
        }

        return true;
    }

    /**
     * Stand still if adjacent to a pub and in good health. Otherwise, go to the
     * closest pub.
     *
     * @return
     */
    @Override
    public BotMove takeAction() {
        if (pathfinder.standingAdjacentToPub() && gameState.getMe().getLife() > 50) {
            return BotMove.STAY;
        } else {
            return pathfinder.goToClosestPub();
        }
    }

    @Override
    public String getName() {
        return "Inn Camper Decision Maker";
    }

}
