package com.brianstempin.vindiniumclient.bot.advanced.mybot;

import com.brianstempin.vindiniumclient.bot.advanced.Vertex;
import com.brianstempin.vindiniumclient.dto.GameState;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

public class MyBotTest {

//    @Test
//    public void AStarTest() {
//        MyBot bot = new MyBot();
//        Vertex[][] arr = new Vertex[3][4];
//        for (int x = 0; x < 3; x++) {
//            for (int y = 0; y < 4; y++) {
//                arr[x][y] = new Vertex(new GameState.Position(x, y), new ArrayList<Vertex>());
//            }
//        }
//
//        //Set every vertex to neighbour its adjacent vertices.
//        for (int x = 0; x < 3; x++) {
//            for (int y = 0; y < 4; y++) {
//                try {
//                    arr[x][y].getAdjacentVertices().add(arr[x + 1][y]);
//                    arr[x][y].getAdjacentVertices().add(arr[x - 1][y]);
//                    arr[x][y].getAdjacentVertices().add(arr[x][y + 1]);
//                    arr[x][y].getAdjacentVertices().add(arr[x][y - 1]);
//                } catch (ArrayIndexOutOfBoundsException ex) {
//                    continue; //Lazy way to this
//                }
//            }
//        }
//
//        //Set up an obstacle by removing some vertices. 
//        for (int x = 0; x < 3; x++) {
//            for (int y = 0; y < 4; y++) {
//                arr[x][y].getAdjacentVertices().remove(arr[1][1]);
//                arr[x][y].getAdjacentVertices().remove(arr[1][2]);
//            }
//        }
//
//        Vertex[] path = bot.searchPath(arr[0][1], arr[2][1]);
//        Vertex[] expected = {arr[0][0], arr[1][0], arr[2][0], arr[2][1]};
//        assertArrayEquals(expected, path);
//
//    }

}
