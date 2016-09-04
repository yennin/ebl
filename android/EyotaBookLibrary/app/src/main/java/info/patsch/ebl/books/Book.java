package info.patsch.ebl.books;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by patsch on 21.08.16.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Book implements Comparable<Book>, Parcelable {
    public final static String BOOK_TAG = "book";

    private String id;
    private boolean hasImage;
    private String imageLoc;
    private byte[] image;
    private String imageEncoded;
    private String pfn;
    private String seriesName;
    private String seriesNumber;
    private String title;
    private String authorUrl;
    private String authorName;
    private int year;
    private boolean isRead;
    private boolean isBook;
    private boolean isEBook;
    private boolean isFavorite;
    private String comment;

    public Book() {

    }

    protected Book(Parcel in) {
        id = in.readString();
        hasImage = in.readByte() != 0;
        imageLoc = in.readString();
        image = in.createByteArray();
        imageEncoded = in.readString();
        pfn = in.readString();
        seriesName = in.readString();
        seriesNumber = in.readString();
        title = in.readString();
        authorUrl = in.readString();
        authorName = in.readString();
        year = in.readInt();
        isRead = in.readByte() != 0;
        isBook = in.readByte() != 0;
        isEBook = in.readByte() != 0;
        isFavorite = in.readByte() != 0;
        comment = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeByte((byte) (hasImage ? 1 : 0));
        dest.writeString(imageLoc);
        dest.writeByteArray(image);
        dest.writeString(imageEncoded);
        dest.writeString(pfn);
        dest.writeString(seriesName);
        dest.writeString(seriesNumber);
        dest.writeString(title);
        dest.writeString(authorUrl);
        dest.writeString(authorName);
        dest.writeInt(year);
        dest.writeByte((byte) (isRead ? 1 : 0));
        dest.writeByte((byte) (isBook ? 1 : 0));
        dest.writeByte((byte) (isEBook ? 1 : 0));
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeString(comment);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public byte[] getImage() {
        return image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean hasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public String getImageLoc() {
        return imageLoc;
    }

    public void setImageLoc(String imageLoc) {
        this.imageLoc = imageLoc;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getPfn() {
        return pfn;
    }

    public void setPfn(String pfn) {
        this.pfn = pfn;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    public void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isBook() {
        return isBook;
    }

    public void setBook(boolean book) {
        isBook = book;
    }

    public boolean isEBook() {
        return isEBook;
    }

    public void setEBook(boolean EBook) {
        isEBook = EBook;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImageEncoded() {
        return imageEncoded;
    }

    public void setImageEncoded(String imageEncoded) {
        this.imageEncoded = imageEncoded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        return id != null ? id.equals(book.id) : book.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(Book that) {
        if (this == that) return 0;
        if (this.getTitle().equals(that.getTitle())) {
            return this.getAuthorName().compareTo(that.getAuthorName());
        }
        else {
            return this.getTitle().compareTo(that.getTitle());
        }
    }

}
