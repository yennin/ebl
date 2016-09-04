package info.patsch.ebl.books.search;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import info.patsch.ebl.R;
import info.patsch.ebl.RecyclerViewFragment;
import info.patsch.ebl.books.Book;
import info.patsch.ebl.books.BookController;
import info.patsch.ebl.books.ffsearch.FFSearch;
import info.patsch.ebl.books.google.BookItems;
import info.patsch.ebl.books.google.BookSearchResult;
import info.patsch.ebl.books.google.GoogleBooksService;
import info.patsch.ebl.books.google.VolumeInfo;
import info.patsch.ebl.scanner.IntentIntegrator;
import info.patsch.ebl.scanner.IntentResult;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class BookSearchFragment extends RecyclerViewFragment
        implements Callback<BookSearchResult>, SearchEntryDialogFragment.OnDialogClosedListener, OnSearchResultListener, BookController.OnBookSelectedListener {

    private static final String TAG = "BookSearchFragment";
    private final static String[] ISBN_FORMATS = {"UPC_A", "EAN_13"};

    private OnFragmentInteractionListener mListener;

    private GoogleBooksService mBooksService = null;
    private String mGoogleApiKey = null;

    private SortedList<Book> model = null;
    private BookAdapter mAdapter;

    private ProgressDialog mProgressDialog = null;

    public static BookSearchFragment newInstance() {
        BookSearchFragment fragment = new BookSearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mGoogleApiKey = getString(R.string.google_api_key);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        mBooksService = retrofit.create(GoogleBooksService.class);

        mAdapter = new BookAdapter();
        model = new SortedList<>(Book.class, sortCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_book_search, container, false);
    }

    public RecyclerView getRecyclerView() {
        return ((RecyclerView) getView().findViewById(R.id.search_results));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLayoutManager(new LinearLayoutManager(getActivity()));
        setAdapter(mAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_books, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan:
                new IntentIntegrator(this).initiateScan();
                return true;
            case R.id.manual:
                SearchEntryDialogFragment dialogFragment = new SearchEntryDialogFragment();
                dialogFragment.setOnDialogClosedListener(this);
                dialogFragment.show(getFragmentManager(), "searchDialog");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onActivityResult(int request, int result, Intent intent) {
        mProgressDialog.dismiss();;
        IntentResult scan = IntentIntegrator.parseActivityResult(request, result, intent);
        if (scan != null && scan.getFormatName() != null) {
            String formatName = scan.getFormatName();
            for (String ISBN_FORMAT : ISBN_FORMATS) {
                if (ISBN_FORMAT.equals(formatName)) {

                    executeSearch(scan.getContents());
                }
            }
            String message = "Found type: %s with value: %s";
            message = String.format(message, formatName, scan.getContents());
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void executeSearch(String text) {
        String query = getString(R.string.isbn_query, text);
        mProgressDialog = startSearching();
        mBooksService.searchBooks(text, mGoogleApiKey).enqueue(this);
    }

    @Override
    public void onResponse(Call<BookSearchResult> call, Response<BookSearchResult> response) {
        mProgressDialog.dismiss();
        if (response.isSuccessful()) {
            final BookSearchResult searchResult = response.body();
            List<Book> books = new ArrayList<>();
            for (BookItems bookItem : searchResult.getItems()) {
                Book book = convert(bookItem);
                books.add(book);
            }
            onBooksFound(books);
        } else {
            ResponseBody responseBody = response.errorBody();
            onNoResults();
        }
    }

    private Book convert(BookItems bookItem) {
        VolumeInfo volumeInfo = bookItem.getVolumeInfo();

        Book book = new Book();
        book.setYear(getYear(volumeInfo.getPublishedDate()));
        book.setAuthorName(join(volumeInfo.getAuthors(), ", "));
        book.setBook(true);
        book.setImageLoc(volumeInfo.getImageLinks().getImageUrl());
        book.setTitle(volumeInfo.getTitle());

        return book;
    }

    private String getImage(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return null;
        }
        Bitmap bitmap;
        try {
            bitmap = Picasso.with(getActivity()).load(imageUrl).get();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.URL_SAFE);
        } catch (Downloader.ResponseException e) {
            Log.w(TAG, e.getMessage(), e);
            return null;
        } catch (IOException e) {
            Log.w(TAG, e.getMessage(), e);
            return null;
        } catch (Exception ex) {
            Log.w(TAG, ex.getMessage(), ex);
            return null;
        }
    }

    private int getYear(String date) {
        if (TextUtils.isEmpty(date)) {
            return 0;
        }
        String year = date.substring(0, 4);
        try {
            return Integer.parseInt(year);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String join(List<String> in, String separator) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iter = in.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (iter.hasNext()) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }


    @Override
    public void onFailure(Call call, Throwable t) {
        mProgressDialog.dismiss();;
        Toast.makeText(getActivity(), t.getMessage(),
                Toast.LENGTH_LONG).show();
        Log.e(getClass().getSimpleName(),
                "Exception from Retrofit request", t);
        onNoResults();
    }

    @Override
    public void onSearchClicked(String query) {
        mProgressDialog = startSearching();
        FFSearch search = new FFSearch(getActivity(), this);
        search.searchBook(query);
    }

    // RecyclerView
    private SortedList.Callback<Book> sortCallback = new SortedList.Callback<Book>() {
        @Override
        public int compare(Book o1, Book o2) {
            return o1.compareTo(o2);
        }

        @Override
        public boolean areContentsTheSame(Book oldItem, Book newItem) {
            return (areItemsTheSame(oldItem, newItem));
        }

        @Override
        public boolean areItemsTheSame(Book oldItem, Book newItem) {
            return (compare(oldItem, newItem) == 0);
        }

        @Override
        public void onInserted(final int position, final int count) {
            mAdapter.notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(final int position, final int count) {
            mAdapter.notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(final int fromPosition, final int toPosition) {
            mAdapter.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(final int position, final int count) {
            mAdapter.notifyItemRangeChanged(position, count);
        }
    };

    @Override
    public void onBooksFound(final List<Book> books) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                for (Book book : books) {
                    if (book.getImageEncoded() == null && book.getImageLoc() != null) {
                        book.setImageEncoded(getImage(book.getImageLoc()));
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void nothing) {
                mAdapter.replaceAll(books);
                mAdapter.notifyDataSetChanged();
                mProgressDialog.dismiss();
            }
        };

        asyncTask.execute();
    }

    @Override
    public void onNoResults() {
        mProgressDialog.dismiss();
        Toast.makeText(getActivity(), R.string.no_results, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBookSelected(Book book) {
        mListener.onBookSelected(book);
    }

    class BookAdapter extends RecyclerView.Adapter<BookController> {
        private List<Book> results = null;

        public BookAdapter() {
            results = new ArrayList<>();
        }

        @Override
        public BookController onCreateViewHolder(ViewGroup parent, int viewType) {
            return (new BookController(getActivity().getLayoutInflater()
                    .inflate(R.layout.book_row, parent, false), null, BookSearchFragment.this));
        }

        @Override
        public void onBindViewHolder(BookController holder, int position) {
            holder.bindModel(results.get(position));
        }

        @Override
        public int getItemCount() {
            return (results.size());
        }

        public void add(Book book) {
            model.beginBatchedUpdates();
            results.add(book);
            model.add(book);
            model.endBatchedUpdates();
        }

        public void replaceAll(List<Book> books) {
            model.beginBatchedUpdates();
            results.clear();
            results.addAll(books);
            model.clear();
            model.addAll(books);
            model.endBatchedUpdates();
        }
    }

    private ProgressDialog startSearching() {
        ProgressDialog progress = new ProgressDialog(getContext());
        progress.setTitle("Searching");
        progress.setMessage("Wait while searching...");
        progress.show();
        return progress;
    }

    public interface OnFragmentInteractionListener {

        void onBookSelected(Book book);
    }
}
