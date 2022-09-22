package kcore;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class CSearch2 {
    Integer n,m,dMax;
    int[] peer_seq, seq, degree, core;

    public Map<Integer, Set<Integer>> loadGraph(String path) throws FileNotFoundException {
        Map<Integer, Set<Integer>> G = new HashMap<>();

        Scanner sc = new Scanner(new BufferedReader(new FileReader(path)));
        String str = sc.nextLine();
        String[] s = str.split("\\s+");
        n = Integer.valueOf(s[0]);
        m = Integer.valueOf(s[1]);

        while (sc.hasNextLine()) {
            Set<Integer> set = new HashSet<>();

            String str2 = sc.nextLine();
            String[] s2 = str2.split("\\s+");

            Integer vid0 = Integer.valueOf(s2[0]);
            Integer vid1 = Integer.valueOf(s2[1]);

            if (G.get(vid0)!= null)
                set=G.get(vid0);
            set.add(vid1);
            G.put(vid0,set);
        }
        sc.close();

        peer_seq = new int[n];
        seq = new int[n];
        degree = new int[n];
        core = new int[n];
        dMax = 0;
        for (int i=0; i<n ; i++){
            peer_seq[i] = i;
            seq[i] = i;
            if (G.get(i) != null){
                degree[i] = G.get(i).size();
                if (degree[i]>dMax)
                    dMax = degree[i];
            }else {
                degree[i] = 0;
            }
        }

        System.out.println("n="+n+",m="+m+",dMAX="+dMax);
        return G;
    }

    public void coreDecompositionLinearList(Map<Integer, Set<Integer>> G){
        int[] bin = new int[dMax + 1];
        for(int i = 0;i<=dMax;++i){
            bin[i] = 0;
        }

//        System.out.println("degree[]: "+Arrays.toString(degree));

        for(int i = 0;i<n;++i){
            ++bin[degree[i]];
        }

//        System.out.println("图中度数为i的点的数量 bin[]: "+Arrays.toString(bin));

        int start = 0;
        for(int i = 0;i<=dMax;++i){
            int num = bin[i];
            bin[i] = start;
            start += num;
        }

//        System.out.println("vert中第一个度数为i的点在vert中的下标 bin[]: "+Arrays.toString(bin));

        int[] pos = new int[n];
        int[] vert = new int[n];
        for(int i = 0;i<n;++i){
            int v = i;
            pos[v] = bin[degree[v]];
            vert[pos[v]] = v;
            ++bin[degree[v]];
        }

        for(int i = dMax;i > 0;--i){
            bin[i] = bin[i-1];
        }
        bin[0] = 0;

//        System.out.println("vert[]: "+Arrays.toString(vert));
//        System.out.println("bin[]: "+Arrays.toString(bin));
//        System.out.println("pos[]: "+Arrays.toString(pos));

        for(int i = 0;i<n;++i){
            int v = vert[i];
            peer_seq[i] = v;
            core[v] = degree[v];
            for(Integer u : G.get(v)){
                if(degree[u] == degree[v]) continue;

                int du = degree[u];
                int pu = pos[u];
                int pw = bin[du];
                int w = vert[pw];

//                System.out.println("u:"+u+" "+degree[u]+" || "+"v:"+v+" "+degree[v]+", w 是 "+w+", pw是 "+pw+", pu是 "+pu);

                if(u != w){
                    pos[u] = pw;
                    vert[pu] = w;
                    pos[w] = pu;
                    vert[pw] = u;
                }
                ++bin[degree[u]];
                --degree[u];
            }
        }

        System.out.println("The peeling sequence, i.e., degenerqcy order (即：每个点被删除的顺序)：");
        for(int i = 0; i < n; i++)
            System.out.print(peer_seq[i]+", ");
        System.out.println("The core number of each vertex: ");
        for(int i = 0; i < n; i++)
            System.out.println("For vertex " + i + ", it core number = " + core[i]);
    }

    public static void main(String[] args) throws FileNotFoundException {
        long startTime =  System.currentTimeMillis();

        CSearch2 search = new CSearch2();
        Map<Integer, Set<Integer>> G = search.loadGraph("data/fb.txt");
        search.coreDecompositionLinearList(G);

        long endTime =  System.currentTimeMillis();
        long usedTime = endTime-startTime;
        System.out.println(usedTime);
    }
}
