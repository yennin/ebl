package info.patsch.ebl.books.ffsearch;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import info.patsch.ebl.R;
import info.patsch.ebl.books.Book;
import info.patsch.ebl.books.search.OnSearchResultListener;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by patsch on 02.09.16.
 */
public class FFSearch implements Callback<ResponseBody> {

    private Context mContext = null;
    private OnSearchResultListener mListener;

    public FFSearch(Context context, OnSearchResultListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public void searchBook(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mContext.getString(R.string.ff_url))
                .build();
        FFBooksService booksService = retrofit.create(FFBooksService.class);
        booksService.searchBooks(query).enqueue(this);
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        try {
            ResponseBody body = response.body();
            String data = body.string();
            int start = data.indexOf("{");
            int end = data.lastIndexOf("}");
            if (start < 0 || end < 0) {
                notifyNoResult();
            }
            else {
                data = data.substring(start, end + 1);

                ObjectMapper objectMapper = new ObjectMapper();
                SearchResult result = objectMapper.readValue(new StringReader(data), SearchResult.class);
                if (result.hits.found > 0) {
                    notifyResults(convert(result.hits.hit));
                } else {
                    notifyNoResult();
                }
            }
        } catch (IOException ex) {
            Log.w("FFSearch", "Failed to load searchresult", ex);
            notifyNoResult();
        }
    }

    private void notifyResults(List<Book> books) {
        if (mListener != null) {
            mListener.onBooksFound(books);
        }
    }

    private void notifyNoResult() {
        if (mListener != null) {
            mListener.onNoResults();
        }
    }

    private List<Book> convert(List<Hit> hits) {
        List<Book> foundBooks = new ArrayList<>();
        for (Hit hit : hits) {
            Book book = new Book();
            book.setHasImage(hasImage(hit.getImageloc(), hit.getImageurlAmazon()));
            book.setImageLoc(buildImageLoc(hit.getImageloc(), hit.getImageurlAmazon()));
            book.setPfn(mContext.getString(R.string.ff_url) + "/" + hit.getPfn());
            book.setSeriesName(hit.getSeriesName());
            book.setSeriesNumber(hit.getSeriesNumber());
            book.setTitle(hit.getTitle());
            book.setYear(Integer.parseInt(hit.getYear()));
            book.setAuthorUrl(mContext.getString(R.string.ff_url) + "/" + hit.getAuthorUrl());
            book.setAuthorName(hit.getAuthorName());
            foundBooks.add(book);
        }
        return foundBooks;
    }

    private boolean hasImage(String imageloc, String imgurlAmazon) {
        return imageloc != null || imgurlAmazon != null;
    }

    private String buildImageLoc(String imageloc, String imgurlAmazon) {
        if (!hasImage(imageloc, imgurlAmazon)) {
            return null;
        }
        return imgurlAmazon == null ? mContext.getString(R.string.ff_img_url) + "/" + imageloc : imgurlAmazon;
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        Log.w("FFSearch", "Failed to load searchresult", t);
        notifyNoResult();
    }
}
