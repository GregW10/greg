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
    @Override
    public String toString() {
        return super.toString() + ", Third: " + third;
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