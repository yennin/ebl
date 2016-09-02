package info.patsch.ebl.books.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import info.patsch.ebl.books.Book;
import info.patsch.ebl.books.BookViewFragment;

public class BookSearchActivity extends AppCompatActivity implements BookSearchFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, BookSearchFragment.newInstance()).commit();
        }
    }


    @Override
    public void onBookSelected(Book book) {
        Intent resultData = new Intent();
        resultData.putExtra(Book.BOOK_TAG, book);
        setResult(Activity.RESULT_OK, resultData);
        finishActivity(BookViewFragment.SEARCH_BOOK_REQUEST);
        finish();
    }
}
