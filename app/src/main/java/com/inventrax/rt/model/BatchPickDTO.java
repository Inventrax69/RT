package com.inventrax.rt.model;

import java.io.Serializable;

public class BatchPickDTO implements Serializable {

    private String Location;
    private String BatchNo;
    private String AvailableQuantity;
    private String OEMBatchNo;
    private String MfgDate;
    private String ExpDate;
    private String SameWithSODetails;
    private String RefLocation;
    private String IsDamaged;
    private String SerialNo;
    private String CalibrationDate;
    private String CalibrationDueDate;
    private String Plant;
    private String StockType;
    private String EndofLifeDate;



    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getBatchNo() {
        return BatchNo;
    }

    public void setBatchNo(String batchNo) {
        BatchNo = batchNo;
    }

    public String getAvailableQuantity() {
        return AvailableQuantity;
    }

    public void setAvailableQuantity(String availableQuantity) {
        AvailableQuantity = availableQuantity;
    }

    public String getOEMBatchNo() {
        return OEMBatchNo;
    }

    public void setOEMBatchNo(String OEMBatchNo) {
        this.OEMBatchNo = OEMBatchNo;
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

    public String getSameWithSODetails() {
        return SameWithSODetails;
    }

    public void setSameWithSODetails(String sameWithSODetails) {
        SameWithSODetails = sameWithSODetails;
    }

    public String getRefLocation() {
        return RefLocation;
    }

    public void setRefLocation(String refLocation) {
        RefLocation = refLocation;
    }

    public String getIsDamaged() {
        return IsDamaged;
    }

    public void setIsDamaged(String isDamaged) {
        IsDamaged = isDamaged;
    }

    public String getSerialNo() {
        return SerialNo;
    }

    public void setSerialNo(String serialNo) {
        SerialNo = serialNo;
    }

    public String getCalibrationDate() {
        return CalibrationDate;
    }

    public void setCalibrationDate(String calibrationDate) {
        CalibrationDate = calibrationDate;
    }

    public String getCalibrationDueDate() {
        return CalibrationDueDate;
    }

    public void setCalibrationDueDate(String calibrationDueDate) {
        CalibrationDueDate = calibrationDueDate;
    }

    public String getPlant() {
        return Plant;
    }

    public void setPlant(String plant) {
        Plant = plant;
    }

    public String getStockType() {
        return StockType;
    }

    public void setStockType(String stockType) {
        StockType = stockType;
    }

    public String getEndofLifeDate() {
        return EndofLifeDate;
    }

    public void setEndofLifeDate(String endofLifeDate) {
        EndofLifeDate = endofLifeDate;
    }
}