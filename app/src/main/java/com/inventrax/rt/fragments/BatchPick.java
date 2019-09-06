package com.inventrax.rt.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cipherlab.barcode.GeneralString;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;
import com.inventrax.rt.R;
import com.inventrax.rt.adapters.PickingAdapter;
import com.inventrax.rt.common.Common;
import com.inventrax.rt.common.constants.ErrorMessages;
import com.inventrax.rt.interfaces.ApiInterface;
import com.inventrax.rt.model.BatchPickDTO;
import com.inventrax.rt.model.PickReqDTO;
import com.inventrax.rt.pojos.OBDInfo;
import com.inventrax.rt.services.RestService;
import com.inventrax.rt.util.DialogUtils;
import com.inventrax.rt.util.ExceptionLoggerUtils;
import com.inventrax.rt.util.FragmentUtils;
import com.inventrax.rt.util.ProgressDialogUtils;
import com.inventrax.rt.util.ScanValidator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by karthik.m on 05/08/2018.
 */

public class BatchPick extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {


    private static final String classCode = "API_FRAG_LIVESTOCK";
    private View rootView;

    private TextInputLayout txtInputLayoutDDNo, txtInputLayoutQty;
    private EditText etDDNo, etQty;
    private Button btnADD, btnCloseOne, btnClearList, btnStart, btnClear, btnBack, btnSkip, btnPick, btnRePrint;
    private TextView lblKitCode, lblDDNo, lblPart, lblOEMPart, lblUOM, lblSerialNo, lblReqQty, lblPkdQty;
    private RecyclerView listPick;
    private LinearLayoutManager linearLayoutManager;
    private CheckBox cbPrint;
    private ListView lvDDNo;
    private RelativeLayout rlAddVLPD, rlPicking;

    String scanner = null;
    private IntentFilter filter;

    String getScanner = null;
    private Gson gson;
    private ScanValidator scanValidator;
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private Common common;
    int listSize = 0;
    static int index = 0, salesUOMId = 0, soDetailsId = 0, outbondId = 0, soHeaderId = 0, currentIndex = 0;
    String selectedLocation = "", selectedBatch = "", qtyInSelectedLocation = "", selectedOEMno = "", selectedSerialNo = "", selectedMfg = "", selectedExp = "";
    double conversationFactor = 0.0;

    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;

    private String userId = "", accountId = "", printer = "",
             uom = "", obdNum = "", mmId = "", lineNumber = "",
            description = "", OEMNumber = "", productionOrderHeaderId = "", SameWithSODetails = "", RefLocation = "", IsDamaged = "";

    private static String dDoc = "";

    private List<String> deliveryDocNo;
    List<OBDInfo> responseList;
    String query;

    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public BatchPick() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_batchpick, container, false);

        loadFormControls();

        return rootView;
    }

    private void loadFormControls() {

        SharedPreferences sp = getActivity().getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        printer = sp.getString("printer", "");

        SharedPreferences sp1 = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp1.getString("UserId", "");
        accountId = sp1.getString("AccountId", "");

        listPick = (RecyclerView) rootView.findViewById(R.id.listPick);
        listPick.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());

        // use a linear layout manager
        listPick.setLayoutManager(linearLayoutManager);

        cbPrint = (CheckBox) rootView.findViewById(R.id.cbPrint);

        lblKitCode = (TextView) rootView.findViewById(R.id.lblKitCode);
        lblDDNo = (TextView) rootView.findViewById(R.id.lblDDNo);
        lblPart = (TextView) rootView.findViewById(R.id.lblPart);
        lblOEMPart = (TextView) rootView.findViewById(R.id.lblOEMPart);
        lblUOM = (TextView) rootView.findViewById(R.id.lblUOM);
        lblSerialNo = (TextView) rootView.findViewById(R.id.lblSerialNo);
        lblReqQty = (TextView) rootView.findViewById(R.id.lblReqQty);
        lblPkdQty = (TextView) rootView.findViewById(R.id.lblPkdQty);

        txtInputLayoutDDNo = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutDDNo);
        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);

        rlAddVLPD = (RelativeLayout) rootView.findViewById(R.id.rlAddVLPD);
        rlPicking = (RelativeLayout) rootView.findViewById(R.id.rlPicking);

        etDDNo = (EditText) rootView.findViewById(R.id.etDDNo);
        etQty = (EditText) rootView.findViewById(R.id.etQty);

        btnADD = (Button) rootView.findViewById(R.id.btnADD);
        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);
        btnStart = (Button) rootView.findViewById(R.id.btnStart);
        btnClearList = (Button) rootView.findViewById(R.id.btnClearList);
        btnClear = (Button) rootView.findViewById(R.id.btnClear);
        btnBack = (Button) rootView.findViewById(R.id.btnBack);
        btnSkip = (Button) rootView.findViewById(R.id.btnSkip);
        btnPick = (Button) rootView.findViewById(R.id.btnPick);
        btnRePrint = (Button) rootView.findViewById(R.id.btnRePrint);

        lvDDNo = (ListView) rootView.findViewById(R.id.lvDDNo);
        lvDDNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDeliveryDoc = lvDDNo.getItemAtPosition(position).toString();
                getPendingItemList(selectedDeliveryDoc);
            }
        });


        btnADD.setOnClickListener(this);
        btnCloseOne.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnClearList.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
        btnPick.setOnClickListener(this);
        btnRePrint.setOnClickListener(this);

        deliveryDocNo = new ArrayList<String>();
        responseList = new ArrayList<OBDInfo>();

        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);


        common = new Common();
        gson = new GsonBuilder().create();


        //For Honeywell
        AidcManager.create(getActivity(), new AidcManager.CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {

                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
                try {
                    barcodeReader.claim();
                    HoneyWellBarcodeListeners();

                } catch (ScannerUnavailableException e) {
                    e.printStackTrace();
                }
            }
        });


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCloseOne:
                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                break;

            case R.id.btnADD:

                if (!etDDNo.getText().toString().isEmpty()) {

                    deliveryDocNo.add(etDDNo.getText().toString());

                    ArrayAdapter<String> listAdp = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, deliveryDocNo);

                    lvDDNo.setAdapter(listAdp);
                    etDDNo.setText("");

                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0010, getActivity(), getContext(), "Error");
                }

                break;

            case R.id.btnStart:

                //dDoc = "";

                for (String s : deliveryDocNo) {
                    dDoc = dDoc + s + ",";
                }

                if (dDoc.equals("")) {

                } else {

                    getPendingItemList(dDoc);
                }

                break;

            case R.id.btnClearList:

                etDDNo.setText("");
                dDoc = "";

                deliveryDocNo.clear();
                lvDDNo.setAdapter(null);

                break;

            case R.id.btnClear:

                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());

                break;

            case R.id.btnBack:

                selectedLocation = "";
                selectedBatch = "";
                qtyInSelectedLocation = "";
                etQty.setText("");
                selectedOEMno = "";
                selectedSerialNo = "";
                selectedMfg = "";
                selectedExp = "";

                index = Integer.parseInt(lblSerialNo.getText().toString()) - 1;

                if (index != 0) {
                    index--;

                } else if (index == 0) {
                    index = listSize - 1;

                }

                lblKitCode.setText(responseList.get(index).getKitCode());
                lblDDNo.setText(responseList.get(index).getObdNumber());
                lblPart.setText(responseList.get(index).getMcode());
                lblUOM.setText(responseList.get(index).getUOM());
                lblReqQty.setText(String.valueOf(responseList.get(index).getSoQty()));
                lblPkdQty.setText(String.valueOf(responseList.get(index).getPickedQty()));
                lblOEMPart.setText(responseList.get(index).getOemPartNumber());


                lblPkdQty.setBackgroundColor(getResources().getColor(R.color.red));

                if (lblReqQty.getText().toString().equalsIgnoreCase(lblPkdQty.getText().toString())) {
                    lblPkdQty.setBackgroundColor(getResources().getColor(R.color.green));
                }
                conversationFactor = responseList.get(index).getConversionFactor();
                uom = responseList.get(index).getUOM();
                obdNum = responseList.get(index).getObdNumber();
                mmId = responseList.get(index).getMaterialMasterId();
                salesUOMId = responseList.get(index).getSuomId();
                soDetailsId = responseList.get(index).getSODetailsId();
                outbondId = responseList.get(index).getOutboundId();
                lineNumber = responseList.get(index).getLineNumber();
                soHeaderId = responseList.get(index).getSOHeaderId();
                description = responseList.get(index).getDescription();
                OEMNumber = responseList.get(index).getOemPartNumber();
                productionOrderHeaderId = responseList.get(index).getProductionOrderHeaderId();

                lblSerialNo.setText(String.valueOf(index + 1));
                //index =0;

                query = "EXEC [dbo].[sp_INV_GetStockInList_For_HHT]@MaterialMasterID=" + responseList.get(index).getMaterialMasterId() + ",@OutBoundID=" + responseList.get(index).getOutboundId();

                getDataSet(query);

                break;


            case R.id.btnSkip:

                etQty.setText("");
                selectedLocation = "";
                selectedBatch = "";
                qtyInSelectedLocation = "";
                selectedOEMno = "";
                selectedSerialNo = "";
                selectedMfg = "";
                selectedExp = "";
                index = Integer.parseInt(lblSerialNo.getText().toString()) - 1;

                if (index == listSize - 1) {

                    index = 0;
                } else {
                    index++;
                }


                lblKitCode.setText(responseList.get(index).getKitCode());
                lblDDNo.setText(responseList.get(index).getObdNumber());
                lblPart.setText(responseList.get(index).getMcode());
                lblUOM.setText(responseList.get(index).getUOM());
                lblReqQty.setText(String.valueOf(responseList.get(index).getSoQty()));
                lblPkdQty.setText(String.valueOf(responseList.get(index).getPickedQty()));
                lblOEMPart.setText(responseList.get(index).getOemPartNumber());


                lblPkdQty.setBackgroundColor(getResources().getColor(R.color.red));

                if (lblReqQty.getText().toString().equalsIgnoreCase(lblPkdQty.getText().toString())) {

                    lblPkdQty.setBackgroundColor(getResources().getColor(R.color.green));
                }
                conversationFactor = responseList.get(index).getConversionFactor();
                uom = responseList.get(index).getUOM();
                obdNum = responseList.get(index).getObdNumber();
                mmId = responseList.get(index).getMaterialMasterId();
                salesUOMId = responseList.get(index).getSuomId();
                soDetailsId = responseList.get(index).getSODetailsId();
                outbondId = responseList.get(index).getOutboundId();
                lineNumber = responseList.get(index).getLineNumber();
                soHeaderId = responseList.get(index).getSOHeaderId();
                description = responseList.get(index).getDescription();
                OEMNumber = responseList.get(index).getOemPartNumber();
                productionOrderHeaderId = responseList.get(index).getProductionOrderHeaderId();

                lblSerialNo.setText(String.valueOf(index + 1));

                query = "EXEC [dbo].[sp_INV_GetStockInList_For_HHT]@MaterialMasterID=" + responseList.get(index).getMaterialMasterId() + ",@OutBoundID=" + responseList.get(index).getOutboundId();
                //index =0;
                getDataSet(query);

                break;

            case R.id.btnPick:

                if (!printer.equalsIgnoreCase("")) {

                    try {
                        if (!etQty.getText().toString().equalsIgnoreCase("0") && !etQty.getText().toString().isEmpty()) {
                            if (Double.parseDouble(etQty.getText().toString()) <= 0) {
                                common.showUserDefinedAlertType(errorMessages.EMC_0017, getActivity(), getContext(), "Error");
                            } else {
                                double qty = Double.parseDouble(etQty.getText().toString().trim());

                                if (qtyInSelectedLocation.equals("")) {
                                    common.showUserDefinedAlertType(errorMessages.EMC_0012, getActivity(), getContext(), "Error");
                                    return;
                                }

                                if (qty > Double.parseDouble(qtyInSelectedLocation)) {
                                    common.showUserDefinedAlertType(errorMessages.EMC_0009 + " " + qtyInSelectedLocation, getActivity(), getContext(), "Error");
                                } else {

                                    if (!selectedLocation.equals("")) {

                                        DialogUtils.showConfirmDialog(getActivity(), "Info", "You are picking" + " " + uom + " - " +
                                                qty + "at location" + selectedLocation, new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        itemPickRequest();
                                                        common.setIsPopupActive(false);
                                                        break;

                                                    case DialogInterface.BUTTON_NEGATIVE:
                                                        common.setIsPopupActive(false);
                                                        break;
                                                }

                                            }
                                        });
                                    } else {
                                        common.showUserDefinedAlertType(errorMessages.EMC_0012, getActivity(), getContext(), "Error");
                                    }

                                }
                            }
                        } else {
                            common.showUserDefinedAlertType(errorMessages.EMC_0018, getActivity(), getContext(), "Error");
                        }
                    } catch (Exception e) {
                        // common.showUserDefinedAlertType(errorMessages.EMC_0018, getActivity(), getContext(), "Error");
                    }

                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0006, getActivity(), getContext(), "Error");
                }

                break;

            case R.id.btnRePrint:
                if (!selectedLocation.equalsIgnoreCase("")) {
                    rePrintCheck();
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0012, getActivity(), getContext(), "Error");
                }
                break;

        }
    }


    // honeywell
    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // update UI to reflect the data
                getScanner = barcodeReadEvent.getBarcodeData();
                ProcessScannedinfo(getScanner);

            }

        });
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }

    @Override
    public void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent) {

    }

    //Honeywell Barcode reader Properties
    public void HoneyWellBarcodeListeners() {

        barcodeReader.addTriggerListener(this);

        if (barcodeReader != null) {
            // set the trigger mode to client control
            barcodeReader.addBarcodeListener(this);
            try {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
            } catch (UnsupportedPropertyException e) {
                // Toast.makeText(this, "Failed to apply properties", Toast.LENGTH_SHORT).show();
            }

            Map<String, Object> properties = new HashMap<String, Object>();
            // Set Symbologies On/Off
            properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
            // Set Max Code 39 barcode length
            properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
            // Turn on center decoding
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Enable bad read response
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);
            // Apply the settings
            barcodeReader.setProperties(properties);
        }
    }


    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {

        if (scannedData != null) {

            if (scannedData.length() == 9) {
                etDDNo.setText(scannedData.trim());
            }

        }
    }


    public void getPendingItemList(String deliveryDocNo) {

        ProgressDialogUtils.showProgressDialog("Please wait..");

        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
        Call<List<OBDInfo>> call = null;
        call = apiService.GetPendingItemList(deliveryDocNo);

        call.enqueue(new Callback<List<OBDInfo>>() {
            @Override
            public void onResponse(Call<List<OBDInfo>> call, Response<List<OBDInfo>> response) {

                ProgressDialogUtils.closeProgressDialog();

                if (response.body() != null) {

                    Gson gson = new Gson();
                    String json = gson.toJson(response.body());

                    responseList = new ArrayList<OBDInfo>();

                    //Log.v("ERT",new Gson().toJson(responseList));

                    for (OBDInfo list : response.body()) {
                        Log.i("onResponse", list.toString());
                        responseList.add(list);
                    }


                    if (responseList.size() > 0) {
                        rlAddVLPD.setVisibility(View.GONE);
                        rlPicking.setVisibility(View.VISIBLE);


                        if (currentIndex > 0) {
                            index = currentIndex;
                        } else if (currentIndex == 0) {
                            index = 0;
                        } else {
                            index = 0;
                        }


                        listSize = responseList.size();

                        if (lblKitCode.getText().toString().equalsIgnoreCase(null) || lblKitCode.getText().toString().equals("") || lblKitCode.getText().toString().equalsIgnoreCase("null")) {

                        } else {

                            cbPrint.setChecked(true);

                            lblKitCode.setText(responseList.get(index).getKitCode());
                            lblDDNo.setText(responseList.get(index).getObdNumber());
                            lblPart.setText(responseList.get(index).getMcode());
                            lblUOM.setText(responseList.get(index).getUOM());
                            lblReqQty.setText(String.valueOf(responseList.get(index).getSoQty()));
                            lblPkdQty.setText(String.valueOf(responseList.get(index).getPickedQty()));
                            lblOEMPart.setText(responseList.get(index).getOemPartNumber());


                            lblPkdQty.setBackgroundColor(getResources().getColor(R.color.red));

                            if (lblReqQty.getText().toString().equalsIgnoreCase(lblPkdQty.getText().toString())) {

                                lblPkdQty.setBackgroundColor(getResources().getColor(R.color.green));
                            }

                            lblSerialNo.setText(String.valueOf(index + 1));
                            conversationFactor = responseList.get(index).getConversionFactor();
                            uom = responseList.get(index).getUOM();
                            obdNum = responseList.get(index).getObdNumber();
                            mmId = responseList.get(index).getMaterialMasterId();
                            salesUOMId = responseList.get(index).getSuomId();
                            soDetailsId = responseList.get(index).getSODetailsId();
                            outbondId = responseList.get(index).getOutboundId();
                            lineNumber = responseList.get(index).getLineNumber();
                            soHeaderId = responseList.get(index).getSOHeaderId();
                            description = responseList.get(index).getDescription();
                            OEMNumber = responseList.get(index).getOemPartNumber();
                            productionOrderHeaderId = responseList.get(index).getProductionOrderHeaderId();

                            query = "EXEC [dbo].[sp_INV_GetStockInList_For_HHT]@MaterialMasterID=" + responseList.get(index).getMaterialMasterId() + ",@OutBoundID=" + responseList.get(index).getOutboundId();

                            getDataSet(query);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<OBDInfo>> call, Throwable throwable) {
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0008, getActivity(), getContext(), "Warning");

                etDDNo.setText("");
                //dDoc = "";
            }
        });

    }

    public void getDataSet(String query) {

        ProgressDialogUtils.showProgressDialog("Please wait..");

        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
        Call<String> call = null;
        call = apiService.GetDataSet(query);

        call.enqueue(new Callback<String>() {


            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                ProgressDialogUtils.closeProgressDialog();

                if (response.body() != null) {

                    Gson gson = new Gson();
                    String json = gson.toJson(response.body());

                    Log.i("onResponse", response.toString());

                    String jsons = response.body(); //gson.toJson(response.body());
                    JSONObject jsonobject = null;

                    try {
                        // get JSONObject from JSON file
                        JSONObject obj = new JSONObject(jsons);
                        // fetch JSONObject named Table
                        JSONArray jsonArray = new JSONArray(obj.getString("Table"));
                        // get available list

                        final List<BatchPickDTO> lstBatchPick = new ArrayList<BatchPickDTO>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonobject = jsonArray.getJSONObject(i);

                            String location = jsonobject.getString("Location");

                            BatchPickDTO batchPickDTO = new BatchPickDTO();
                            batchPickDTO.setLocation(jsonobject.getString("Location"));
                            batchPickDTO.setAvailableQuantity(jsonobject.getString("AvailableQuantity"));
                            batchPickDTO.setBatchNo(jsonobject.getString("BatchNo"));
                            batchPickDTO.setOEMBatchNo(jsonobject.getString("OEMBatchNo"));
                            batchPickDTO.setSameWithSODetails(jsonobject.getString("SameWithSODetails"));
                            batchPickDTO.setRefLocation(jsonobject.getString("RefLocation"));
                            batchPickDTO.setIsDamaged(jsonobject.getString("IsDamaged"));
                            batchPickDTO.setSerialNo(jsonobject.getString("SerialNo"));
                            if (jsonobject.toString().contains("MfgDate")) {
                                batchPickDTO.setMfgDate(jsonobject.getString("MfgDate"));
                            }
                            if (jsonobject.toString().contains("ExpDate")) {
                                batchPickDTO.setExpDate(jsonobject.getString("ExpDate"));
                            }

                            if (batchPickDTO.getSameWithSODetails().equalsIgnoreCase("1") && batchPickDTO.getRefLocation().equalsIgnoreCase("1") && batchPickDTO.getIsDamaged().equalsIgnoreCase("No")) {
                                lstBatchPick.add(batchPickDTO);
                            }
                        }


                        PickingAdapter adapter = new PickingAdapter(getContext(), lstBatchPick, new PickingAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClicked(int position, Object object) {
                                // Handle Object of list item here

                                selectedBatch = lstBatchPick.get(position).getBatchNo();
                                selectedLocation = lstBatchPick.get(position).getLocation();
                                qtyInSelectedLocation = lstBatchPick.get(position).getAvailableQuantity();
                                selectedOEMno = lstBatchPick.get(position).getOEMBatchNo();
                                selectedSerialNo = lstBatchPick.get(position).getSerialNo();
                                selectedExp = lstBatchPick.get(position).getExpDate();
                                selectedMfg = lstBatchPick.get(position).getMfgDate();


                                //Toast.makeText(getContext(),lstBatchPick.get(position).getLocation() + "/" +lstBatchPick.get(position).getBatchNo(),Toast.LENGTH_SHORT).show();

                            }
                        });

                        listPick.setAdapter(adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        ProgressDialogUtils.closeProgressDialog();
                    }

                    ProgressDialogUtils.closeProgressDialog();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable throwable) {
                common.showUserDefinedAlertType(errorMessages.EMC_0008, getActivity(), getContext(), "Warning");
                ProgressDialogUtils.closeProgressDialog();
            }
        });
    }

    public void itemPickRequest() {

        ProgressDialogUtils.showProgressDialog("Please wait..");

        btnPick.setEnabled(false);

        PickReqDTO pickReqDTO = new PickReqDTO();
        pickReqDTO.setQty(Double.parseDouble(etQty.getText().toString()));
        pickReqDTO.setBatchNumber(selectedBatch);
        pickReqDTO.setSerialnumber("");
        pickReqDTO.setLocation(selectedLocation);
        pickReqDTO.setMfgdate("");
        pickReqDTO.setEXpdate("");
        pickReqDTO.setDeliveryDocNumber(obdNum);
        pickReqDTO.setMmid(mmId);
        pickReqDTO.setSalesUOMId(String.valueOf(salesUOMId));
        pickReqDTO.setSoDetailsId(String.valueOf(soDetailsId));
        pickReqDTO.setOutbondId(String.valueOf(outbondId));
        pickReqDTO.setSoHeaderId(String.valueOf(soHeaderId));
        pickReqDTO.setUserId(userId);

        String mspVal = "";


        if (selectedMfg != null && !selectedMfg.equals("null")) {
            mspVal += selectedMfg + ",";
        } else {

            mspVal += "y" + ",";
        }

        if (selectedExp != null && !selectedExp.equals("null")) {
            mspVal += selectedExp + ",";
        } else {
            mspVal += "y" + ",";
        }

        if (selectedSerialNo != null && !selectedSerialNo.equals("null")) {
            mspVal += selectedSerialNo + ",";
        } else {
            mspVal += "y" + ",";
        }

        if (selectedBatch != null && !selectedBatch.isEmpty() && !selectedBatch.equals("null")) {
            mspVal += selectedBatch + ",";
        } else {
            mspVal += "y" + ",";
        }

        if (selectedOEMno != null && !selectedOEMno.equals("null")) {
            mspVal += selectedOEMno;
        } else {
            mspVal += "y" + ",";
        }

        mspVal = mspVal.replaceAll("y,", "");

        //pickReqDTO.setMspValues(selectedMfg + "," + selectedExp + "," + selectedSerialNo + "," + selectedBatch + "," + selectedOEMno);
        pickReqDTO.setMspValues(mspVal);
        pickReqDTO.setPrintRequired(cbPrint.isChecked());
        pickReqDTO.setPrinterIP(printer);
        pickReqDTO.setRemarks("");
        pickReqDTO.setSOQty(Double.parseDouble(lblReqQty.getText().toString()));
        pickReqDTO.setPickedQty(Double.parseDouble(lblPkdQty.getText().toString()));
        pickReqDTO.setMCode(lblPart.getText().toString());
        pickReqDTO.setDescription(description);
        pickReqDTO.setOEMNumber(OEMNumber);
        pickReqDTO.setUOM(uom);
        pickReqDTO.setKitCode(lblKitCode.getText().toString());
        pickReqDTO.setProductionOrderHeaderId(productionOrderHeaderId);
        pickReqDTO.setLineNumber(lineNumber);
        pickReqDTO.setUserId(userId);
        pickReqDTO.setConversionfactor(conversationFactor);


        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

        Call<String> call = null;
        call = apiService.ProcessPickRequest(pickReqDTO);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                ProgressDialogUtils.closeProgressDialog();

                if (response.body() != null) {

                    try {
                        JSONObject movieObject = new JSONObject(response.body());
                        String status = movieObject.getString("status");
                        if (status.equalsIgnoreCase("true")) {
                            currentIndex = index;
                            index = 0;
                            common.showUserDefinedAlertType("Picked Successfully", getActivity(), getContext(), "Success");
                            etQty.setText("");
                            getPendingItemList(dDoc);
                        } else {
                            common.showUserDefinedAlertType(movieObject.getString("responceMessage"), getActivity(), getContext(), "Warning");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    btnPick.setEnabled(true);


                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable throwable) {
                ProgressDialogUtils.closeProgressDialog();
                getPendingItemList(dDoc);
                common.showUserDefinedAlertType(errorMessages.EMC_0008, getActivity(), getContext(), "Warning");
                btnPick.setEnabled(true);
            }
        });

    }

    public void rePrintCheck() {

        ProgressDialogUtils.showProgressDialog("Please wait..");

        String OBDNumber, partNo;

        OBDNumber = obdNum;
        partNo = lblPart.getText().toString();

        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

        Call<String> call = null;
        call = apiService.RePrintCheck(OBDNumber, partNo);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                ProgressDialogUtils.closeProgressDialog();

                if (response.body() != null) {

                    Gson gson = new Gson();
                    String json = gson.toJson(response.body());
                    Log.i("onResponse", response.body().toString());

                    if (!response.body().toString().equalsIgnoreCase("0.0")) {

                        etQty.setText(response.body().toString());
                        rePrint();

                    } else {

                        common.showUserDefinedAlertType("Quantity in selected location" + " " + selectedLocation + " " + "is " + response.body().toString(), getActivity(), getContext(), "Warning");
                    }

                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable throwable) {

                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0008, getActivity(), getContext(), "Warning");
                btnPick.setEnabled(true);
            }
        });

    }

    public void rePrint() {

        ProgressDialogUtils.showProgressDialog("Please wait..");

        PickReqDTO pickReqDTO = new PickReqDTO();
        pickReqDTO.setBatchNumber(selectedBatch);
        pickReqDTO.setQty(Double.parseDouble(etQty.getText().toString()));
        pickReqDTO.setSerialnumber("");
        pickReqDTO.setLocation(selectedLocation);
        pickReqDTO.setMfgdate("");
        pickReqDTO.setEXpdate("");
        pickReqDTO.setDeliveryDocNumber(obdNum);
        pickReqDTO.setMmid(mmId);
        pickReqDTO.setSalesUOMId(String.valueOf(salesUOMId));
        pickReqDTO.setSoDetailsId(String.valueOf(soDetailsId));
        pickReqDTO.setOutbondId(String.valueOf(outbondId));
        pickReqDTO.setSoHeaderId(String.valueOf(soHeaderId));
        pickReqDTO.setUserId(userId);
        pickReqDTO.setMspValues("," + selectedBatch + ",");
        pickReqDTO.setPrintRequired(cbPrint.isChecked());
        pickReqDTO.setPrinterIP(printer);
        pickReqDTO.setRemarks("");
        pickReqDTO.setSOQty(Double.parseDouble(lblReqQty.getText().toString()));
        pickReqDTO.setPickedQty(Double.parseDouble(lblPkdQty.getText().toString()));
        pickReqDTO.setMCode(lblPart.getText().toString());
        pickReqDTO.setDescription(description);
        pickReqDTO.setOEMNumber(OEMNumber);
        pickReqDTO.setUOM(uom);
        pickReqDTO.setKitCode(lblKitCode.getText().toString());
        pickReqDTO.setProductionOrderHeaderId(productionOrderHeaderId);
        pickReqDTO.setLineNumber(lineNumber);
        pickReqDTO.setUserId(userId);
        pickReqDTO.setConversionfactor(conversationFactor);


        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

        Call<String> call = null;
        call = apiService.RePrint(pickReqDTO);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                ProgressDialogUtils.closeProgressDialog();

               /* if (response.body() != null) {

                    Gson gson = new Gson();
                    String json = gson.toJson(response.body());
                    Log.i("onResponse", response.body().toString());
                    try {
                        JSONObject jsonObject = new JSONObject(response.body());
                        String status = jsonObject.getString("status");
                        if (status.equalsIgnoreCase("true")) {

                            common.showUserDefinedAlertType("Reprint successful", getActivity(), getContext(), "Success");
                            etQty.setText("");

                        } else {
                            common.showUserDefinedAlertType("Reprint Failed", getActivity(), getContext(), "Error");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }*/

            }

            @Override
            public void onFailure(Call<String> call, Throwable throwable) {

                ProgressDialogUtils.closeProgressDialog();
                //common.showUserDefinedAlertType("Reprint successful", getActivity(), getContext(), "Success");
                btnPick.setEnabled(true);
            }
        });

    }


    @Override
    public void onPause() {
        super.onPause();
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
            }
            barcodeReader.release();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                // Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_batch_picking));
    }


    @Override
    public void onDestroyView() {

        // Honeywell onDestroyView
        if (barcodeReader != null) {
            // unregister barcode event listener honeywell
            barcodeReader.removeBarcodeListener((BarcodeReader.BarcodeListener) this);

            // unregister trigger state change listener
            barcodeReader.removeTriggerListener((BarcodeReader.TriggerListener) this);
        }

        // Cipher onDestroyView
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", false);
        getActivity().sendBroadcast(RTintent);
        getActivity().unregisterReceiver(this.myDataReceiver);
        super.onDestroyView();

    }

}


