package greg.misc;

public class Trio<K, V, U> extends Pair<K, V> {
    public U third;
    public Trio() {
        super();
        third = null;
    }
    public Trio(K first_element) {
        super(first_element);
    }
    public Trio(K first_element, V second_element) {
        super(first_element, second_element);
    }
    public Trio(K first_element, V second_element, U third_element) {
        super(first_element, second_element);
        third = third_element;
    }
    public Trio(Trio<? extends K, ? extends V, ? extends U> t) {
        this.first = t.first;
        this.second = t.second;
        this.third = t.third;
    }
    public Trio(K first_element, Pair<? extends V, ? extends U> p) {
        this.first = first_element;
        this.second = p.first;
        this.third = p.second;
    }
    public Trio(Pair<? extends K, ? extends V> p, U third_element) {
        this.first = p.first;
        this.second = p.second;
        this.third = third_element;
    }
    @Override
    public String toString() {
        return super.toString() + "\b, " + (third instanceof String || third instanceof StringBuilder ? "\"" + third +
                "\"" : third) + ")";
    }
    @Override
    public boolean equals(Object other) {
        if (other.getClass() != Trio.class) {
            return super.equals(other);
        }
        return this.third.equals(((Trio<?, ?, ?>) other).third);
    }
    @Override
    public void clear() {
        super.clear();
        third = null;
    }
}