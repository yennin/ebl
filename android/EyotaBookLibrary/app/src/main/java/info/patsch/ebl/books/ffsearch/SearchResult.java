package info.patsch.ebl.books.ffsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by patsch on 02.09.16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {
    public String rank;
    public String matchExpr;
    public Hits hits;
    public Info info;

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getMatchExpr() {
        return matchExpr;
    }

    public void setMatchExpr(String matchExpr) {
        this.matchExpr = matchExpr;
    }

    public Hits getHits() {
        return hits;
    }

    public void setHits(Hits hits) {
        this.hits = hits;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }
}
