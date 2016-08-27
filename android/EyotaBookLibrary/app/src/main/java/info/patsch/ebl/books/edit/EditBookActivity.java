package info.patsch.ebl.books.edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import info.patsch.ebl.R;
import info.patsch.ebl.books.Book;

public class EditBookActivity extends AppCompatActivity implements EditBookFragment.OnFragmentInteractionListener {

    private Book book = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        book = getIntent().getExtras().getParcelable(Book.BOOK_TAG);

        if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
            EditBookFragment bookFragment = EditBookFragment.newInstance(book);
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, bookFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_ok:
                if (validate(book)) {
                    Intent resultData = new Intent();
                    resultData.putExtra(Book.BOOK_TAG, book);
                    setResult(Activity.RESULT_OK, resultData);
                    finish();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBookUpdated(Book book) {
        this.book = book;
    }

    private boolean validate(Book book) {
        if (TextUtils.isEmpty(book.getTitle())) {
            Toast.makeText(this, R.string.validation_error_title, Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(book.getAuthorName())) {
            Toast.makeText(this, R.string.validation_error_author, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
