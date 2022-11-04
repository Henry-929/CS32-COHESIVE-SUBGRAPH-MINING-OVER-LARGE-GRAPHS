package kcore.decomposition;

import java.util.Map;
import java.util.Set;

public class delSeparateGraph {
    private final Map<Integer, Set<Integer>> G;
    private final int distance;

    public delSeparateGraph(Map<Integer, Set<Integer>> G, int distance){

        this.G = G;
        this.distance = distance;

    }

    public Map<Integer, Set<Integer>> getG(){
        return G;
    }

    public int getDistance(){
        return distance;
    }

    
}
