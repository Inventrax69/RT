package com.inventrax.rt.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OBDInfo {

    public OBDInfo() {

    }

    @SerializedName("SOHeaderId")
    @Expose
    private int SOHeaderId;
    @SerializedName("SODetailsId")
    @Expose
    private int SODetailsId;
    @SerializedName("OutboundId")
    @Expose
    private int OutboundId;
    @SerializedName("Password")
    @Expose
    private int Password;
    @SerializedName("ObdNumber")
    @Expose
    private String ObdNumber;
    @SerializedName("MaterialMasterId")
    @Expose
    private String MaterialMasterId;
    @SerializedName("Mcode")
    @Expose
    private String Mcode;
    @SerializedName("OemPartNumber")
    @Expose
    private String OemPartNumber;
    @SerializedName("Description")
    @Expose
    private String Description;
    @SerializedName("SuomId")
    @Expose
    private int SuomId;
    @SerializedName("SoQty")
    @Expose
    private Double SoQty;
    @SerializedName("ConversionFactor")
    @Expose
    private Double ConversionFactor;
    @SerializedName("PickedQty")
    @Expose
    private Double PickedQty;
    @SerializedName("KitCode")
    @Expose
    private String KitCode;
    @SerializedName("IsSkipped")
    @Expose
    private Boolean IsSkipped;
    @SerializedName("UOM")
    @Expose
    private String UOM;
    @SerializedName("DocumentTypeId")
    @Expose
    private String DocumentTypeId;
    @SerializedName("LineNumber")
    @Expose
    private String LineNumber;
    @SerializedName("ProductionOrderHeaderId")
    @Expose
    private String ProductionOrderHeaderId;



    public int getSOHeaderId() {
        return SOHeaderId;
    }

    public void setSOHeaderId(int SOHeaderId) {
        this.SOHeaderId = SOHeaderId;
    }

    public int getSODetailsId() {
        return SODetailsId;
    }

    public void setSODetailsId(int SODetailsId) {
        this.SODetailsId = SODetailsId;
    }

    public int getOutboundId() {
        return OutboundId;
    }

    public void setOutboundId(int outboundId) {
        OutboundId = outboundId;
    }

    public int getPassword() {
        return Password;
    }

    public void setPassword(int password) {
        Password = password;
    }

    public String getObdNumber() {
        return ObdNumber;
    }

    public void setObdNumber(String obdNumber) {
        ObdNumber = obdNumber;
    }

    public String getMaterialMasterId() {
        return MaterialMasterId;
    }

    public void setMaterialMasterId(String materialMasterId) {
        MaterialMasterId = materialMasterId;
    }

    public String getMcode() {
        return Mcode;
    }

    public void setMcode(String mcode) {
        Mcode = mcode;
    }

    public String getOemPartNumber() {
        return OemPartNumber;
    }

    public void setOemPartNumber(String oemPartNumber) {
        OemPartNumber = oemPartNumber;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public int getSuomId() {
        return SuomId;
    }

    public void setSuomId(int suomId) {
        SuomId = suomId;
    }

    public Double getSoQty() {
        return SoQty;
    }

    public void setSoQty(Double soQty) {
        SoQty = soQty;
    }

    public Double getConversionFactor() {
        return ConversionFactor;
    }

    public void setConversionFactor(Double conversionFactor) {
        ConversionFactor = conversionFactor;
    }

    public Double getPickedQty() {
        return PickedQty;
    }

    public void setPickedQty(Double pickedQty) {
        PickedQty = pickedQty;
    }

    public String getKitCode() {
        return KitCode;
    }

    public void setKitCode(String kitCode) {
        KitCode = kitCode;
    }

    public Boolean getSkipped() {
        return IsSkipped;
    }

    public void setSkipped(Boolean skipped) {
        IsSkipped = skipped;
    }

    public String getUOM() {
        return UOM;
    }

    public void setUOM(String UOM) {
        this.UOM = UOM;
    }

    public String getDocumentTypeId() {
        return DocumentTypeId;
    }

    public void setDocumentTypeId(String documentTypeId) {
        DocumentTypeId = documentTypeId;
    }

    public String getLineNumber() {
        return LineNumber;
    }

    public void setLineNumber(String lineNumber) {
        LineNumber = lineNumber;
    }

    public String getProductionOrderHeaderId() {
        return ProductionOrderHeaderId;
    }

    public void setProductionOrderHeaderId(String productionOrderHeaderId) {
        ProductionOrderHeaderId = productionOrderHeaderId;
    }
}
