package game.vector;

import java.util.Vector;

public class MyVector<T> {

    /**
     * Vektor pro ukladani.
     */
    public Vector<T> vector;

    /**
     * Vytvori novy vektor.
     */
    public MyVector() {
        vector = new Vector<>();
    }

    /**
     * Prida prvek do vektoru.
     */
    public synchronized void add(T type) {
        vector.add(type);
        notify();
    }

    /**
     * Prida prvek na dany index.
     */
    public synchronized void addToIndex(int index, T type) {
        vector.add(index, type);
        notify();
    }

    /**
     * Odebere prvek z vektru na danem indexu.
     */
    public synchronized void remove(int index) throws InterruptedException {
        while(vector.isEmpty()) {
            wait();
        }

        vector.removeElementAt(index);
        notify();
    }

    /**
     * Vrati prvek na danem indexu.
     */
    public synchronized T elementAt(int index) throws InterruptedException {
        while(vector.isEmpty()) {
            wait();
        }

        T type = vector.elementAt(index);
        notify();
        return type;
    }

    /**
     * Odstrani vsechny prvky vektoru.
     */
    public synchronized void removeAllElements() throws InterruptedException {
        vector.removeAllElements();
        notify();
    }

    /**
     * Vrati pocet prvku vektoru.
     */
    public int size() {
        return vector.size();
    }
}
