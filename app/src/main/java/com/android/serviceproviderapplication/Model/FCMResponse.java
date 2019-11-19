package com.android.serviceproviderapplication.Model;

import java.util.List;

/**
 * Created by Ahtisham Alam on 4/23/2018.
 */

public class FCMResponse {
    public long multicast_id;
    public int success;
    public int failure;
    public int cononical_id;
    public List<Result> results;

    public FCMResponse() {
    }

    public FCMResponse(long multicast_id,int success,int failure,int cononical_id,List<Result> results) {
        this.multicast_id = multicast_id;
        this.success = success;
        this.failure = failure;
        this.cononical_id = cononical_id;
        this.results = results;
    }


    public long getMulticast_id() {
        return multicast_id;
    }

    public void setMulticast_id(long multicast_id) {
        this.multicast_id = multicast_id;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public int getCononical_id() {
        return cononical_id;
    }

    public void setCononical_id(int cononical_id) {
        this.cononical_id = cononical_id;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }
}
