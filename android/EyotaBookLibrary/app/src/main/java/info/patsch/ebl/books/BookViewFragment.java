package info.patsch.ebl.books;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import info.patsch.ebl.R;
import info.patsch.ebl.RecyclerViewFragment;
import info.patsch.ebl.books.edit.EditBookActivity;
import info.patsch.ebl.books.events.BookAddedEvent;
import info.patsch.ebl.books.events.BookDBRemoveEvent;
import info.patsch.ebl.books.events.BookDBUpdateEvent;
import info.patsch.ebl.books.events.BookRemovedEvent;
import info.patsch.ebl.books.search.BookSearchActivity;

/**
 * Created by patsch on 22.08.16.
 */
public class BookViewFragment extends RecyclerViewFragment implements FilterConstants, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    public static final String TAG = "BookViewFragment";
    private static final String STATE_QUERY = "state_query";

    private final static String ARG_FLAGS = "flags";
    private static final String ARG_BOOKS = "books";

    public static final int EDIT_BOOK_REQUEST = 37;

    private Set<Book> mBooks = null;
    private SortedList<Book> model = null;
    private BookAdapter adapter = null;

    private SearchView mSearchView = null;
    private CharSequence initialQuery;

    private int mFlags = ALL;

    public BookViewFragment() {
    }

    public static BookViewFragment newInstance(Set<Book> books, int flags) {
        BookViewFragment fragment = new BookViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FLAGS, flags);
        args.putParcelableArrayList(ARG_BOOKS, new ArrayList<>(books));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mFlags = getArguments().getInt(ARG_FLAGS);
        ArrayList<Parcelable> parcelables = getArguments().getParcelableArrayList(ARG_BOOKS);
        for (Parcelable parcelable : parcelables) {
            mBooks.add((Book) parcelable);
        }

        adapter.addAll(mBooks);
        adapter.getFilter().filter(initialQuery);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mBooks = new HashSet<>();
        adapter = new BookAdapter();
        model = new SortedList<>(Book.class, sortCallback);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setLayoutManager(new LinearLayoutManager(getActivity()));
        setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BookSearchActivity.class);
                getActivity().startActivity(intent);
            }
        });
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
            model.clear();
            model.addAll(books);
            filterList.clear();
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
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            initialQuery = savedInstanceState.getCharSequence(STATE_QUERY);
        }
        setHasOptionsMenu(true);
    }

    private void deleteBook(Book book) {
        EventBus.getDefault().post(new BookDBRemoveEvent(book));
    }

    private void updateBook(Book book) {
        EventBus.getDefault().postSticky(new BookDBUpdateEvent(book));
    }


    private class CustomFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            Boolean readFlag = null;
            Boolean bookFlag = null;
            Boolean eBookFlag = null;

            if ((mFlags & ALL) != ALL) { // flag filter required
                if ((mFlags & ANY_READ) != ANY_READ) { // readFlag filter required
                    readFlag = (mFlags & READ) == READ;
                }
                if ((mFlags & ANY_BOOK) != ANY_BOOK) { // bookFlag filter required
                    bookFlag = (mFlags & BOOK) == BOOK;
                }
                if ((mFlags & ANY_EBOOK) != ANY_EBOOK) { // ebook filter required
                    eBookFlag = (mFlags & EBOOK) == EBOOK;
                }
            }

            BookFilter filter = new BookFilter(constraint, readFlag, bookFlag, eBookFlag);

            List<Book> filteredBooks = new ArrayList<>();
            for (Book book : mBooks) {
                if (filter.accept(book)) {
                    filteredBooks.add(book);
                }
            }

            results.count = filteredBooks.size();
            results.values = filteredBooks;
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
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
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onBookAdded(BookAddedEvent event) {
        this.mBooks.add(event.getBook());
        adapter.add(event.getBook());
        adapter.notifyDataSetChanged();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onBookRemoved(BookRemovedEvent event) {
        this.mBooks.remove(event.getBook());
        adapter.remove(event.getBook());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (adapter != null && isVisibleToUser) {
            adapter.getFilter().filter("");
            adapter.notifyDataSetChanged();
        }
    }

    class BookFilter {
        private String query;
        private Boolean read;
        private Boolean book;
        private Boolean eBook;

        public BookFilter(CharSequence query, Boolean read, Boolean book, Boolean eBook) {
            if (!TextUtils.isEmpty(query)) {

                this.query = query.toString().toUpperCase();
            }
            this.read = read;
            this.book = book;
            this.eBook = eBook;
        }

        public boolean accept(Book testBook) {

            if (query != null) {
                if (!testBook.getAuthorName().toUpperCase().contains(query) &&
                        !testBook.getTitle().toUpperCase().contains(query) &&
                        !(testBook.getSeriesName() != null && testBook.getSeriesName().toUpperCase().contains(query))) {
                    return false;
                }
            }

            if (read != null && read != testBook.isRead()) {
                return false;
            }
            if (book != null && book != testBook.isBook()) {
                return false;
            }
            if (eBook != null && eBook != testBook.isEBook()) {
                return false;
            }

            return true;
        }
    }
}
