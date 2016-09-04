package info.patsch.ebl.books.ffsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collection;

import info.patsch.ebl.books.Book;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchStatus {
    public Collection<Book> books;
    public int totalResults;

    public SearchStatus(Collection<Book> books, int found) {
        this.books = books;
        this.totalResults = found;
    }

    public boolean hasResults() {
        return totalResults > 0;
    }

    public int getAvailableResults() {
        return hasResults() ? books.size() : 0;
    }
}
