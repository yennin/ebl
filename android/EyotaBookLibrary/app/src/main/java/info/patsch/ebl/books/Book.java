package info.patsch.ebl.books;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by patsch on 21.08.16.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Book implements Comparable<Book> {
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
        isBook = true;
    }

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

        if (title != null ? !title.equals(book.title) : book.title != null) return false;
        return authorName != null ? authorName.equals(book.authorName) : book.authorName == null;

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (authorName != null ? authorName.hashCode() : 0);
        return result;
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
