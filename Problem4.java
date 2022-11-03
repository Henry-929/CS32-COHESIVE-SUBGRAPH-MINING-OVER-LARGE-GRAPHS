package kcore;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import kcore.decomposition.ListLinearHeap;

public class Problem4 {
    Integer n,m;
    int[] peer_seq, degree, core, pstart, edges;

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

            if (G.get(vid1)!= null)
                set2=G.get(vid1);
            set2.add(vid0);
            G.put(vid1,set2);
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
    public HashMap<String, Object> findMaxMinD2(Map<Integer, Set<Integer>> G,ArrayList<Integer> list){
        int max_core = 0;
        int u = 0;
        int key = 0;
        int i=0;
        int temp = 0;
        Set<Integer> hashset = null;

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

            temp = u;
            hashset = G.get(temp);
            //System.out.println("删除的节点是 "+u);
            deleteNode(u, G);
            i++;
        }

        //对删除了节点造成query node节点不相连，进行回溯
        G.put(temp, hashset);
        for (int p : hashset){
            G.get(p).add(temp);
        }
        retrunMap.put("G", G);
        return retrunMap;
    }




    /**
     * 找到满足 Maximizing the minimum degree 的图 G
     * @param G 表示一个当前步骤的图G
     * @param list 表示查询节点 query node 集合
     * @return 返回找到符合条件的 Maximizing the minimum degree的图 G
     */
    public Map<Integer, Set<Integer>> findMaxMinD(Map<Integer, Set<Integer>> G,ArrayList<Integer> list){
        int max_core = 0;
        int u = 0;
        int key = 0;
        int i=0;

        ListLinearHeap linearHeap = new ListLinearHeap(n,n-1,peer_seq,degree);

        while (checkConnection(list, G) && i<n){
            i++;
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
                        G.put(u,new HashSet<>());
                        G.get(u).add(list.get(0));
                        G.get(list.get(0)).add(u);
                    }

                    u=queryNode;

                    while (core[u] == core[u+1]){
                        u = u+1;
                        G.put(u,new HashSet<>());
                        G.get(u).add(list.get(0));
                        G.get(list.get(0)).add(u);
                    }
                    return G;
                }
            }

            for (int j=pstart[u]; j<pstart[u+1];j++){
                if (core[edges[j]] == 0)
                    linearHeap.decrement(edges[j]);
            }

            //System.out.println("删除的节点是 "+u);
            deleteNode(u, G);
        }
        return G;
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

    //检查G节点数量是否超出size constraint
    public boolean checkSizeConstraint(int size, Map<Integer, Set<Integer>> G){
        if (G.size() <= size)
            return false;
        else
            return true;
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
     * 获取两个搜索节点间最短路径距离，用于判断两个搜索节点是否相连（采用BSF算法）
     * @param p1 一个搜索节点 query node
     * @param p2 一个搜索节点 query node
     * @param G
     * @return 返回两个搜索节点间最短路径距离（若相连则返回最短路径距离，若不相连则返回-1）
     */
    public int getDistance(int p1, int p2, Map<Integer, Set<Integer>> G) {

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
     * We need a function to delete all nodes that exceed the distance limit
     * what we need: bfs to calculate the distance, a set to record nodes and a DISTANCE LIMIT!
     * @param p1 The query node
     * @param G
     * @param d the distance limit
     * @return nodeSet a set of nodes that exceed the distance limit
     */
    public Set<Integer> getDistantNodeSet(int p1, Map<Integer, Set<Integer>> G, int d, ArrayList<Integer> list){

        Queue<Integer> queue = new LinkedList<Integer>(); // 队列，用于BFS搜素
        int distance = 0;
        int temp = 0;
        int queueEnd = 0;
        Set<Integer> tempCol = new HashSet<>();
        // visit数组（visit为标志是否访问过的数组,访问过为1，否则为0）
        int[] visit = new int[n];
        // isQueueEnd标志节点i是否是某轮bfs广搜的终点，若是，其为true，,需要使distance++
        boolean[] isQueueEnd = new boolean[n];

        // a set to record distant nodes
        Set<Integer> nodeSet = new HashSet<>();

        // 初始化，对p1进行设定
        queue.add(p1);
        visit[p1] = 1;
        isQueueEnd[p1]=true;

        while (!queue.isEmpty()) {
            temp = queue.poll(); // 弹出并保存queue的头元素
            // 将与queue头元素直接相连，且未访问过的元素入队
            tempCol = G.get(temp); // tempCol保存头元素对应的关系矩阵行
            for (int t : tempCol){  // 头元素对应的关系矩阵行，遍历此行中的所有元素，并将其加入队列,同时把其标记为访问过
                if (visit[t] == 0){
                    queue.add(t);
                    visit[t] = 1;
                    queueEnd = t; // 记录当前队尾

                    // This is a distant node and it is not a distant node
                    if (distance > d && !list.contains(t)){
                        nodeSet.add(t);
                    }
                }
            }
            // 记录当前队尾，并使distance++
            if (isQueueEnd[temp]) {
                isQueueEnd[queueEnd]=true;
                distance++;
            }
        }
        return nodeSet;
    }

    /**
     * 获取距离节点p1在图G中最远的节点（采用BSF算法）
     * @param p1 一个搜索节点 query node
     * @param G
     * @return 返回最远距离节点 和 与节点p1在图G中不相连的节点数组
     */
    public HashMap<String, Object> getFarthestNode(int p1, Map<Integer, Set<Integer>> G) {

        HashMap<String, Object> map = new HashMap<>();
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

        while (!queue.isEmpty()) {
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
            // 记录当前队尾，并使distance++
            if (isQueueEnd[temp]) {
                isQueueEnd[queueEnd]=true;
                distance++;
            }
        }

        map.put("visitArray",visit);
        map.put("farthestNode",queueEnd);
        map.put("fartdistance",distance-1);
        return map;
    }

    //删除与query node 不相连的节点
    public  Map<Integer, Set<Integer>> delSeparateComponent(ArrayList<Integer> list,Map<Integer, Set<Integer>> G){
        int[] separateComponent = (int[]) getFarthestNode(list.get(0), G).get("visitArray");
        for (int i=0;i<n;i++){
            if (separateComponent[i]==0){
                deleteNode(i,G);
            }
        }
        return G;
    }


    /**
     * 找到符合 size Constraint 的图G
     * @param G  表示一个当前步骤的图G
     * @param sizeConstraint
     * @param list  表示查询节点 query node 集合
     */

    public Map<Integer, Set<Integer>> findConstraintG2(Map<Integer, Set<Integer>> G,
                                                      int sizeConstraint,
                                                      ArrayList<Integer> list, int d){

        while(checkConnection(list, G) && checkSizeConstraint(sizeConstraint,G)){
            for(Integer i: list ){
                Set<Integer> nodeSet = new HashSet<>();
                nodeSet = getDistantNodeSet(i, G, d, list);
                for (Integer n: nodeSet){
                    deleteNode(n, G);  //删除最远距离node
                }
            }
            d--;
        }
        return G;                                   
    }


    /**
     * 找到符合 size Constraint 的图G
     * @param G  表示一个当前步骤的图G
     * @param sizeConstraint
     * @param list  表示查询节点 query node 集合
     */
    public Map<Integer, Set<Integer>> findConstraintG(Map<Integer, Set<Integer>> G,
                                                      int sizeConstraint,
                                                      ArrayList<Integer> list){
        int tempDistance = -1;
        int tempnode = -1;
        while(checkConnection(list, G) && checkSizeConstraint(sizeConstraint,G)){
            for(Integer i: list ){
                HashMap<String, Object> nodeMap = getFarthestNode(i, G);
                int farthestNode = (int) nodeMap.get("farthestNode");
                int fartdistance = (int) nodeMap.get("fartdistance");
                if (tempDistance < fartdistance && !list.contains(farthestNode)){
                    tempDistance = fartdistance;
                    tempnode = farthestNode;
                }
            }
            deleteNode(tempnode, G);  //删除最远距离node
            tempDistance = -1;
        }

        return G;
    }

    /**
     * 将与query node和query node相邻的节点进行屏蔽，不予以删除
     * @param farthestNode 查找到的距离query node最远距离节点
     * @param list（query node）
     * @param G
     * @return 返回最远距离节点（即将要删除的节点是否是 query node和query node相邻的节点）
     */
    public boolean checkQueryN(int farthestNode,ArrayList<Integer> list,Map<Integer, Set<Integer>> G){
        for (int i : list){
            for (int v : G.get(i)){
                if (farthestNode == v || farthestNode == i)
                    return false;
            }
        }
        return true;
    }


    public static void main(String[] args) throws FileNotFoundException {

        Problem4 search = new Problem4();
        Map<Integer, Set<Integer>> G = search.loadGraph("testdata/5_lastfm.txt");
        ArrayList<Integer> list = search.loadQueryNode("data/QD1.txt");
        int sizeConstraint = 30;
        int distance = G.size();
        System.out.println(distance);
        long startTime =  System.currentTimeMillis();


        HashMap<String, Object> maxMinD = search.findMaxMinD2(G, list);
        Map<Integer, Set<Integer>> maxMinDGraph = (Map<Integer, Set<Integer>>) maxMinD.get("G");
        Map<Integer, Set<Integer>> delSepareteGraph = search.delSeparateComponent(list, maxMinDGraph);
        Map<Integer, Set<Integer>> constraintG = search.findConstraintG2(delSepareteGraph, sizeConstraint, list, distance);
        // Map<Integer, Set<Integer>> constraintG = search.findConstraintG(delSepareteGraph, sizeConstraint, list);

        long endTime =  System.currentTimeMillis();
        long usedTime = endTime-startTime;

        int tempDegree = 0;
        for (int i : list){
            if (search.core[i]>tempDegree){
                tempDegree = search.core[i];
            }
        }

        int tempEdgeSize = 0;
        for (int i : constraintG.keySet()){
            tempEdgeSize += constraintG.get(i).size();
        }
        float nodeSize = constraintG.size();
        float edgeSize = tempEdgeSize/2;
        float f = edgeSize / nodeSize;

        System.out.println("Solution graph minimum degree is: "+ tempDegree);
        System.out.println("Solution graph density is: " + f);
        System.out.println("Solution graph size is: " + constraintG.size());
        System.out.println("Solution used time: "+usedTime);
    }

}