package info.patsch.ebl.books.events;

import info.patsch.ebl.books.Book;

/**
 * Created by patsch on 03.09.16.
 */
public class BookDBUpdateEvent extends AbstractBookEvent {

    public BookDBUpdateEvent(Book book) {
        super(book);
    }
}
