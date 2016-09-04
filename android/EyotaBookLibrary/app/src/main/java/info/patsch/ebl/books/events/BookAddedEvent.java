package info.patsch.ebl.books.events;

import info.patsch.ebl.books.Book;

public class BookAddedEvent extends AbstractBookEvent {

    public BookAddedEvent(Book book) {
        super(book);
    }
}
