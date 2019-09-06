package com.inventrax.rt.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LiveStock {


    @SerializedName("OEMPartNo")
    @Expose
    private String OEMPartNo;
    @SerializedName("AvailableQty")
    @Expose
    private Double AvailableQty;
    @SerializedName("IsDamaged")
    @Expose
    private String IsDamaged;
    @SerializedName("HasDiscrepancy")
    @Expose
    private String HasDiscrepancy;
    @SerializedName("IsNonConfirmity")
    @Expose
    private String IsNonConfirmity;
    @SerializedName("AsIs")
    @Expose
    private String AsIs;
    @SerializedName("IsPositiveRecall")
    @Expose
    private String IsPositiveRecall;
    @SerializedName("MfgDate")
    @Expose
    private String MfgDate;
    @SerializedName("ExpDate")
    @Expose
    private String ExpDate;
    @SerializedName("SerialNo")
    @Expose
    private String SerialNo;
    @SerializedName("BatchNo")
    @Expose
    private String BatchNo;
    @SerializedName("OEMBatchNo")
    @Expose
    private String OEMBatchNo;
    @SerializedName("Location")
    @Expose
    private String Location;

    public LiveStock() {

    }

    public String getOEMPartNo() {
        return OEMPartNo;
    }

    public void setOEMPartNo(String OEMPartNo) {
        this.OEMPartNo = OEMPartNo;
    }

    public Double getAvailableQty() {
        return AvailableQty;
    }

    public void setAvailableQty(Double availableQty) {
        AvailableQty = availableQty;
    }

    public String getIsDamaged() {
        return IsDamaged;
    }

    public void setIsDamaged(String isDamaged) {
        IsDamaged = isDamaged;
    }

    public String getHasDiscrepancy() {
        return HasDiscrepancy;
    }

    public void setHasDiscrepancy(String hasDiscrepancy) {
        HasDiscrepancy = hasDiscrepancy;
    }

    public String getIsNonConfirmity() {
        return IsNonConfirmity;
    }

    public void setIsNonConfirmity(String isNonConfirmity) {
        IsNonConfirmity = isNonConfirmity;
    }

    public String getAsIs() {
        return AsIs;
    }

    public void setAsIs(String asIs) {
        AsIs = asIs;
    }

    public String getIsPositiveRecall() {
        return IsPositiveRecall;
    }

    public void setIsPositiveRecall(String isPositiveRecall) {
        IsPositiveRecall = isPositiveRecall;
    }

    public String getMfgDate() {
        return MfgDate;
    }

    public void setMfgDate(String mfgDate) {
        MfgDate = mfgDate;
    }

    public String getExpDate() {
        return ExpDate;
    }

    public void setExpDate(String expDate) {
        ExpDate = expDate;
    }

    public String getSerialNo() {
        return SerialNo;
    }

    public void setSerialNo(String serialNo) {
        SerialNo = serialNo;
    }

    public String getBatchNo() {
        return BatchNo;
    }

    public void setBatchNo(String batchNo) {
        BatchNo = batchNo;
    }

    public String getOEMBatchNo() {
        return OEMBatchNo;
    }

    public void setOEMBatchNo(String OEMBatchNo) {
        this.OEMBatchNo = OEMBatchNo;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }
}
