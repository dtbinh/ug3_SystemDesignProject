package movement;

import java.util.ArrayList;

/**
 *
 * @author Matt
 */
public class PathTranslator {

    public static ArrayList<int[]> translatePath(ArrayList<int[]> waypoints) {
        ArrayList<int[]> translatedPath = new ArrayList<int[]>();
        int[] command;
        int[] nextCommand;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            command = waypoints.get(i);
            nextCommand = waypoints.get(i + 1);
            System.out.println("Curr Command "+i+": "+command[3]+" "+command[4]);
            System.out.println("Next Command "+i+": "+nextCommand[3]+" "+nextCommand[4]);
            if(i >= 1) {
                if((nextCommand[3]+command[3])/2 > 0 &&  (nextCommand[4]+command[4])/2 > 0)
                    translatedPath.add(new int[] {(nextCommand[3]+command[3])/2, (nextCommand[4]+command[4])/2});
            }
        }
        if(waypoints.size()>0)
            translatedPath.add(new int[] {waypoints.get(waypoints.size()-1)[3], waypoints.get(waypoints.size()-1)[4]});
        System.out.println("PATH_TRANSLATOR: size: "+translatedPath.size());
        System.out.println("");
        return translatedPath;
    }

}
