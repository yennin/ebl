package info.patsch.ebl;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Set;

import info.patsch.ebl.books.Book;
import info.patsch.ebl.books.BookViewFragment;
import info.patsch.ebl.books.FilterConstants;
import info.patsch.ebl.books.ModelFragment;
import info.patsch.ebl.books.events.BooksFilteredEvent;
import info.patsch.ebl.books.events.BooksLoadedEvent;

;

public class MainActivity extends AppCompatActivity implements FilterConstants {

    private final static int RC_SIGN_IN = 27;

    private GoogleApiClient mClient;
    private ViewPager mViewPager;

    private TextView mBookCount;

    private static final String MODEL = "model";

    private ModelFragment model = null;

    ProgressDialog mProgressDialog = null;

    private static boolean isDbInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isDbInitialized) {
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                isDbInitialized = true;
            } catch (DatabaseException ex) {
                Log.w("MainActivity", ex.getMessage(), ex);
            }
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mBookCount = (TextView)findViewById(R.id.book_count_bar);
    }

    private void setupPager(Set<Book> books) {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), books);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mProgressDialog.dismiss();;
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        mProgressDialog = startLoading();

        if (model == null) {
            ModelFragment mfrag =
                    (ModelFragment) getSupportFragmentManager().findFragmentByTag(MODEL);

            if (mfrag == null) {
                mfrag = new ModelFragment();

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(mfrag, MODEL)
                        .commit();
            } else if (mfrag.getBooks() != null) {
                EventBus.getDefault().post(new BooksLoadedEvent(mfrag.getBooks()));
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBookLoaded(BooksLoadedEvent event) {
        setupPager(event.getBooks());
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onBooksFiltered(BooksFilteredEvent event) {
        if (event.getCount() > 0) {
            mBookCount.setText(getString(R.string.book_count, event.getCount()));
            mBookCount.setVisibility(View.VISIBLE);
        }
        else {
            mBookCount.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient.connect();

        // Default Database rules do not allow unauthenticated reads, so we need to
        // sign in before attaching the RecyclerView adapter otherwise the Adapter will
        // not be able to read any data from the Database.
        if (!isSignedIn()) {
            signIn();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://info.patsch.ebl/http/host/path")
        );
        AppIndex.AppIndexApi.start(mClient, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://info.patsch.ebl/http/host/path")
        );
        AppIndex.AppIndexApi.end(mClient, viewAction);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient.disconnect();
    }

    private void signIn() {
        Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show();

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(
                                AuthUI.EMAIL_PROVIDER,
                                AuthUI.GOOGLE_PROVIDER)

                        .build(),
                RC_SIGN_IN);
    }

    public boolean isSignedIn() {
        return (FirebaseAuth.getInstance().getCurrentUser() != null);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Set<Book> books = null;

        public SectionsPagerAdapter(FragmentManager fm, Set<Book> books) {
            super(fm);
            this.books = books;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return BookViewFragment.newInstance(books, ALL);
                case 1:
                    return BookViewFragment.newInstance(books, READ | ANY_EBOOK | ANY_BOOK);
                case 2:
                    return BookViewFragment.newInstance(books, ANY_BOOK | ANY_EBOOK);
            }
            return BookViewFragment.newInstance(books, ALL);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "All";
                case 1:
                    return "Read";
                case 2:
                    return "Unread";
            }
            return null;
        }
    }

    private ProgressDialog startLoading() {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.show();
        return progress;
    }
}
