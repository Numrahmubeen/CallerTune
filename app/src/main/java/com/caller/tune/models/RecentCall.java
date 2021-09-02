package com.caller.tune.models;

public class RecentCall {
    private String phoneNumber, name, callTyp, callDate, callTime, callDuration, subscriberId, callerDp;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public RecentCall(String phoneNumber, String name, String callTyp, String callDate, String callTime, String callDuration, String subscriberId, String callerDp) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.callTyp = callTyp;
        this.callDate = callDate;
        this.callTime = callTime;
        this.callDuration = callDuration;
        this.subscriberId = subscriberId;
        this.callerDp = callerDp;
    }

    public String getCallerDp() {
        return callerDp;
    }

    public void setCallerDp(String callerDp) {
        this.callerDp = callerDp;
    }

    public String getCallTime() {
        return callTime;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }

    public String getSubscriberId() {
        return subscriberId;
    }
    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getCallTyp() {
        return callTyp;
    }
    public void setCallTyp(String callTyp) {
        this.callTyp = callTyp;
    }
    public String getCallDate() {
        return callDate;
    }
    public void setCallDate(String callDate) {
        this.callDate = callDate;
    }
    public String getCallDuration() {
        return callDuration;
    }
    public void setCallDuration(String callDuration) {
        this.callDuration = callDuration;
    }
}
