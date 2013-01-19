/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package strategy;

import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author s0791003
 */
public class Angles {

    private static Point[][] goalPoints = new Point[][]{
        //Goal Left
        new Point[]{new Point(1, 6), new Point(1, 7), new Point(1, 8), new Point(1, 9), new Point(1, 10), new Point(1, 11), new Point(1, 12)},
        //Goal Right
        new Point[]{new Point(34, 6), new Point(34, 7), new Point(34, 8), new Point(34, 9), new Point(34, 10), new Point(34, 11), new Point(34, 12)}};

    public static double getAngle(Point a, Point b) {

        return Math.toDegrees(Math.atan2((a.y - b.y), (b.x - a.x)));

    }

    public static ArrayList<int[]> goalMoves(Point ball, int side) {

        int angle;
        int[] vector = new int[2];

        ArrayList<int[]> moves = new ArrayList<int[]>();

        for (Point i : goalPoints[side]) {

            angle = (int) getAngle(i, ball);

            vector[0] = (int)(ball.x - i.x);
            vector[1] = (int)(ball.y - i.y);

            if(vector[1] == 0) continue;
            
            double length = Math.sqrt(vector[0]*vector[0] + vector[1]*vector[1]);

            double[] normVector = new double[]{vector[0]/length, vector[1]/length};
            System.out.println(ball.x + normVector[0]*2 + " " + ball.y + normVector[1]*2);
            
            moves.add(new int[]{(int)Math.round(ball.x + normVector[0]*3), (int)Math.round(ball.y + normVector[1]*3)});

        }

        return moves;
    }

    public static void main(String[] args){
        ArrayList<int[]> angles = Angles.goalMoves(new Point(15, 9), 0);
        
        for(int[] i: angles){
            System.out.println("Goal square: (" + i[0] + ", " + i[1] + ")");
        }
        
        
    }


}
