package com.android.serviceproviderapplication.Model;

/**
 * Created by AhSaN BaiG on 5/4/2018.
 */

public class ServiceProviderInformation {

    public String spName;
    public String spEmail;
    public String spPhoneNo;
    public String spPassword;
    public String spPhotoUrl;
    public String spProfession;

    public String spAddress;
    public String spExperience;
    public String spRatings;

    public ServiceProviderInformation() {
    }

    public ServiceProviderInformation(String spName, String spEmail, String spPhoneNo, String spPassword, String spProfession, String spAddress, String spExperience) {
        this.spName = spName;
        this.spEmail = spEmail;
        this.spPhoneNo = spPhoneNo;
        this.spPassword = spPassword;
        this.spProfession = spProfession;
        this.spAddress = spAddress;
        this.spExperience = spExperience;
    }

    public String getSpName() {
        return spName;
    }

    public String getSpEmail() {
        return spEmail;
    }

    public String getSpPhoneNo() {
        return spPhoneNo;
    }

    public String getSpPassword() {
        return spPassword;
    }

    public String getSpPhotoUrl() {
        return spPhotoUrl;
    }

    public String getSpProfession() {
        return spProfession;
    }

    public String getSpAddress() {
        return spAddress;
    }

    public String getSpExperience() {
        return spExperience;
    }

    public void setSpName(String spName) {
        this.spName = spName;
    }

    public void setSpEmail(String spEmail) {
        this.spEmail = spEmail;
    }

    public void setSpPhoneNo(String spPhoneNo) {
        this.spPhoneNo = spPhoneNo;
    }

    public void setSpPassword(String spPassword) {
        this.spPassword = spPassword;
    }

    public void setSpPhotoUrl(String spPhotoUrl) {
        this.spPhotoUrl = spPhotoUrl;
    }

    public void setSpProfession(String spProfession) {
        this.spProfession = spProfession;
    }

    public void setSpAddress(String spAddress) {
        this.spAddress = spAddress;
    }

    public void setSpExperience(String spExperience) {
        this.spExperience = spExperience;
    }

    public String getSpRatings() {
        return spRatings;
    }

    public void setSpRatings(String spRatings) {
        this.spRatings = spRatings;
    }
}
