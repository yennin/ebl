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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import info.patsch.ebl.R;
import info.patsch.ebl.books.Book;
import info.patsch.ebl.books.search.BookSearchActivity;
import info.patsch.ebl.databinding.FragmentEditBookBinding;


public class EditBookFragment extends Fragment {

    private static final String TAG = "BookEditFragment";
    private static final String ARG_BOOK = "book";

    private OnFragmentInteractionListener mListener;

    private Book mBook = null;

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

        setRetainInstance(true);

        if (getArguments() != null) {
            mBook = getArguments().getParcelable(ARG_BOOK);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentEditBookBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_book, container, false);

        initDatabinding(dataBinding);
        return dataBinding.getRoot();
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
                } else {
                    try {
                        int year = Integer.parseInt(charSequence.toString());
                        mBook.setYear(year);
                    } catch (NumberFormatException ex) {
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
        } else {
            mPictureView.setBackgroundResource(R.drawable.border);
        }
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
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_ok:
                if (validate(mBook)) {
                    if (mListener != null) {
                        mListener.onReturnResult(mBook);
                    }
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validate(Book book) {
        if (TextUtils.isEmpty(book.getTitle())) {
            Toast.makeText(getActivity(), R.string.validation_error_title, Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(book.getAuthorName())) {
            Toast.makeText(getActivity(), R.string.validation_error_author, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public interface OnFragmentInteractionListener {
        void onReturnResult(Book book);
    }
}
