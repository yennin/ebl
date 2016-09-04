package info.patsch.ebl.books.google;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleBooksService {
    @GET("/books/v1/volumes?")
    Call<BookSearchResult> searchBooks(@Query("q") String query, @Query("key") String apiKey);
}
