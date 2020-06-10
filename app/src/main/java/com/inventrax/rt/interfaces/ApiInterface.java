package com.inventrax.rt.interfaces;

import com.inventrax.rt.model.PickReqDTO;
import com.inventrax.rt.pojos.LiveStock;
import com.inventrax.rt.pojos.OBDInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface ApiInterface {


    @GET("api/getsettings")
    Call<String> getSettings();


    @GET("api/login/{UserName}/{Password}/{LoginIPAddress}")
    Call<String> get(
            @Query("UserName") String UserName,
            @Query("Password") String Password,
            @Query("LoginIPAddress") String LoginIPAddress);

    @GET("api/getlivestock/{mcode}/{location}/{batchnumber}")
    Call<List<LiveStock>> getlivestock(
            @Query("mcode") String mcode,
            @Query("location") String location,
            @Query("batchnumber") String batchnumber);

    @GET("api/Intertransfer/{MaterialCode}/{Location}")
    Call<String> Getintertransferdata(
            @Query("MaterialCode") String mcode,
            @Query("Location") String location);

    @GET("api/Internaltransfer/{vGMDIDs}/{Location}/{vQuantity}/{UserID}/{vMcode}")
    Call<String> Internaltransfer(
            @Query("vGMDIDs") String gmdId,
            @Query("Location") String location,
            @Query("vQuantity") String qty,
            @Query("UserID") String userId,
            @Query("vMcode") String mcode);



    @GET("api/GetPendingItemList/{obdNumberList}")
    Call<List<OBDInfo>> GetPendingItemList(@Query("obdNumberList") String obdNumberList);

    @GET("api/GetDataSet/{sqlString}")
    Call<String> GetDataSet(@Query("sqlString") String sqlquery);

    @POST("api/ProcessPickRequest")
    Call<String> ProcessPickRequest(@Body PickReqDTO pickReqDTO);

    @GET("api/RePrintCheck/{OBDNumber}/{PartNumber}")
    Call<String> RePrintCheck(
            @Query("OBDNumber") String OBDNumber,
            @Query("PartNumber") String PartNumber);

    @POST("api/RePrint")
    Call<String> RePrint(@Body PickReqDTO pickReqDTO);

}