package com.inventrax.rt.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginDTO {

    public LoginDTO(){

    }

    @SerializedName("UserID")
    @Expose
    private int UserID;
    @SerializedName("FirstName")
    @Expose
    private String FirstName;
    @SerializedName("LastName")
    @Expose
    private String LastName;
    @SerializedName("Password")
    @Expose
    private String Password;
    @SerializedName("Roles")
    @Expose
    private String Roles;
    @SerializedName("Warehouses")
    @Expose
    private String Warehouses;
    @SerializedName("SiteCodes")
    @Expose
    private String SiteCodes;
    @SerializedName("DepartmentIDs")
    @Expose
    private String DepartmentIDs;
    @SerializedName("TenantID")
    @Expose
    private String TenantID;
    @SerializedName("MachineIPAddress")
    @Expose
    private String MachineIPAddress;


    public int getUserID() {
        return UserID;
    }

    public void setUserID(int userID) {
        UserID = userID;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getRoles() {
        return Roles;
    }

    public void setRoles(String roles) {
        Roles = roles;
    }

    public String getWarehouses() {
        return Warehouses;
    }

    public void setWarehouses(String warehouses) {
        Warehouses = warehouses;
    }

    public String getSiteCodes() {
        return SiteCodes;
    }

    public void setSiteCodes(String siteCodes) {
        SiteCodes = siteCodes;
    }

    public String getDepartmentIDs() {
        return DepartmentIDs;
    }

    public void setDepartmentIDs(String departmentIDs) {
        DepartmentIDs = departmentIDs;
    }

    public String getTenantID() {
        return TenantID;
    }

    public void setTenantID(String tenantID) {
        TenantID = tenantID;
    }

    public String getMachineIPAddress() {
        return MachineIPAddress;
    }

    public void setMachineIPAddress(String machineIPAddress) {
        MachineIPAddress = machineIPAddress;
    }
}
