package info.patsch.ebl.books;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import info.patsch.ebl.R;

public class BookController extends RecyclerView.ViewHolder implements View.OnClickListener {
    private OnBookSelectedListener mOnBookSelectedListener;

    View cardView = null;
    ImageView thumbnailView = null;
    TextView titleView = null;
    TextView authorView = null;
    TextView yearView = null;
    TextView seriesView = null;
    TextView seriesNumberView = null;
    CheckBox readBox = null;
    CheckBox bookBox = null;
    CheckBox ebookBox = null;
    ImageButton dropdownButton = null;

    public BookController(View row, View.OnClickListener dropdownListener, OnBookSelectedListener onBookSelectedListener) {
        super(row);
        cardView = row;
        thumbnailView = (ImageView) row.findViewById(R.id.thumbnail);
        titleView = (TextView) row.findViewById(R.id.title);
        authorView = (TextView) row.findViewById(R.id.author);
        yearView = (TextView) row.findViewById(R.id.year);
        seriesView = (TextView) row.findViewById(R.id.series);
        seriesNumberView = (TextView) row.findViewById(R.id.seriesNumber);
        readBox = (CheckBox) row.findViewById(R.id.read);
        bookBox = (CheckBox) row.findViewById(R.id.book);
        ebookBox = (CheckBox) row.findViewById(R.id.ebook);
        dropdownButton = (ImageButton) row.findViewById(R.id.edit_dropdown);

        this.mOnBookSelectedListener = onBookSelectedListener;

        row.setOnClickListener(this);
        if (dropdownListener != null) {
            dropdownButton.setOnClickListener(dropdownListener);
        }
        else {
            dropdownButton.setVisibility(View.INVISIBLE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            row.setOnTouchListener(new View.OnTouchListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v
                            .findViewById(R.id.row_content)
                            .getBackground()
                            .setHotspot(event.getX(), event.getY());

                    return (false);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if (mOnBookSelectedListener != null) {
            mOnBookSelectedListener.onBookSelected((Book)v.getTag());
        }

    }

    public void bindModel(Book book) {
        if (book.getTitle() != null) {
            titleView.setText(book.getTitle());
            authorView.setText(book.getAuthorName());
            yearView.setText(String.valueOf(book.getYear()));
            seriesView.setText(book.getSeriesName());
            seriesNumberView.setText(book.getSeriesNumber());
            readBox.setChecked(book.isRead());
            bookBox.setChecked(book.isBook());
            ebookBox.setChecked(book.isEBook());
            setImage(book);
            cardView.setTag(book);
        }
        dropdownButton.setTag(book);
    }

    private void setImage(Book book) {
        if (book.getImageEncoded() != null) {
            byte[] image = Base64.decode(book.getImageEncoded(), Base64.URL_SAFE);
            Bitmap bMap = BitmapFactory.decodeByteArray(image, 0, image.length);
            thumbnailView.setImageBitmap(bMap);
            thumbnailView.setVisibility(View.VISIBLE);
        }
        else {
            thumbnailView.setVisibility(View.INVISIBLE);
        }
    }

    public interface OnBookSelectedListener {
        void onBookSelected(Book book);
    }

}