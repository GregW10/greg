package greg.misc;

public class Pair <K, V> {
    public K first;
    public V second;
    public Pair() {
        first = null; second = null;
    }
    public Pair(K first_element) {
        first = first_element;
    }
    public Pair(K first_element, V second_element) {
        this.first = first_element; this.second = second_element;
    }
    @Override
    public String toString() {
        return "First: " + first + ", Second: " + second;
    }
    @Override
    public boolean equals(Object other) {
        if (other == null || other.getClass() != Pair.class) {
            return false;
        }
        Pair<?, ?> object = (Pair<?, ?>) other;
        return this.first.equals(object.first) && this.second.equals(object.second);
    }
    public void clear() {
        first = null;
        second = null;
    }
}