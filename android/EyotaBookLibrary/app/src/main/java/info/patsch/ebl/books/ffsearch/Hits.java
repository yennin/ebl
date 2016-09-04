package info.patsch.ebl.books.ffsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Hits {
    public int found;
    public int start;
    public List<Hit> hit;

    public int getFound() {
        return found;
    }

    public void setFound(int found) {
        this.found = found;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public List<Hit> getHit() {
        return hit;
    }

    public void setHit(List<Hit> hit) {
        this.hit = hit;
    }
}
