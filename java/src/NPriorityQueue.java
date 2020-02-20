import java.util.PriorityQueue;

public class NPriorityQueue<E extends Comparable<E>> {
    int size;
    int capacity;
    PriorityQueue<E> pq;

    NPriorityQueue(int capacity) {
        size = 0;
        this.capacity = capacity;
        pq = new PriorityQueue<>(capacity);
    }

    public void add(E e) {
        if (size < capacity) {
            pq.add(e);
            size++;
            return;
        }
        if (pq.peek().compareTo(e) > 0) {
            pq.poll();
            pq.add(e);
        }
    }

    public E poll() {
        size--;
        return pq.poll();
    }
}
