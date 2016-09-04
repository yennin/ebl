package info.patsch.ebl.books.events;

import info.patsch.ebl.books.Book;

/**
 * Created by patsch on 03.09.16.
 */
public class BookDBNewEvent extends AbstractBookEvent {
    public BookDBNewEvent(Book book) {
        super(book);
    }
}
