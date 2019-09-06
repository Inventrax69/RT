package com.inventrax.rt.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DetailsDTO
{
    @SerializedName("SerialNo")
    @Expose
    private String SerialNo;

    @SerializedName("GoodsMovementDetailsIDs")
    @Expose
    private String GoodsMovementDetailsIDs;

    @SerializedName("IsNonConfirmity")
    @Expose
    private String IsNonConfirmity;

    @SerializedName("AsIs")
    @Expose
    private String AsIs;

    @SerializedName("KitPlannerID")
    @Expose
    private String KitPlannerID;

    @SerializedName("ProductCategoryID")
    @Expose
    private String ProductCategoryID;

    @SerializedName("Quantity")
    @Expose
    private String Quantity;

    @SerializedName("BatchNo")
    @Expose
    private String BatchNo;

    @SerializedName("OEMBatchNo")
    @Expose
    private String OEMBatchNo;

    @SerializedName("IsDamaged")
    @Expose
    private String IsDamaged;

    @SerializedName("HasDiscrepancy")
    @Expose
    private String HasDiscrepancy;

    @SerializedName("Location")
    @Expose
    private String Location;

    public String getSerialNo ()
    {
        return SerialNo;
    }

    public void setSerialNo (String SerialNo)
    {
        this.SerialNo = SerialNo;
    }

    public String getGoodsMovementDetailsIDs ()
    {
        return GoodsMovementDetailsIDs;
    }

    public void setGoodsMovementDetailsIDs (String GoodsMovementDetailsIDs)
    {
        this.GoodsMovementDetailsIDs = GoodsMovementDetailsIDs;
    }

    public String getIsNonConfirmity ()
    {
        return IsNonConfirmity;
    }

    public void setIsNonConfirmity (String IsNonConfirmity)
    {
        this.IsNonConfirmity = IsNonConfirmity;
    }

    public String getAsIs ()
    {
        return AsIs;
    }

    public void setAsIs (String AsIs)
    {
        this.AsIs = AsIs;
    }

    public String getKitPlannerID ()
    {
        return KitPlannerID;
    }

    public void setKitPlannerID (String KitPlannerID)
    {
        this.KitPlannerID = KitPlannerID;
    }

    public String getProductCategoryID ()
    {
        return ProductCategoryID;
    }

    public void setProductCategoryID (String ProductCategoryID)
    {
        this.ProductCategoryID = ProductCategoryID;
    }

    public String getQuantity ()
    {
        return Quantity;
    }

    public void setQuantity (String Quantity)
    {
        this.Quantity = Quantity;
    }

    public String getBatchNo ()
    {
        return BatchNo;
    }

    public void setBatchNo (String BatchNo)
    {
        this.BatchNo = BatchNo;
    }

    public String getOEMBatchNo ()
    {
        return OEMBatchNo;
    }

    public void setOEMBatchNo (String OEMBatchNo)
    {
        this.OEMBatchNo = OEMBatchNo;
    }

    public String getIsDamaged ()
    {
        return IsDamaged;
    }

    public void setIsDamaged (String IsDamaged)
    {
        this.IsDamaged = IsDamaged;
    }

    public String getHasDiscrepancy ()
    {
        return HasDiscrepancy;
    }

    public void setHasDiscrepancy (String HasDiscrepancy)
    {
        this.HasDiscrepancy = HasDiscrepancy;
    }

    public String getLocation ()
    {
        return Location;
    }

    public void setLocation (String Location)
    {
        this.Location = Location;
    }

}