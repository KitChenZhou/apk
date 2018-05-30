package com.ckt.testauxiliarytool.autophone;

public class CallRecord {

    private String id;
    private String startDate;
    private String endDate;
    private String duration;
    private String networkType;
    private String isConnect;

    public CallRecord(String id, String startDate, String endDate, String duration, String networkType, String isConnect) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.networkType = networkType;
        this.isConnect = isConnect;
    }

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDuration() {
        return duration;
    }

    void setDuration(String duration) {
        this.duration = duration;
    }

    public String getNetworkType() {
        return networkType;
    }

    void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String isConnect() {
        return isConnect;
    }

    void setConnect(String connect) {
        isConnect = connect;
    }

    @Override
    public String toString() {
        return "CallRecord{" +
                "id=" + id +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", duration=" + duration +
                ", networkType='" + networkType + '\'' +
                ", isConnect=" + isConnect +
                '}';
    }
}
