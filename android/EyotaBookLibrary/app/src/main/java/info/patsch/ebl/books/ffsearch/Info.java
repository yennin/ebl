package info.patsch.ebl.books.ffsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Info {

    public String rid;
    public int timeMs;
    public int cpuTimeMs;

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public int getTimeMs() {
        return timeMs;
    }

    public void setTimeMs(int timeMs) {
        this.timeMs = timeMs;
    }

    public int getCpuTimeMs() {
        return cpuTimeMs;
    }

    public void setCpuTimeMs(int cpuTimeMs) {
        this.cpuTimeMs = cpuTimeMs;
    }
}
