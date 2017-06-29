package info.patsch.ebl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;

import info.patsch.ebl.books.BookViewFragment;
import info.patsch.ebl.books.BooksHolder;
import info.patsch.ebl.books.FilterConstants;
import info.patsch.ebl.books.ModelFragment;
import info.patsch.ebl.books.events.BooksFilteredEvent;
import info.patsch.ebl.books.events.BooksLoadedEvent;
import info.patsch.ebl.books.exception.ExceptionHandler;
import info.patsch.ebl.books.search.BookSearchActivity;


public class MainActivity extends AppCompatActivity implements FilterConstants {

    private final static int RC_SIGN_IN = 27;

    private ViewPager mViewPager;

    private TextView mBookCount;

    private static final String MODEL = "model";

    private ModelFragment model = null;

    ProgressDialog mProgressDialog = null;

    private static boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        if (!isInitialized) {
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);

                OkHttpDownloader okHttpDownloader = new OkHttpDownloader(this);
                Picasso picasso = new Picasso.Builder(this)
                        .downloader(okHttpDownloader)
                        .build();

                Picasso.setSingletonInstance(picasso);
            } catch (DatabaseException ex) {
                Log.w("MainActivity", ex.getMessage(), ex);
            } catch (IllegalStateException ignored) {
            } finally {
                isInitialized = true;
            }
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mBookCount = (TextView) findViewById(R.id.book_count_bar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BookSearchActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupPager() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(sectionsPagerAdapter);
        mProgressDialog.dismiss();
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
            } else if (BooksHolder.INSTANCE.isInitialized()) {
                EventBus.getDefault().post(new BooksLoadedEvent());
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBookLoaded(BooksLoadedEvent event) {
        setupPager();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onBooksFiltered(BooksFilteredEvent event) {
        if (event.getCount() > 0) {
            mBookCount.setText(getString(R.string.book_count, event.getCount()));
            mBookCount.setVisibility(View.VISIBLE);
        } else {
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

        // Default Database rules do not allow unauthenticated reads, so we need to
        // sign in before attaching the RecyclerView adapter otherwise the Adapter will
        // not be able to read any data from the Database.
        if (!isSignedIn()) {
            signIn();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void signIn() {
        Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show();

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                        .build(),
                RC_SIGN_IN);
    }

    public boolean isSignedIn() {
        return (FirebaseAuth.getInstance().getCurrentUser() != null);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return BookViewFragment.newInstance(ALL);
                case 1:
                    return BookViewFragment.newInstance(READ | ANY_EBOOK | ANY_BOOK);
                case 2:
                    return BookViewFragment.newInstance(ANY_BOOK | ANY_EBOOK);
            }
            return BookViewFragment.newInstance(ALL);
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
