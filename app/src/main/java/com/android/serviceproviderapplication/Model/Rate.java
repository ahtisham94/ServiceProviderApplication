package com.android.serviceproviderapplication.Model;

/**
 * Created by AhSaN BaiG on 5/24/2018.
 */

public class Rate {
    String rates;
    String comments;

    public Rate() {
    }

    public Rate(String rates, String comments) {
        this.rates = rates;
        this.comments = comments;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
