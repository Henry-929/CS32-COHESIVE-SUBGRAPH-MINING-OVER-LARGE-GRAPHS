package kcore;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

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
        return G;
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
                    while (core[u] == core[u+1]){
                        u = u+1;
                        G.put(u,null);
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
     * 获取距离一个搜索节点query node最远距离的节点和距离
     * @param queryN 一个搜索节点query node
     * @param G 当前图G
     * @return 返回值获取可看main中调用案例
     */
    public HashMap<String, Integer> getMaxDistance(int queryN, Map<Integer, Set<Integer>> G){
        HashMap<String, Integer> map = new HashMap<>();
        int array[] = new int[n]; //用于存储每个节点距离查询节点的距离（index对应每个节点，value对应距离）
        int sort[] = new int[n];//用于存储排序后的距离值

        //遍历找到每个节点到查询节点的最短路径距离
        for (Integer v : G.keySet()){
            int disV = getDistance(queryN, v, G);//使用BSF算法，获取节点v到查询节点queryN 的最短路径距离
            if (disV == -1){
                //将不相连节点距离设置为最大值，方便排序
                disV = Integer.MAX_VALUE;
            }
            array[v] = disV;
        }
        //map.put("queryNode", queryN); //输出查询节点
        for (int i=0;i<sort.length;i++){
            sort[i] = array[i];
        }
        //进行归并排序
        mergeSort(sort, 0, array.length-1);

        for (int i=0;i<array.length;i++){
            if (array[i] == sort[array.length-1]){
                map.put("node", i);//输出当前图中距离查询节点queryN最远距离的节点
                //map.put("maxDist", sort[array.length-1]);//输出当前图中距离查询节点queryN最远距离的节点的距离
            }
        }
        return map;
    }

    /**
     * 两路归并算法，两个排好序的子序列合并为一个子序列
     */
    public void merge(int []a,int left,int mid,int right){
        int []tmp=new int[a.length];//辅助数组
        int p1=left,p2=mid+1,k=left;//p1、p2是检测指针，k是存放指针

        while(p1<=mid && p2<=right){
            if(a[p1]<=a[p2])
                tmp[k++]=a[p1++];
            else
                tmp[k++]=a[p2++];
        }

        while(p1<=mid) tmp[k++]=a[p1++];//如果第一个序列未检测完，直接将后面所有元素加到合并的序列中
        while(p2<=right) tmp[k++]=a[p2++];//同上

        //复制回原素组
        for (int i = left; i <=right; i++)
            a[i]=tmp[i];
    }

    /**
     * 归并排序
     */
    public void mergeSort(int [] a,int start,int end){
        if(start<end){//当子序列中只有一个元素时结束递归
            int mid=(start+end)/2;//划分子序列
            mergeSort(a, start, mid);//对左侧子序列进行递归排序
            mergeSort(a, mid+1, end);//对右侧子序列进行递归排序
            merge(a, start, mid, end);//合并
        }
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
        while(checkConnection(list, G) && checkSizeConstraint(sizeConstraint,G)){
            for(Integer i: list ){
                HashMap<String, Integer> map = getMaxDistance(i, G);
                //Integer queryNode = map.get("queryNode");
                Integer node = map.get("node");
                //Integer maxDist = map.get("maxDist");
                //System.out.println("query node 节点 "+queryNode+" 距离最远节点 "+node+" 的距离为 "+maxDist);

                deleteNode(node, G);  //删除最远距离node
                if (!checkSizeConstraint(sizeConstraint,G))
                    return G;
            }
        }
        return G;
    }


    public static void main(String[] args) throws FileNotFoundException {
        long startTime =  System.currentTimeMillis();

        Problem4 search = new Problem4();
        Map<Integer, Set<Integer>> G = search.loadGraph("data/toy1.txt");
        ArrayList<Integer> list = search.loadQueryNode("data/QD1.txt");

        Map<Integer, Set<Integer>> maxMinDGraph = search.findMaxMinD(G, list);

        int sizeConstraint = 4;
        Map<Integer, Set<Integer>> constraintG = search.findConstraintG(maxMinDGraph, sizeConstraint, list);

        //System.out.println("G now: "+constraintG);
        System.out.println("Solution graph vertices included: "+ constraintG.keySet());

        long endTime =  System.currentTimeMillis();
        long usedTime = endTime-startTime;
        System.out.println("used time: "+usedTime);
    }

}