package info.patsch.ebl.books.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageLink {

    private String thumbnail;
    private String small;
    private String medium;
    private String large;
    private String smallThumbnail;
    private String extraLarge;

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getLarge() {
        return large;
    }

    public void setLarge(String large) {
        this.large = large;
    }

    public String getSmallThumbnail() {
        return smallThumbnail;
    }

    public void setSmallThumbnail(String smallThumbnail) {
        this.smallThumbnail = smallThumbnail;
    }

    public String getExtraLarge() {
        return extraLarge;
    }

    public void setExtraLarge(String extraLarge) {
        this.extraLarge = extraLarge;
    }

    public String getImageUrl() {
        if (medium != null) {
            return medium;
        } else if (thumbnail != null) {
            return thumbnail;
        } else if (small != null) {
            return small;
        } else if (smallThumbnail != null) {
            return smallThumbnail;
        } else if (large != null) {
            return large;
        } else return extraLarge;
    }
}
