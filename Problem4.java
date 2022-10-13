package kcore.decomposition;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class Problem4 {
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
                    System.out.println(list.get(i) + " " + list.get(j));

                    return false;
                }
            }
        }
        return true;

    }

    /**
     * 从当前y步骤时图Gy中删除节点v以及相关边的关系
     * @param targetNode the node to be deleted
     * @param currentGraph the current graph
     */
    public void deleteNode(int targetNode, Map<Integer, Set<Integer>> currentGraph) {

        ListLinearHeap linearHeap = new ListLinearHeap(n,n-1,peer_seq,degree);
        //remove the nodes
        currentGraph.remove(targetNode);
        linearHeap.remove(targetNode);
//        delete degree
        for (int j=pstart[targetNode]; j<pstart[targetNode+1];j++){
            if (core[edges[j]] == 0)
                linearHeap.decrement(edges[j]);
        }
        // remove the edges
        Set<Integer> integers = currentGraph.keySet();
        for (Integer integer : integers) {
            if (currentGraph.get(integer).contains(targetNode)) {
                if (currentGraph.get(integer).size() == 1) {
                    deleteNode(integer, currentGraph);
                } else {
                    currentGraph.get(integer).remove(targetNode);

                }
            }
        }

        System.out.println(targetNode + " is deleted this time.");
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

    /**
     * 获取距离一个搜索节点query node最远距离的节点和距离
     * @param queryN 一个搜索节点query node
     * @param G 当前图G
     * @return 返回值获取可看main中调用案例
     */
    public HashMap<String, Integer> getMaxDistance(int queryN, Map<Integer, Set<Integer>> G){
        HashMap<String, Integer> map = new HashMap<>();
        int array[] = new int[G.size()];
        int sort[] = new int[G.size()];

        for (Integer v : G.keySet()){
            int disV = getDistance(queryN, v, G);
            if (disV == -1){
                disV = Integer.MAX_VALUE;
            }
            array[v] = disV;
        }
        map.put("queryNode", queryN);
        for (int i=0;i<sort.length;i++){
            sort[i] = array[i];
        }
        mergeSort(sort, 0, array.length-1);

        for (int i=0;i<array.length;i++){
            if (array[i] == sort[array.length-1]){
                map.put("node", i);
                map.put("maxDist", sort[array.length-1]);
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

    public static void main(String[] args) throws FileNotFoundException {
        long startTime =  System.currentTimeMillis();

        Problem4 search = new Problem4();
        Map<Integer, Set<Integer>> G = search.loadGraph("/Users/rxia/Desktop/COMP5703  Capstone/code/CS32-COHESIVE-SUBGRAPH-MINING-OVER-LARGE-GRAPHS/data/toy1.txt"); //change the Absolute path into Relative path
        ArrayList<Integer> list = search.loadQueryNode("/Users/rxia/Desktop/COMP5703  Capstone/code/CS32-COHESIVE-SUBGRAPH-MINING-OVER-LARGE-GRAPHS/data/QD1.txt");
        System.out.println(list);

//        search.coreDecompositionLinearList(G);

//        int distance = search.getDistance(5, 6, G);
//        System.out.println("图中点0-3的距离为 "+distance);
//        boolean distance1 = search.checkConnection(list, G);
//        System.out.println(distance1);

        HashMap<String, Integer> map = search.getMaxDistance(2, G);
        Integer queryNode = map.get("queryNode");
        Integer node = map.get("node");
        Integer maxDist = map.get("maxDist");
        System.out.println("query node 节点 "+queryNode+" 距离最远节点 "+node+" 的距离为 "+maxDist);

        int v = 0;
        search.deleteNode(v, G);
        System.out.println("G now after remove node "+v+": "+"\n"+G);
        int v2 = 1;
        search.deleteNode(v2, G);
        System.out.println("G now after remove node "+v2+": "+"\n"+G);


        long endTime =  System.currentTimeMillis();
        long usedTime = endTime-startTime;
        System.out.println("used time: "+usedTime);
    }

}
