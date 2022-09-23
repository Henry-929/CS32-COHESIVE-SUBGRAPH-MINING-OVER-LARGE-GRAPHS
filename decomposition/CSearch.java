package kcore.decomposition;

import kcore.ListLinearHeap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class CSearch {
    Integer n,m;
    int[] peer_seq, degree, core, pstart, edges;


    public Map<Integer, ArrayList<Integer>> loadGraph(String path) throws FileNotFoundException {
        Map<Integer, ArrayList<Integer>> G = new HashMap<>();

        Scanner sc = new Scanner(new BufferedReader(new FileReader(path)));
        String str = sc.nextLine();
        String[] s = str.split("\\s+");
        n = Integer.valueOf(s[0]);
        m = Integer.valueOf(s[1]);

        while (sc.hasNextLine()) {
            ArrayList<Integer> set = new ArrayList<>();

            String str2 = sc.nextLine();
            String[] s2 = str2.split("\\s+");

            Integer vid0 = Integer.valueOf(s2[0]);
            Integer vid1 = Integer.valueOf(s2[1]);

            if (G.get(vid0)!= null)
                set=G.get(vid0);
            set.add(vid1);
            G.put(vid0,set);
        }
//        System.out.println(G);
        sc.close();

        peer_seq = new int[n];
        degree = new int[n];
        core = new int[n];
        int dMax = 0;
        for (int i=0; i<n ; i++){
            peer_seq[i] = i;
            if (G.get(i) != null){
                degree[i] = G.get(i).size();
                if (degree[i]>dMax)
                    dMax = degree[i];
            }else {
                degree[i] = 0;
            }
//            System.out.println(degree[i]);
        }

        pstart = new int[n+1];
        edges = new int[m*2];
        pstart[0] = 0;
        for (int i=0;i<n;i++){
            if (G.get(i) != null){
                int j = 0;
                for (Integer nei : G.get(i)){
                    edges[pstart[i]+j] = nei;
                    j++;
                }
                pstart[i+1] = pstart[i] + G.get(i).size();
            }else {
                pstart[i+1] = pstart[i];
            }
        }
//        System.out.println("pstart："+Arrays.toString(pstart));
//        System.out.println("edges："+Arrays.toString(edges));
        System.out.println("n="+n+",m="+m+",dMAX="+dMax);

//        for (int i=0;i<degree.length;i++){
//            System.out.print(degree[i]+",");
//        }
        return G;
    }

    public void coreDecompositionLinearList(Map<Integer, ArrayList<Integer>> G){
        int max_core = 0;
        int u = 0;
        int key = 0;
        ListLinearHeap linearHeap = new ListLinearHeap(n,n-1,peer_seq,degree);
        for (int i=0;i<n;i++){
            HashMap<Integer,Integer> map = linearHeap.pop_min();
            for (Map.Entry<Integer,Integer> entry : map.entrySet()){
                u = entry.getKey();
                key = entry.getValue();
            }

            if (key > max_core)
                max_core = key;
            peer_seq[i] = u;
            core[u] = max_core;
            for (int j=pstart[u]; j<pstart[u+1];j++){
                if (core[edges[j]] == 0)
                    linearHeap.decrement(edges[j]);
            }
        }

        System.out.println("The peeling sequence, i.e., degeneracy order (即：每个点被删除的顺序)：");
        for(int i = 0; i < n; i++)
            System.out.print(peer_seq[i]+", ");
        System.out.println("The core number of each vertex: ");
        for(int i = 0; i < n; i++)
            System.out.println("For vertex "+i+", it core number = "+core[i]);
    }


    public static void main(String[] args) throws FileNotFoundException {
        long startTime =  System.currentTimeMillis();

        CSearch search = new CSearch();
        Map<Integer, ArrayList<Integer>> G = search.loadGraph("decomposition/data/toy1.txt"); // use a GitHub path
        search.coreDecompositionLinearList(G);

        long endTime =  System.currentTimeMillis();
        long usedTime = endTime-startTime;
        System.out.println("Time consuming: " + usedTime);
    }
}
