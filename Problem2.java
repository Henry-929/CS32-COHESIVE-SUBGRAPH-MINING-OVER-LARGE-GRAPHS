package kcore.decomposition;


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
//        System.out.println("G: "+G);
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
//                    System.out.println(list.get(i) + " " + list.get(j));

                    return false;
                }
            }
        }
        return true;

    }

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
//        for (int j=pstart[targetNode]; j<pstart[targetNode+1];j++){
//            if (core[edges[j]] == 0)
//                linearHeap.decrement(edges[j]);
//        }
//         remove the edges
        Set<Integer> integers = currentGraph.keySet();
        for (Integer integer : integers) {
            if (currentGraph.get(integer).contains(targetNode)) {
                currentGraph.get(integer).remove(targetNode);
//                if (currentGraph.get(integer).size() == 1) {
//                    deleteNode(integer, currentGraph);
//                } else {
//                    currentGraph.get(integer).remove(targetNode);
//
//                }
            }
        }

        System.out.println(targetNode + " is deleted this time.");
}


    public static void main(String[] args) throws FileNotFoundException {
        long startTime = System.currentTimeMillis();

        Problem2 search = new Problem2();
        //不要再写绝对路径了！！
        Map<Integer, Set<Integer>> G = search.loadGraph("data/toy1.txt");
        ArrayList<Integer> list = search.loadQueryNode("data/QD1.txt");
        System.out.println("Query list: " + list);

        Stack<Integer> seq = new Stack<Integer>();
        search.coreDecompositionLinearList(G);
//        System.out.println("The peeling sequence, i.e., degeneracy order (即：每个点被删除的顺序)：");
//        for (int i = 0; i < n; i++)
//            System.out.print(peer_seq[i] + ", ");
        for (int j = 0; j < n; j++) {
            if (search.checkConnection(list, G)) {
                search.deleteNode(peer_seq[j], G);  //按顺序删除节点
                seq.push(peer_seq[j]); //把删除节点加入seq stack里
            } else {
                System.out.println("Query nodes is no long connected! stop!");
                break;
            }
        }


        System.out.println("G now: " + G);
        ArrayList<Integer> solution = new ArrayList<Integer>(G.keySet());
        solution.add(seq.pop());
        System.out.println("Solution graph vertices included: " + solution);

//        ArrayList<Integer> output = new ArrayList<Integer>();
//
//        for(int j=0;j<solution.size();j++){
//            for (Integer i: list) {
//                if(G.get(solution.get(j)).contains(i)){
//                    output.add(solution.get(j));
//                }
//
//            }
//        }
//
//        System.out.println("Solution graph vertices included: " + output);


        long endTime = System.currentTimeMillis();
        long usedTime = endTime - startTime;
        System.out.println("used time: " + usedTime);

    }
}
