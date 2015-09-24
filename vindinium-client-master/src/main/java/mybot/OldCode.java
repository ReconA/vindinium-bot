package mybot;

import com.brianstempin.vindiniumclient.bot.advanced.Mine;
import com.brianstempin.vindiniumclient.bot.advanced.Pub;
import com.brianstempin.vindiniumclient.bot.advanced.Vertex;

/**
 * Old code that may be useful in the future. 
 * 
 */

public class OldCode {

//        /**
//     * Find closest vertex adjacent to a pub.
//     *
//     * @return The closest vertex adjacent to a pub.
//     */
//    private Vertex findClosestPub() {
//        int minDistance = Integer.MAX_VALUE;
//        Vertex closest = null;
//
//        for (Vertex v : adjacentToPub) {
//            if (v.getDistance() < minDistance) {
//                closest = v;
//                minDistance = v.getDistance();
//            }
//        }
//
//        return closest;
//    }
//
//    /**
//     * Check if any pub is adjacent to this vertex. If there is an adjacent pub,
//     * add the vertex to the list of vertices with adjacent pub, and add the pub
//     * to the vertices adjacent pubs list.
//     *
//     * @param v Vertex to be checked.
//     */
//    private void checkIfAdjacentToPub(Vertex v) {
//        for (Pub p : gameState.getPubs().values()) {
//            if (calcManhattanDistance(p.getPosition(), v.getPosition()) == 1) {
//                adjacentToPub.add(v);
//                v.addAdjacentPub(p);
//            }
//        }
//    }
//
//    /**
//     * Check if any mine is adjacent to this vertex. If there is an adjacent
//     * mine, add the vertex to the list of vertices with adjacent mines, and add
//     * the mine to the vertices adjacent mines list.
//     *
//     * @param v Vertex to be checked.
//     */
//    private void checkIfAdjacentToMine(Vertex v) {
//        for (Mine m : gameState.getMines().values()) {
//            if (calcManhattanDistance(m.getPosition(), v.getPosition()) == 1) {
//                adjacentToMine.add(v);
//                v.addAdjacentMine(m);
//            }
//        }
//    }
//
//
//
//    /**
//     * Find the closest vertex adjacent to a mine.
//     *
//     * @return Closest vertex adjacent to a mine.
//     */
//    private Vertex findClosestMine() {
//        int minDistance = Integer.MAX_VALUE;
//        Vertex closest = null;
//
//        for (Vertex v : this.adjacentToMine) {
//            for (Mine m : v.getAdjacentMines()) {
//                if (v.getDistance() < minDistance
//                        && !isMyMine(m)) {
//                    closest = v;
//                    minDistance = v.getDistance();
//                }
//            }
//        }
//
//        return closest;
//    }
}
