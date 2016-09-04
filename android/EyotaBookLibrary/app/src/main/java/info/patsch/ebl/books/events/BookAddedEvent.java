package info.patsch.ebl.books.events;

import info.patsch.ebl.books.Book;

/**
 * Created by patsch on 03.09.16.
 */
public class BookAddedEvent extends AbstractBookEvent {

    public BookAddedEvent(Book book) {
        super(book);
    }
}
