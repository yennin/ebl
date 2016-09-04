package info.patsch.ebl.books;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashSet;
import java.util.Set;
import info.patsch.ebl.R;
import info.patsch.ebl.books.events.BookAddedEvent;
import info.patsch.ebl.books.events.BookDBNewEvent;
import info.patsch.ebl.books.events.BookDBRemoveEvent;
import info.patsch.ebl.books.events.BookDBUpdateEvent;
import info.patsch.ebl.books.events.BookRemovedEvent;
import info.patsch.ebl.books.events.BooksLoadedEvent;

/**
 * Created by patsch on 03.09.16.
 */
public class ModelFragment extends Fragment implements FirebaseAuth.AuthStateListener {

    public static final String TAG = "ModelFragment";

    private FirebaseAuth mAuth = null;
    private DatabaseReference mBookRef = null;

    private Set<Book> mBooks = null;

    private boolean initialDataLoaded = false;
    private boolean isLoading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mBooks = new HashSet<>();
    }

    @Override
    public void onDestroy() {
        if (mAuth != null) {
            mAuth.removeAuthStateListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);

    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            loadBooks();

        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");
        }
    }

    private void loadBooks() {
        if (initialDataLoaded || isLoading) {
            return;
        }
        isLoading = true;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        mBookRef = ref.child("users").child(currentUser.getUid()).child("books");
        mBookRef.keepSynced(true);

        mBookRef.orderByChild("title").addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (initialDataLoaded) {
                            Book book = dataSnapshot.getValue(Book.class);
                            mBooks.add(book);
                            EventBus.getDefault().postSticky(new BookAddedEvent(book));
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        if (initialDataLoaded) {
                            Book book = dataSnapshot.getValue(Book.class);
                            mBooks.add(book);
                            EventBus.getDefault().postSticky(new BookAddedEvent(book));
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        if (initialDataLoaded) {
                            Book book = dataSnapshot.getValue(Book.class);
                            mBooks.remove(book);
                            EventBus.getDefault().postSticky(new BookRemovedEvent(book));
                        }
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        if (initialDataLoaded) {
                            // should not happen, ignore
                            Book book = dataSnapshot.getValue(Book.class);
                            Log.w(TAG, "Book moved" + book.getTitle());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });

        mBookRef.orderByChild("title").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (initialDataLoaded) {
                            return;
                        }
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        for (DataSnapshot child : children) {
                            Book book = child.getValue(Book.class);
                            mBooks.add(book);
                        }
                        EventBus.getDefault().post(new BooksLoadedEvent(mBooks));
                        Toast.makeText(getActivity(), TextUtils.concat(getString(R.string.done_loading, mBooks.size())), Toast.LENGTH_SHORT)
                                .show();
                        initialDataLoaded = true;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    private boolean addImage(Book book) {
        if (mBooks.contains(book)) {
            return false;
        }
        if (book.getImage() != null) {
            book.setImageEncoded(Base64.encodeToString(book.getImage(), Base64.URL_SAFE));
        }
        book.setImage(null);
        return true;
    }

    private void storeNewBook(Book book) {
        DatabaseReference childRef = mBookRef.push();
        book.setId(childRef.getKey());
        childRef.setValue(book);
        EventBus.getDefault().postSticky(new BookAddedEvent(book));
    }

    private void updateBook(Book book) {
        mBookRef.child(book.getId()).setValue(book);
        mBooks.add(book);
        EventBus.getDefault().postSticky(new BookAddedEvent(book));
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onNewBook(BookDBNewEvent event) {
        Book book = event.getBook();

        if (TextUtils.isEmpty(book.getTitle())) {
            Toast.makeText(getActivity(), R.string.validation_error_title, Toast.LENGTH_LONG).show();
            return;
        } else if (TextUtils.isEmpty(book.getAuthorName())) {
            Toast.makeText(getActivity(), R.string.validation_error_author, Toast.LENGTH_LONG).show();
            return;
        }

        if (book.getId() == null) { //new book
            if (addImage(book)) {
                storeNewBook(event.getBook());
                Toast.makeText(getActivity(), R.string.new_book_added, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), R.string.book_alread_exists, Toast.LENGTH_LONG).show();
            }
        } else {
            updateBook(book);
            Toast.makeText(getActivity(), R.string.book_alread_exists, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onBookUpdated(BookDBUpdateEvent event) {
        updateBook(event.getBook());
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onBookRemoved(BookDBRemoveEvent event) {
        mBookRef.child(event.getBook().getId()).removeValue();
        EventBus.getDefault().postSticky(new BookRemovedEvent(event.getBook()));
    }

    // external access

    public Set<Book> getBooks() {
        return mBooks;
    }
}
