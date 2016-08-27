package info.patsch.ebl.books.edit;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

import info.patsch.ebl.R;
import info.patsch.ebl.books.Book;
import info.patsch.ebl.books.google.BookSearchResult;
import info.patsch.ebl.books.google.GoogleBooksService;
import info.patsch.ebl.databinding.FragmentEditBookBinding;
import info.patsch.ebl.scanner.IntentIntegrator;
import info.patsch.ebl.scanner.IntentResult;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;


public class EditBookFragment extends Fragment implements Callback<BookSearchResult> {

    private static final String TAG = "BookEditFragment";
    private static final String ARG_BOOK = "book";

    private final static String[] ISBN_FORMATS = { "UPC_A", "EAN_13" };

    private OnFragmentInteractionListener mListener;
    private Book mBook = null;
    private View base = null;
    private GoogleBooksService service = null;
    private String apiKey = null;

    private ImageView mPictureView;

    public static EditBookFragment newInstance(Book book) {
        EditBookFragment fragment = new EditBookFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_BOOK, book);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBook = getArguments().getParcelable(ARG_BOOK);
        }
        apiKey = getString(R.string.google_api_key);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentEditBookBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_book, container, false);

        initDatabinding(dataBinding);
        base = dataBinding.getRoot();

        ImageButton scanButton = (ImageButton) base.findViewById(R.id.scan);
        scanButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                (new IntentIntegrator(EditBookFragment.this)).initiateScan();

            }
        });

        return base;
    }

    private void initDatabinding(final FragmentEditBookBinding dataBinding) {
        dataBinding.setBook(mBook);
        mPictureView = dataBinding.picture;
        setImage(mBook);

        dataBinding.yearInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(charSequence)) {
                    mBook.setYear(0);
                }
                else {
                    try {
                        int year = Integer.parseInt(charSequence.toString());
                        mBook.setYear(year);
                    }
                    catch (NumberFormatException ex) {
                        dataBinding.yearInput.setError(getString(R.string.wrong_year_format));
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // do nothing
            }
        });

    }

    private void setImage(Book book) {
        if (book.getImageEncoded() != null) {
            byte[] image = Base64.decode(book.getImageEncoded(), Base64.URL_SAFE);
            Bitmap bMap = BitmapFactory.decodeByteArray(image, 0, image.length);
            mPictureView.setImageBitmap(bMap);
            mPictureView.setBackground(null);
        }
        else {
            mPictureView.setBackgroundResource(R.drawable.border);
        }
    }

    public void onActivityResult(int request, int result, Intent intent) {

        IntentResult scan=IntentIntegrator.parseActivityResult(request, result, intent);
        if (scan != null && scan.getFormatName() != null) {
            String formatName = scan.getFormatName();
            for (int i = 0; i < ISBN_FORMATS.length; i++) {
                if (ISBN_FORMATS[i].equals(formatName)) {
                    // search books
                    String query = getString(R.string.isbn_query, scan.getContents());
                    service.searchBooks(scan.getContents(), apiKey).enqueue(this);

                }
            }
            String message = "Found type: %s with value: %s";
            message = String.format(message, formatName, scan.getContents());
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.googleapis.com")
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();

            service = retrofit.create(GoogleBooksService.class);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResponse(Call<BookSearchResult> call, Response<BookSearchResult> response) {
        if (response.isSuccessful()) {
            BookSearchResult searchResult = response.body();
            searchResult.getTotalItems();


        }
        else {
            ResponseBody responseBody = response.errorBody();
            try {
                Log.e(TAG, responseBody.string());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public void onFailure(Call<BookSearchResult> call, Throwable t) {
        Toast.makeText(getActivity(), t.getMessage(),
                Toast.LENGTH_LONG).show();
        Log.e(getClass().getSimpleName(),
                "Exception from Retrofit request to StackOverflow", t);
    }

    public interface OnFragmentInteractionListener {
        void onBookUpdated(Book book);
    }
}
