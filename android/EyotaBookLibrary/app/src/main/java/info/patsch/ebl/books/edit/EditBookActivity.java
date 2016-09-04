package info.patsch.ebl.books.edit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;

import info.patsch.ebl.books.Book;
import info.patsch.ebl.books.events.BookDBUpdateEvent;

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
    public void onReturnResult(Book book) {
        EventBus.getDefault().postSticky(new BookDBUpdateEvent(book));
        finish();
    }
}
