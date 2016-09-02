package info.patsch.ebl.books.search;

import java.util.List;

import info.patsch.ebl.books.Book;

public interface OnSearchResultListener {

        void onBooksFound(List<Book> books);
        void onNoResults();
    }
