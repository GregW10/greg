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
    public Pair(Pair<? extends K, ? extends V> p) {
        this.first = p.first;
        this.second = p.second;
    }
    @Override
    public String toString() {
        return "Pair(" + (first instanceof String || first instanceof StringBuilder? "\"" + first + "\"" : first) +
                ", " + (second instanceof String || second instanceof StringBuilder ? "\"" + second + "\"" : second) +
                ")";
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