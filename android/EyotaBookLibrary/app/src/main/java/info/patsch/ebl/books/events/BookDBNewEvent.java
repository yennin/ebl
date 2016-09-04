package info.patsch.ebl.books.events;

import info.patsch.ebl.books.Book;

public class BookDBNewEvent extends AbstractBookEvent {
    public BookDBNewEvent(Book book) {
        super(book);
    }
}
