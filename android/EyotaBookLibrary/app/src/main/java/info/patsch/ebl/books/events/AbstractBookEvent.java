package info.patsch.ebl.books.events;

import info.patsch.ebl.books.Book;

public abstract class AbstractBookEvent {
    private Book mBook;

    public AbstractBookEvent(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book must not be null");
        }
        this.mBook = book;
    }

    public Book getBook() {
        return mBook;
    }
}
