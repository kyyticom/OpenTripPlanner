/* This file is based on code copied from project OneBusAway, see the LICENSE file for further information. */
package org.opentripplanner.model;

public final class BookingRule extends TransitEntity {

    private static final long serialVersionUID = 1L;

    private int type;

    private int priorNoticeDurationMin;

    private int priorNoticeDurationMax;

    private int priorNoticeLastDay;

    private int priorNoticeLastTime;

    private int priorNoticeStartDay;

    private int priorNoticeStartTime;

    private FeedScopedId priorNoticeServiceId;

    private String message;

    private String pickupMessage;

    private String dropOffMessage;

    private String phoneNumber;

    private String infoUrl;

    private String url;

    public BookingRule(FeedScopedId id) {
        super(id);
    }

    public BookingRule(BookingRule br) {
        super(br.getId());
        this.type = br.type;
        this.priorNoticeDurationMin = br.priorNoticeDurationMin;
        this.priorNoticeDurationMax = br.priorNoticeDurationMax;
        this.priorNoticeLastDay = br.priorNoticeLastDay;
        this.priorNoticeLastTime = br.priorNoticeLastTime;
        this.priorNoticeStartDay = br.priorNoticeStartDay;
        this.priorNoticeStartTime = br.priorNoticeStartTime;
        this.priorNoticeServiceId = br.priorNoticeServiceId;
        this.message = br.message;
        this.pickupMessage = br.pickupMessage;
        this.dropOffMessage = br.dropOffMessage;
        this.phoneNumber = br.phoneNumber;
        this.infoUrl = br.infoUrl;
        this.url = br.url;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPriorNoticeDurationMin() {
        return priorNoticeDurationMin;
    }

    public void setPriorNoticeDurationMin(int priorNoticeDurationMin) {
        this.priorNoticeDurationMin = priorNoticeDurationMin;
    }

    public int getPriorNoticeDurationMax() {
        return priorNoticeDurationMax;
    }

    public void setPriorNoticeDurationMax(int priorNoticeDurationMax) {
        this.priorNoticeDurationMax = priorNoticeDurationMax;
    }

    public int getPriorNoticeLastDay() {
        return priorNoticeLastDay;
    }

    public void setPriorNoticeLastDay(int priorNoticeLastDay) {
        this.priorNoticeLastDay = priorNoticeLastDay;
    }

    public int getPriorNoticeLastTime() {
        return priorNoticeLastTime;
    }

    public void setPriorNoticeLastTime(int priorNoticeLastTime) {
        this.priorNoticeLastTime = priorNoticeLastTime;
    }

    public int getPriorNoticeStartDay() {
        return priorNoticeStartDay;
    }

    public void setPriorNoticeStartDay(int priorNoticeStartDay) {
        this.priorNoticeStartDay = priorNoticeStartDay;
    }

    public int getPriorNoticeStartTime() {
        return priorNoticeStartTime;
    }

    public void setPriorNoticeStartTime(int priorNoticeStartTime) {
        this.priorNoticeStartTime = priorNoticeStartTime;
    }

    public FeedScopedId getPriorNoticeServiceId() {
        return priorNoticeServiceId;
    }

    public void setPriorNoticeServiceId(FeedScopedId priorNoticeServiceId) {
        this.priorNoticeServiceId = priorNoticeServiceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPickupMessage() {
        return pickupMessage;
    }

    public void setPickupMessage(String pickupMessage) {
        this.pickupMessage = pickupMessage;
    }

    public String getDropOffMessage() {
        return dropOffMessage;
    }

    public void setDropOffMessage(String dropOffMessage) {
        this.dropOffMessage = dropOffMessage;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getInfoUrl() {
        return infoUrl;
    }

    public void setInfoUrl(String infoUrl) {
        this.infoUrl = infoUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "<BookingRule " + getId() + ">";
    }
}