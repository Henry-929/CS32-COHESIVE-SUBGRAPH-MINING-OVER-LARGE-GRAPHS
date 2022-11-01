package kcore;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class Problem2 {
    static Integer n;
    Integer m;
    static int[] peer_seq;
    int[] degree;
    int[] core;
    int[] pstart;
    int[] edges;


    //导入原数据图G
    public Map<Integer, Set<Integer>> loadGraph(String path) throws FileNotFoundException {
        Map<Integer, Set<Integer>> G = new HashMap<>();

        Scanner sc = new Scanner(new BufferedReader(new FileReader(path)));
        String str = sc.nextLine();
        String[] s = str.split("\\s+");
        n = Integer.valueOf(s[0]);
        m = Integer.valueOf(s[1]);

        while (sc.hasNextLine()) {
            Set<Integer> set1 = new HashSet<>();
            Set<Integer> set2 = new HashSet<>();
            String str2 = sc.nextLine();
            String[] s2 = str2.split("\\s+");

            Integer vid0 = Integer.valueOf(s2[0]);
            Integer vid1 = Integer.valueOf(s2[1]);

            if (G.get(vid0)!= null)
                set1=G.get(vid0);
            set1.add(vid1);
            G.put(vid0,set1);

            // add edges    
            if(G.get(vid1) != null)
                set2 = G.get(vid1);        
            set2.add(vid0);
            G.put(vid1, set2);

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
        return G;
    }

    /**
     * 找到满足 Maximizing the minimum degree 的图 G
     * @param G 表示一个当前步骤的图G
     * @param list 表示查询节点 query node 集合
     * @return 返回找到符合条件的 Maximizing the minimum degree的图 G
     */
    public HashMap<String, Object> findMaxMinD(Map<Integer, Set<Integer>> G,ArrayList<Integer> list){
        int max_core = 0;
        int u = 0;
        int key = 0;
        int i=0;
        int temp = 0;

        HashMap<String, Object> retrunMap = new HashMap<>();
        ListLinearHeap linearHeap = new ListLinearHeap(n,n-1,peer_seq,degree);

        while (checkConnection(list, G) && i<n){

            HashMap<Integer,Integer> map = linearHeap.pop_min();
            for (Map.Entry<Integer,Integer> entry : map.entrySet()){
                u = entry.getKey();
                key = entry.getValue();
            }

            if (key > max_core)
                max_core = key;
            peer_seq[i] = u;
            core[u] = max_core;

            for(int queryNode : list){
                if (queryNode == u){
                    //将贪心算法过程中多删除的节点进行回溯，重新加入G中
                    while (u > 0 && core[u] == core[u-1]){
                        u = u-1;
                        HashSet<Integer> set = new HashSet<>();
                        G.put(u,set);
                        G.get(u).add(list.get(0));
                        G.get(list.get(0)).add(u);
                    }

                    u=queryNode;

                    while (core[u] == core[u+1]){
                        u = u+1;
                        HashSet<Integer> set = new HashSet<>();
                        G.put(u,set);
                        G.get(u).add(list.get(0));
                        G.get(list.get(0)).add(u);
                    }

                    retrunMap.put("queryNode",queryNode);
                    retrunMap.put("G", G);
                    return retrunMap;
                }
            }

            for (int j=pstart[u]; j<pstart[u+1];j++){
                if (core[edges[j]] == 0)
                    linearHeap.decrement(edges[j]);
            }

            //System.out.println("删除的节点是 "+u);
            deleteNode(u, G);
            i++;
        }

        //对删除了节点造成query node节点不相连，进行回溯
        G.put(temp, null);
        retrunMap.put("G", G);
        return retrunMap;
    }


    //导入查询节点
    public ArrayList<Integer> loadQueryNode(String path) throws FileNotFoundException {
        ArrayList<Integer> list = new ArrayList<>();

        Scanner sc = new Scanner(new BufferedReader(new FileReader(path)));
        String str = sc.nextLine();
        String[] s = str.split(",");
        for (int i = 0; i < s.length; i++) {
            list.add(Integer.valueOf(s[i]));
        }

        sc.close();
        return list;
    }

    //检查查询节点之间的连接性
    public boolean checkConnection(ArrayList<Integer> list, Map<Integer, Set<Integer>> G) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 1; j < list.size(); j++) {
                int d =  getDistance(list.get(i), list.get(j), G);
                if (d == -1) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * 获取两个搜索节点间最短路径距离，用于判断两个搜索节点是否相连（采用BSF算法）
     * @param p1 一个搜索节点 query node
     * @param p2 一个搜索节点 query node
     * @param G
     * @return 返回两个搜索节点间最短路径距离（若相连则返回最短路径距离，若不相连则返回-1）
     */
    public int getDistance(Integer p1, Integer p2, Map<Integer, Set<Integer>> G) {

        Queue<Integer> queue = new LinkedList<Integer>(); // 队列，用于BFS搜素
        int distance = 0;
        int temp = 0;
        int queueEnd = 0;
        Set<Integer> tempCol = new HashSet<>();
        // visit数组（visit为标志是否访问过的数组,访问过为1，否则为0）
        int[] visit = new int[n];
        // isQueueEnd标志节点i是否是某轮bfs广搜的终点，若是，其为true，,需要使distance++
        boolean[] isQueueEnd = new boolean[n];

        // 初始化，对p1进行设定
        queue.add(p1);
        visit[p1] = 1;
        isQueueEnd[p1]=true;

        while (!Objects.equals(queue.peek(), p2)) {
            temp = queue.poll(); // 弹出并保存queue的头元素
            // 将与queue头元素直接相连，且未访问过的元素入队
            tempCol = G.get(temp); // tempCol保存头元素对应的关系矩阵行
            for (int t : tempCol){  // 头元素对应的关系矩阵行，遍历此行中的所有元素，并将其加入队列,同时把其标记为访问过
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


    /**
     * 从当前y步骤时图Gy中删除节点v以及相关边的关系
     * @param targetNode the node to be deleted
     * @param currentGraph the current graph
     */
    public void deleteNode(int targetNode, Map<Integer, Set<Integer>> currentGraph) {
        Set<Integer> removeReNodes = currentGraph.remove(targetNode);
        if (removeReNodes != null){
            for (int i : removeReNodes){
                currentGraph.get(i).remove(targetNode);
            }
        }
    }

    /**
     * 获取与节点p1在图G中不相连的节点数组（采用BSF算法）
     * @param p1 一个搜索节点 query node
     * @param G
     * @return 返回是否相连数组
     */
    public int[] getSeparateComponent(int p1, Map<Integer, Set<Integer>> G) {

        Queue<Integer> queue = new LinkedList<Integer>(); // 队列，用于BFS搜素
        int temp = 0;
        Set<Integer> tempCol = new HashSet<>();
        // visit数组（visit为标志是否访问过的数组,访问过为1，否则为0）
        int[] visited = new int[n];

        // 初始化，对p1进行设定
        queue.add(p1);
        visited[p1] = 1;

        while (!queue.isEmpty()) {
            temp = queue.poll(); // 弹出并保存queue的头元素
            // 将与queue头元素直接相连，且未访问过的元素入队
            tempCol = G.get(temp); // tempCol保存头元素对应的关系矩阵行
            for (int t : tempCol){  // 头元素对应的关系矩阵行，遍历此行中的所有元素，并将其加入队列,同时把其标记为访问过
                if (visited[t] == 0){
                    queue.add(t);
                    visited[t] = 1;
                }
            }
        }

        return visited;
    }

    //删除与query node 不相连的节点
    public  Map<Integer, Set<Integer>> delSeparateComponent(ArrayList<Integer> list,Map<Integer, Set<Integer>> G){
        if (!checkConnection(list, G)){
            return G;
        }

        int[] separateComponent = getSeparateComponent(list.get(0), G);
        for (int i=0;i<n;i++){
            if (separateComponent[i]==0){
                deleteNode(i,G);
            }
        }
        return G;
    }


    public static void main(String[] args) throws FileNotFoundException {
        Problem2 search = new Problem2();
        Map<Integer, Set<Integer>> G = search.loadGraph("data/fb.txt");
        ArrayList<Integer> list = search.loadQueryNode("data/QD1.txt");

        long startTime = System.currentTimeMillis();

        HashMap<String, Object> maxMinD = search.findMaxMinD(G, list);
        Map<Integer, Set<Integer>> maxMinDGraph = (Map<Integer, Set<Integer>>) maxMinD.get("G");
        Map<Integer, Set<Integer>> delSepareteGraph = search.delSeparateComponent(list, maxMinDGraph);

        long endTime = System.currentTimeMillis();
        long usedTime = endTime - startTime;

        int queryNode = (int) maxMinD.get("queryNode");
        int tempEdgeSize = 0;
        for (int i : delSepareteGraph.keySet()){
            tempEdgeSize += delSepareteGraph.get(i).size();
        }
        float nodeSize = delSepareteGraph.size();
        float edgeSize = tempEdgeSize/2;
        float f = edgeSize / nodeSize;

        System.out.println("Solution graph minimum degree is: "+ search.core[queryNode]);
        System.out.println("Solution graph density is: " + f);
        System.out.println("Solution graph size is: " + delSepareteGraph.size());
        System.out.println("Solution used time: " + usedTime);

    }
}
