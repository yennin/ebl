package info.patsch.ebl.books.events;

import info.patsch.ebl.books.Book;

/**
 * Created by patsch on 03.09.16.
 */
public class BookDBRemoveEvent extends AbstractBookEvent {

    public BookDBRemoveEvent(Book book) {
        super(book);
    }
}
