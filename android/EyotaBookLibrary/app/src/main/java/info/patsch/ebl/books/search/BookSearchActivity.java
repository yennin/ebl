package info.patsch.ebl.books.search;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;

import info.patsch.ebl.books.Book;
import info.patsch.ebl.books.events.BookDBNewEvent;

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
        EventBus.getDefault().postSticky(new BookDBNewEvent(book));
        finish();
    }
}
