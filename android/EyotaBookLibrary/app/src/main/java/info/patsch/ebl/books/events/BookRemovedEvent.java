package info.patsch.ebl.books.events;

import info.patsch.ebl.books.Book;

public class BookRemovedEvent extends AbstractBookEvent {

    public BookRemovedEvent(Book book) {
        super(book);
    }
}
