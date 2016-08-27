package info.patsch.ebl.books.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by patsch on 27.08.16.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class BookSearchResult {
    private String kind;
    private List<BookItems> items;
    private int totalItems;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public List<BookItems> getItems() {
        return items;
    }

    public void setItems(List<BookItems> items) {
        this.items = items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
}
