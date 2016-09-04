package info.patsch.ebl.books.events;

import java.util.Set;

import info.patsch.ebl.books.Book;

/**
 * Created by patsch on 03.09.16.
 */
public class BooksLoadedEvent {
    private Set<Book> books;

    public BooksLoadedEvent(Set<Book> books) {
        this.books = books;
    }

    public Set<Book> getBooks() {
        return books;
    }

}
