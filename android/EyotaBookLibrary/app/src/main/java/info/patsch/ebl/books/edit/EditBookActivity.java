package info.patsch.ebl.books.edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import info.patsch.ebl.books.Book;

public class EditBookActivity extends AppCompatActivity implements EditBookFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Book book = getIntent().getExtras().getParcelable(Book.BOOK_TAG);

        if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
            EditBookFragment bookFragment = EditBookFragment.newInstance(book);
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, bookFragment).commit();
        }
    }

    @Override
    public void onBookUpdated(Book book) {
        Intent resultData = new Intent();
        resultData.putExtra(Book.BOOK_TAG, book);
        setResult(Activity.RESULT_OK, resultData);
    }
}
