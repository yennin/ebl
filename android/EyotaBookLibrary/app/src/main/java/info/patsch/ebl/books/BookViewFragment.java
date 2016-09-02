package info.patsch.ebl.books;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import info.patsch.ebl.R;
import info.patsch.ebl.RecyclerViewFragment;
import info.patsch.ebl.books.edit.EditBookActivity;
import info.patsch.ebl.books.search.BookSearchActivity;

/**
 * Created by patsch on 22.08.16.
 */
public class BookViewFragment extends RecyclerViewFragment implements FirebaseAuth.AuthStateListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    public static final String TAG = "BookViewFragment";
    private static final String STATE_QUERY = "state_query";

    public static final int EDIT_BOOK_REQUEST = 37;
    public final static int SEARCH_BOOK_REQUEST = 27;

    private FirebaseAuth mAuth = null;
    private DatabaseReference mRef = null;
    private DatabaseReference mBookRef = null;


    private SortedList<Book> model = null;
    private Set<Book> books = null;
    private BookAdapter adapter = null;
    private SearchView mSearchView = null;
    private CharSequence initialQuery;

    private boolean initialDataLoaded = false;
    private boolean isLoading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        adapter = new BookAdapter();
        model = new SortedList<>(Book.class, sortCallback);
        books = new HashSet<>();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setLayoutManager(new LinearLayoutManager(getActivity()));
        setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BookSearchActivity.class);
                startActivityForResult(intent, SEARCH_BOOK_REQUEST);
            }
        });
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
        if (initialDataLoaded || isLoading) {
            return;
        }
        final ProgressDialog progressDialog = startLoading();
        isLoading = true;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference();
        mBookRef = mRef.child("users").child(currentUser.getUid()).child("books");
        mBookRef.keepSynced(true);

        mBookRef.orderByChild("title").addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (initialDataLoaded) {
                            Book book = dataSnapshot.getValue(Book.class);
                            books.add(book);
                            adapter.add(book);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        if (initialDataLoaded) {
                            Book book = dataSnapshot.getValue(Book.class);
                            books.add(book);
                            adapter.add(book);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        if (initialDataLoaded) {
                            Book book = dataSnapshot.getValue(Book.class);
                            books.remove(book);
                            adapter.remove(book);
                            adapter.notifyDataSetChanged();
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
                            books.add(book);
                        }
                        adapter.addAll(books);
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), TextUtils.concat(getString(R.string.done_loading, books.size())), Toast.LENGTH_SHORT)
                                .show();
                        initialDataLoaded = true;
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
                    .inflate(R.layout.book_row, parent, false), new DropdownListener(), null));
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

        public void addAll(Set<Book> books) {
            model.beginBatchedUpdates();
            model.addAll(books);
            filterList.addAll(books);
            Collections.sort(filterList);
            model.endBatchedUpdates();
        }

        public void add(Book book) {
            model.beginBatchedUpdates();
            if (filterList.contains(book)) {
                // replace
                model.remove(book);
                filterList.remove(book);
            }
            model.add(book);
            filterList.add(book);
            Collections.sort(filterList);
            model.endBatchedUpdates();
        }

        public void remove(Book book) {
            model.beginBatchedUpdates();
            model.remove(book);
            filterList.remove(book);
            model.endBatchedUpdates();
        }

        public void replaceAll(List<Book> books) {
            model.beginBatchedUpdates();
            filterList = books;
            model.clear();
            model.addAll(books);
            Collections.sort(filterList);
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
        if (mSearchView != null && !mSearchView.isIconified()) {
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

    private void loadBooksFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getResources().openRawResource(R.raw.books)) {
            Book[] books = mapper.readValue(is, Book[].class);
            int i = 0;
            for (Book book : books) {
                book.setId(i++ + "");
                addBook(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteBook(Book book) {
        adapter.remove(book);
        mBookRef.child(book.getId()).removeValue();
    }

    private void updateBook(Book book) {
        mBookRef.child(book.getId()).setValue(book);
    }

    private boolean addBook(Book book) {
        if (books.contains(book)) {
            return false;
        }
        if (book.getImage() != null) {
            book.setImageEncoded(Base64.encodeToString(book.getImage(), Base64.URL_SAFE));
        }
        book.setImage(null);
        DatabaseReference childRef = mBookRef.push();
        book.setId(childRef.getKey());
        childRef.setValue(book);
        books.add(book);
        adapter.add(book);
        adapter.notifyDataSetChanged();
        return true;
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
                results.values = new ArrayList<>(books);
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, final FilterResults results) {
            adapter.replaceAll((List<Book>) results.values);
            adapter.notifyDataSetChanged();
        }
    }

    private class DropdownListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final Book book = (Book) v.getTag();

            PopupMenu popup = new PopupMenu(getActivity(), v);//
            popup.getMenuInflater().inflate(R.menu.book_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.delete:
                            deleteBook(book);
                            break;
                        case R.id.edit:
                            editBookInfo(book);
                            break;
                        case R.id.toggle_book:
                            book.setBook(!book.isBook());
                            updateBook(book);
                            break;
                        case R.id.toggle_ebook:
                            book.setEBook(!book.isEBook());
                            updateBook(book);
                            break;
                        case R.id.toggle_read:
                            book.setRead(!book.isRead());
                            updateBook(book);
                            break;
                    }
                    adapter.notifyDataSetChanged();
                    return true;
                }
            });

            popup.show();//showing popup menu
        }
    }

    private void editBookInfo(Book book) {
        Intent intent = new Intent(getActivity(), EditBookActivity.class);
        intent.putExtra(Book.BOOK_TAG, book);
        startActivityForResult(intent, EDIT_BOOK_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == EDIT_BOOK_REQUEST || requestCode == SEARCH_BOOK_REQUEST) {
                Book book = data.getParcelableExtra(Book.BOOK_TAG);
                if (TextUtils.isEmpty(book.getTitle())) {
                    Toast.makeText(getActivity(), R.string.validation_error_title, Toast.LENGTH_LONG).show();
                    return;
                } else if (TextUtils.isEmpty(book.getAuthorName())) {
                    Toast.makeText(getActivity(), R.string.validation_error_author, Toast.LENGTH_LONG).show();
                    return;
                }

                if (book.getId() == null) { //new book
                    if (addBook(book)) {
                        Toast.makeText(getActivity(), R.string.new_book_added, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), R.string.book_alread_exists, Toast.LENGTH_LONG).show();
                    }
                } else {
                    updateBook(book);
                    adapter.add(book);
                    books.add(book);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), R.string.book_alread_exists, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
