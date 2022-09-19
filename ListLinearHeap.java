package kcore;
import java.util.HashMap;

public class ListLinearHeap {
    private final int n;
    private final int key_cap;
    private int max_key;
    private int min_key;
    int[] keys;
    int[] heads;
    int[] pres;
    int[] nexts;

    public ListLinearHeap(int n, int key_cap, int[] ids, int[] keys) {
        this.n = n;
        this.key_cap = key_cap;
        this.max_key = 0;
        this.min_key = key_cap;
        this.keys = null;
        this.heads = null;
        this.pres = null;
        this.nexts = null;
        if (this.keys == null) {
            this.keys = new int[n];
        }

        if (this.pres == null) {
            this.pres = new int[n];
        }

        if (this.nexts == null) {
            this.nexts = new int[n];
        }

        if (this.heads == null) {
            this.heads = new int[key_cap + 1];
        }

        this.min_key = key_cap;
        this.max_key = 0;

        int i;
        for(i = 0; i <= key_cap; ++i) {
            this.heads[i] = n;
        }

        for(i = 0; i < n; ++i) {
            this.insert(ids[i], keys[i]);
        }

    }

    public void insert(int id, int key) {
        this.keys[id] = key;
        this.pres[id] = this.n;
        this.nexts[id] = this.heads[key];
        if (this.heads[key] != this.n) {
            this.pres[this.heads[key]] = id;
        }

        this.heads[key] = id;
        if (key < this.min_key) {
            this.min_key = key;
        }

        if (key > this.max_key) {
            this.max_key = key;
        }

    }

    public int remove(int id) {
        if (this.pres[id] == this.n) {
            this.heads[this.keys[id]] = this.nexts[id];
            if (this.nexts[id] != this.n) {
                this.pres[this.nexts[id]] = this.n;
            }
        } else {
            int pid = this.pres[id];
            this.nexts[pid] = this.nexts[id];
            if (this.nexts[id] != this.n) {
                this.pres[this.nexts[id]] = pid;
            }
        }

        return this.keys[id];
    }

    public int getN() {
        return this.n;
    }

    public int getKey_cap() {
        return this.key_cap;
    }

    public int getKey(int id) {
        return this.keys[id];
    }

    public boolean empty() {
        this.tighten();
        return this.min_key > this.max_key;
    }

    public HashMap pop_min() {
        if (this.empty()) {
            return null;
        } else {
            HashMap map = new HashMap();
            map.put(this.heads[this.min_key], this.min_key);
            this.heads[this.min_key] = this.nexts[this.heads[this.min_key]];
            if (this.heads[this.min_key] != this.n) {
                this.pres[this.heads[this.min_key]] = this.n;
            }

            return map;
        }
    }

    public int decrement(int id) {
        int dec = 1;
        int new_key = this.keys[id] - dec;
        this.remove(id);
        this.insert(id, new_key);
        return new_key;
    }

    private void tighten() {
        while(this.min_key <= this.max_key && this.heads[this.min_key] == this.n) {
            ++this.min_key;
        }

        while(this.min_key <= this.max_key && this.heads[this.max_key] == this.n) {
            --this.max_key;
        }

    }
}
