package info.patsch.ebl.books.events;

import info.patsch.ebl.books.Book;

public class BookDBRemoveEvent extends AbstractBookEvent {

    public BookDBRemoveEvent(Book book) {
        super(book);
    }
}
