package info.patsch.ebl.books;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by patsch on 05.09.16.
 */
public enum BooksHolder {

    INSTANCE;

    // instance vars, constructor
    private final Set<Book> mBooks = new HashSet<>();
    private boolean initialized = false;

    BooksHolder() {
    }

    public Set<Book> getBooks() {
        return mBooks;
    }

    public boolean add(Book book) {
        return this.mBooks.add(book);
    }

    public boolean remove(Book book) {
        return this.mBooks.remove(book);
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }


    public int size() {
        return this.mBooks.size();
    }


    public boolean checkExisting(Book newBook) {
        ExistingBookFilter filter = new ExistingBookFilter(newBook);
        for (Book book : mBooks) {
            if (filter.accept(book)) {
                return true;
            }
        }
        return false;
    }

    private static class ExistingBookFilter {
        private Book newBook;

        public ExistingBookFilter(Book newBook) {
            this.newBook = newBook;
        }

        public boolean accept(Book testBook) {
            return newBook.getTitle().equals(testBook.getTitle())
                    && newBook.getAuthorName().equals(testBook.getAuthorName())
                    && (newBook.getSeriesName() != null ? newBook.getSeriesName().equals(testBook.getSeriesName()) : testBook.getSeriesName() == null);
        }
    }
}
