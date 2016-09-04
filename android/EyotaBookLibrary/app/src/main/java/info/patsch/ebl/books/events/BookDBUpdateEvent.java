package info.patsch.ebl.books.events;

import info.patsch.ebl.books.Book;

public class BookDBUpdateEvent extends AbstractBookEvent {

    public BookDBUpdateEvent(Book book) {
        super(book);
    }
}
