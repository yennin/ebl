package info.patsch.ebl.books;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import info.patsch.ebl.R;
import info.patsch.ebl.RecyclerViewFragment;

/**
 * Created by patsch on 22.08.16.
 */
public class BookViewFragment extends RecyclerViewFragment implements FirebaseAuth.AuthStateListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    public static final String TAG = "BookViewFragment";
    private static final String STATE_QUERY = "state_query";

    private FirebaseAuth mAuth = null;
    private DatabaseReference mRef = null;
    private DatabaseReference mBookRef = null;


    private SortedList<Book> model = null;
    private List<Book> books = null;
    private BookAdapter adapter = null;
    private SearchView mSearchView = null;
    private CharSequence initialQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        adapter = new BookAdapter();
        model = new SortedList<>(Book.class, sortCallback);
        books = new ArrayList<>();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setLayoutManager(new LinearLayoutManager(getActivity()));
        setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);
    }

    @Override
    public void onDestroy() {
        if (mAuth != null) {
            mAuth.removeAuthStateListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        configureSearchView(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void configureSearchView(Menu menu) {

        MenuItem search = menu.findItem(R.id.booklist_filter);
        mSearchView = (SearchView) search.getActionView();

        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint("Search Here");

        mSearchView.setOnCloseListener(this);
        mSearchView.setSubmitButtonEnabled(false);

        if (initialQuery != null) {
            mSearchView.setIconified(false);
            search.expandActionView();
            mSearchView.setQuery(initialQuery, true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        final ProgressDialog progressDialog = startLoading();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference();
        mBookRef = mRef.child("users").child(currentUser.getUid()).child("books");
        mBookRef.keepSynced(true);
        mBookRef.orderByChild("title").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        for (DataSnapshot child : children) {
                            Book book = child.getValue(Book.class);
                            books.add(book);
                        }
                        adapter.add(books);
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), R.string.done, Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }

    private ProgressDialog startLoading() {
        ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.show();
        return progress;
    }

    private SortedList.Callback<Book> sortCallback = new SortedList.Callback<Book>() {
        @Override
        public int compare(Book o1, Book o2) {
            return o1.compareTo(o2);
        }

        @Override
        public boolean areContentsTheSame(Book oldItem, Book newItem) {
            return (areItemsTheSame(oldItem, newItem));
        }

        @Override
        public boolean areItemsTheSame(Book oldItem, Book newItem) {
            return (compare(oldItem, newItem) == 0);
        }

        @Override
        public void onInserted(final int position, final int count) {
            adapter.notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(final int position, final int count) {
            adapter.notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(final int fromPosition, final int toPosition) {
            adapter.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(final int position, final int count) {
            adapter.notifyItemRangeChanged(position, count);
        }
    };


    class BookAdapter extends RecyclerView.Adapter<BookController> implements Filterable {

        List<Book> filterList = null;
        Filter filter = null;

        public BookAdapter() {
            this.filterList = new ArrayList<>();
        }

        @Override
        public BookController onCreateViewHolder(ViewGroup parent, int viewType) {
            return (new BookController(getActivity().getLayoutInflater()
                    .inflate(R.layout.book_row, parent, false)));
        }

        @Override
        public void onBindViewHolder(BookController holder, int position) {
            holder.bindModel(filterList.get(position));
        }

        @Override
        public int getItemCount() {
            return (filterList.size());
        }

        @Override
        public Filter getFilter() {
            if (filter == null) {
                filter = new CustomFilter();
            }
            return filter;
        }

        public void add(List<Book> books) {
            model.beginBatchedUpdates();
            model.addAll(books);
            filterList.addAll(books);
            model.endBatchedUpdates();
        }

        public void replaceAll(List<Book> books) {
            filterList = books;
            model.beginBatchedUpdates();
            model.clear();
            model.addAll(books);
            model.endBatchedUpdates();
        }
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        adapter.getFilter().filter(query);
        return false;
    }

    @Override
    public boolean onClose() {
        adapter.getFilter().filter("");
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (!mSearchView.isIconified()) {
            state.putCharSequence(STATE_QUERY, mSearchView.getQuery());
        }

        //state.putStringArrayList(STATE_MODEL, words);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
        } else {
            initialQuery = savedInstanceState.getCharSequence(STATE_QUERY);
        }
        setHasOptionsMenu(true);
    }


    //  Query query = mBookRef.orderByChild("title");
//mManager.smoothScrollToPosition(mBooks, null, positionStart);


    private void addBook() {
        /*
                if (isSignedIn()) {
                    Book book = new Book();
                    book.setTitle("Test Book");
                    book.setAuthorName("Test Author");
                    book.setYear(2019);
                    DatabaseReference childRef = mBookRef.push();
                    book.setId(childRef.getKey());
                    childRef.setValue(book);
         */
        //loadBooksFromFile();
    }

    private void loadBooksFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getResources().openRawResource(R.raw.books)) {
            Book[] books = mapper.readValue(is, Book[].class);
            for (Book book : books) {
                addBook(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addBook(Book book) {
        if (book.getImage() != null) {
            book.setImageEncoded(Base64.encodeToString(book.getImage(), Base64.URL_SAFE));
        }
        book.setImage(null);
        DatabaseReference childRef = mBookRef.push();
        book.setId(childRef.getKey());
        childRef.setValue(book);
    }


    private class CustomFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (!TextUtils.isEmpty(constraint)) {

                constraint = constraint.toString().toUpperCase();

                List<Book> filteredBooks = new ArrayList<>();
                for (Book book : books) {
                    if (book.getAuthorName().toUpperCase().contains(constraint) ||
                            book.getTitle().toUpperCase().contains(constraint) ||
                            (book.getSeriesName() != null && book.getSeriesName().toUpperCase().contains(constraint))) {
                        filteredBooks.add(book);
                    }
                }

                results.count = filteredBooks.size();
                results.values = filteredBooks;
            } else {
                results.count = books.size();
                results.values = books;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, final FilterResults results) {
            adapter.replaceAll((List<Book>) results.values);
            adapter.notifyDataSetChanged();
        }
    }

}
