package info.patsch.ebl.books.ffsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {
    private List<String> authorsinfo;
    private List<String> booktype;
    private List<String> db;
    private List<String> hasimage;
    private List<String> imageloc;
    private List<String> imageurl_amazon;
    private List<String> pfn;
    private List<String> seriesinfo;
    private List<String> title;
    private List<String> year;

    public List<String> getAuthorsinfo() {
        return authorsinfo;
    }

    public void setAuthorsinfo(List<String> authorsinfo) {
        this.authorsinfo = authorsinfo;
    }

    public List<String> getBooktype() {
        return booktype;
    }

    public void setBooktype(List<String> booktype) {
        this.booktype = booktype;
    }

    public List<String> getDb() {
        return db;
    }

    public void setDb(List<String> db) {
        this.db = db;
    }

    public List<String> getHasimage() {
        return hasimage;
    }

    public void setHasimage(List<String> hasimage) {
        this.hasimage = hasimage;
    }

    public List<String> getImageloc() {
        return imageloc;
    }

    public void setImageloc(List<String> imageloc) {
        this.imageloc = imageloc;
    }

    public List<String> getImageurl_amazon() {
        return imageurl_amazon;
    }

    public void setImageurl_amazon(List<String> imageurl_amazon) {
        this.imageurl_amazon = imageurl_amazon;
    }

    public List<String> getPfn() {
        return pfn;
    }

    public void setPfn(List<String> pfn) {
        this.pfn = pfn;
    }

    public List<String> getSeriesinfo() {
        return seriesinfo;
    }

    public void setSeriesinfo(List<String> seriesinfo) {
        this.seriesinfo = seriesinfo;
    }

    public List<String> getTitle() {
        return title;
    }

    public void setTitle(List<String> title) {
        this.title = title;
    }

    public List<String> getYear() {
        return year;
    }

    public void setYear(List<String> year) {
        this.year = year;
    }
}
