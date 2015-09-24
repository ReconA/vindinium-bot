package mybot;

import com.brianstempin.vindiniumclient.bot.BotMove;

/**
 * Interface for decision makers. Decision makers are used to imitate a decision tree. 
 * They are called in predetermined order until one wants to take action. 
 *
 */
public interface DecisionMaker {
    /**
     * Determines whether this decision maker wants to take action. 
     * For example, CombatDecisionMaker would want to act if there is an enemy nearby.
     * @param pathfinder 
     * @return True if this decider wants to act. 
     */
    public boolean wantsToAct(Pathfinder pathfinder);
    
    /**
     * Decide what move this DM will make. 
     * @return Move direction. 
     */
    public BotMove takeAction();
    
    /**
     * Get a string that describes this decision maker. 
     * @return Description of the decision maker. For example, "Combat Decision Maker".
     */
    public String getType();
}
