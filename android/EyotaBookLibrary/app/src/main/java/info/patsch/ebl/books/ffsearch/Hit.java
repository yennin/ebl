package info.patsch.ebl.books.ffsearch;

import android.text.Html;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Hit {

    private String id;
    private Data data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getYear() {
        return getFirstOrNull(data.getYear());
    }

    public String getTitle() {
        return decodeUrl(getFirstOrNull(data.getTitle()));
    }

    public String getPfn() {
        return getFirstOrNull(data.getPfn());
    }

    public String getImageurlAmazon() {
        return getFirstOrNull(data.getImageurl_amazon());
    }

    public String getImageloc() {
        return getFirstOrNull(data.getImageloc());
    }

    public String getHasimage() {
        return getFirstOrNull(data.getHasimage());
    }

    public String getAuthorUrl() {
        return splitAuthorInfo(0);
    }

    public String getAuthorName() {
        return decodeUrl(splitAuthorInfo(1));
    }

    public String getSeriesName() {
        return decodeUrl(splitSeriesInfo(0));
    }

    public String getSeriesNumber() {
        return splitSeriesInfo(1);
    }

    private String splitAuthorInfo(int pos) {
        if (getFirstOrNull(data.getAuthorsinfo()) == null) return null;
        String[] split = getFirstOrNull(data.getAuthorsinfo()).split("\\|");
        if (split.length > pos) {
            return split[pos];
        } else {
            return null;
        }
    }

    private String splitSeriesInfo(int pos) {
        if (getFirstOrNull(data.getSeriesinfo()) == null) return null;
        String[] split = getFirstOrNull(data.getSeriesinfo()).split("\\|");
        if (split.length > pos) {
            return split[pos];
        } else {
            return null;
        }
    }

    private String getFirstOrNull(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private String decodeUrl(String in) {
        if (in == null) return null;

        return Html.fromHtml(in).toString();

    }
}
