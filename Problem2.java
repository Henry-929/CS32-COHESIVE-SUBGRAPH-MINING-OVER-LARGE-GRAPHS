package kcore.decomposition;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class Problem2 {
    Integer n,m;
    int[] peer_seq, degree, core, pstart, edges;


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

        System.out.println("n="+n+",m="+m+",dMAX="+dMax);

        // 返回图G 数据类型格式是：{(0,{1,2,3}),(1,{2,3})....} 表示点0 与 点1，2，3相邻（直接相连），点1 与点2，3相邻。
        System.out.println("G: "+G);
        return G;
    }

    public void coreDecompositionLinearList(Map<Integer, Set<Integer>> G){
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

    }

//    public Map<Integer, Set<Integer>> findMaxMinD(){
//        while (){
//
//        }
//
//
//        return null;
//    }

    public int getDistance(Integer p1, Integer p2, Map<Integer, Set<Integer>> G) {

        Queue<Integer> queue = new LinkedList<Integer>(); // 队列，用于BFS搜素
        int distance = 0;
        Integer temp = 0;
        Integer queueEnd = 0;
        Set<Integer> tempCol = new HashSet<>();
        // visit数组（visit为标志是否访问过的数组,访问过为1，否则为0）
        int[] visit = new int[G.size()];
        // isQueueEnd标志节点i是否是某轮bfs广搜的终点，若是，其为true，,需要使distance++
        boolean[] isQueueEnd = new boolean[G.size()];

        // 初始化，对p1进行设定
        queue.add(p1);
        visit[p1] = 1;
        isQueueEnd[p1]=true;

        while (!Objects.equals(queue.peek(), p2)) {
            temp = queue.poll(); // 弹出并保存queue的头元素
            // 将与queue头元素直接相连，且未访问过的元素入队
//            System.out.println("++++++++"+G.get(temp).size());
            tempCol = G.get(temp); // tempCol保存头元素对应的关系矩阵行
            for (Integer t : tempCol){  // 头元素对应的关系矩阵行，遍历此行中的所有元素，并将其加入队列,同时把其标记为访问过
                if (visit[t] == 0){
                    queue.add(t);
                    visit[t] = 1;
                    queueEnd = t; // 记录当前队尾
                }
            }

            // 最后队列空，说明没有p1到p2的直接通路
            if (queue.isEmpty())
                return -1;

            // 记录当前队尾，并使distance++
            if (isQueueEnd[temp]) {
                isQueueEnd[queueEnd]=true;
                distance++;
            }
        }
        return distance;
    }

    //从当前y步骤时图Gy中删除节点v以及相关边的关系
    // add removed node to seq list
    public void deleteNode(int v, Map<Integer, Set<Integer>> G){
        List<Integer> seq = new ArrayList<Integer>();

        ListLinearHeap linearHeap = new ListLinearHeap(n,n-1,peer_seq,degree);
        G.remove(v);
        linearHeap.remove(v);

        for (int j=pstart[v]; j<pstart[v+1];j++){
            if (core[edges[j]] == 0)
                linearHeap.decrement(edges[j]);
        }

        seq.add(v);
        System.out.println("Seq list: "+seq);

    }


    /**
     * remove the target nodes and corresponding edges
     * for-loop can be improved, but I cannot ^-^
     * Who can try just try
     * @autor Yifan
     * @param targetNode the node to be deleted
     * @param currentGraph the current graph
     * @return nothing
     * @throws null
     */
    public void deleteNodeVersion2(int targetNode, Map<Integer, Set<Integer>> currentGraph){
        //remove the nodes
        currentGraph.remove(targetNode);
        // remove the edges
        Set<Integer> integers = currentGraph.keySet();
        for (Integer integer : integers) {
            if (currentGraph.get(integer).contains(targetNode)){
                if (currentGraph.get(integer).size()==1){
                    deleteNodeVersion2(integer, currentGraph);
                }else{
                currentGraph.get(integer).remove(targetNode);
                }
            }
        }
        System.out.println(targetNode+" is deleted this time.");
    }

    public static void main(String[] args) throws FileNotFoundException {
        long startTime =  System.currentTimeMillis();

        Problem2 search = new Problem2();
        Map<Integer, Set<Integer>> G = search.loadGraph("data/toy1.txt"); //change the Absolute path into Relative path
//        search.coreDecompositionLinearList(G);

        int distance = search.getDistance(5, 6, G);
        System.out.println("图中点0-3的距离为 "+distance);

        int v = 9;
        search.deleteNodeVersion2(v, G);
        System.out.println("G now after remove node "+v+": "+"\n"+G);

        long endTime =  System.currentTimeMillis();
        long usedTime = endTime-startTime;
        System.out.println("used time: "+usedTime);
    }

}
