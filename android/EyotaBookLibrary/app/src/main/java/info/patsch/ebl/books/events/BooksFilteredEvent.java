package info.patsch.ebl.books.events;

public class BooksFilteredEvent {
    private int count = 0;

    public BooksFilteredEvent(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
