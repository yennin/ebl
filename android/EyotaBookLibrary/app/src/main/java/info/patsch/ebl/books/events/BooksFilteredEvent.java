package info.patsch.ebl.books.events;

/**
 * Created by patsch on 04.09.16.
 */
public class BooksFilteredEvent {
    private int count = 0;

    public BooksFilteredEvent(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
