package info.patsch.ebl.books.ffsearch;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FFBooksService {
    @GET("/db-search/v4/books/?start=0&size=50&return-fields=booktype,title,year,pfn,hasimage,authorsinfo,seriesinfo,db,imageloc,imageurl_amazon")
    Call<ResponseBody> searchBooks(@Query("q") String query);
}
