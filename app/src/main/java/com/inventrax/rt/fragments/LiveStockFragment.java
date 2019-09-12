package com.inventrax.rt.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.inventrax.rt.common.Common;
import com.inventrax.rt.common.constants.ErrorMessages;
import com.inventrax.rt.interfaces.ApiInterface;
import com.inventrax.rt.pojos.LiveStock;
import com.inventrax.rt.services.RestService;
import com.inventrax.rt.util.ExceptionLoggerUtils;
import com.inventrax.rt.util.FragmentUtils;
import com.inventrax.rt.util.ProgressDialogUtils;
import com.inventrax.rt.util.ScanValidator;

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

public class LiveStockFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {


    private static final String classCode = "API_FRAG_LIVESTOCK";
    private View rootView;

    private TextInputLayout txtInputLayoutPartNo, txtInputLayoutLocation, txtInputLayoutBatch;
    private EditText etPartNo, etLocation, etBatch;
    private Button btnClear, btnSearch, btnClose;

    String scanner = null;
    private IntentFilter filter;

    String getScanner = null;
    private Gson gson;
    private ScanValidator scanValidator;

    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private Common common;

    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;

    private String userId = null, accountId = null;
    private String part = "", location = "", batch = "";

    TableLayout tl;

    // private boolean isPart = false, isLocation= false, isBatch = false;

    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public LiveStockFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_livestock, container, false);
        loadFormControls();

        return rootView;
    }

    private void loadFormControls() {

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("UserId", "");
        accountId = sp.getString("AccountId", "");

        txtInputLayoutPartNo = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPartNo);
        txtInputLayoutLocation = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutLocation);
        txtInputLayoutBatch = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutBatch);

        etPartNo = (EditText) rootView.findViewById(R.id.etPartNo);
        etLocation = (EditText) rootView.findViewById(R.id.etLocation);
        etBatch = (EditText) rootView.findViewById(R.id.etBatch);

        btnClear = (Button) rootView.findViewById(R.id.btnClear);
        btnSearch = (Button) rootView.findViewById(R.id.btnSearch);
        btnClose = (Button) rootView.findViewById(R.id.btnClose);

        btnClear.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnClose.setOnClickListener(this);

        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

        tl = (TableLayout) rootView.findViewById(R.id.table);
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
            case R.id.btnClose:
                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnClear:
                clearFields();
                break;
            case R.id.btnSearch:

                tl.removeAllViews();

                if(!etLocation.getText().toString().isEmpty() || !etBatch.getText().toString().isEmpty() || !etPartNo.getText().toString().isEmpty()){

                    // To get Live stock details
                    getLiveStock();

                }else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0014, getActivity(), getContext(), "Error");
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

            if (!scannedData.equals("")) {

                if (scannedData.contains(",")) {
                    String partNo = scannedData.split("[,]", 2)[0].trim();
                    String batchNo = scannedData.split("[,]", 2)[1].trim();

                    if (ScanValidator.IsItemScanned(partNo)) {
                        etPartNo.setText(partNo);
                        etBatch.setText(batchNo);
                    } else {
                        common.showUserDefinedAlertType("Please scan valid barcode", getActivity(), getContext(), "Error");
                    }

                } else if (ScanValidator.IsLocationScanned(scannedData)) {
                    etLocation.setText(scannedData.trim());
                } else {
                    common.showUserDefinedAlertType("Please scan valid barcode", getActivity(), getContext(), "Error");
                }

            } else {
                common.showUserDefinedAlertType("Please scan valid barcode", getActivity(), getContext(), "Error");
            }

        } else {
            common.showUserDefinedAlertType("Please scan valid barcode", getActivity(), getContext(), "Error");
        }
    }


    public void getLiveStock() {

        part = etPartNo.getText().toString();
        location = etLocation.getText().toString();
        batch = etBatch.getText().toString();

        ProgressDialogUtils.showProgressDialog("Please wait..");

        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
        Call<List<LiveStock>> call = null;
        call = apiService.getlivestock(part, location, batch);

        call.enqueue(new Callback<List<LiveStock>>() {
            @Override
            public void onResponse(Call<List<LiveStock>> call, Response<List<LiveStock>> response) {

                Gson gson = new Gson();
                String json = gson.toJson(response.body());
                List<LiveStock> lst = new ArrayList<LiveStock>();

                ProgressDialogUtils.closeProgressDialog();

                if (response.body().size() == 0) {

                    common.showUserDefinedAlertType(errorMessages.EMC_0011, getActivity(), getContext(), "Warning");
                    clearFields();

                } else {

                    for (LiveStock size : response.body()) {
                        Log.i("onResponse", size.toString());
                        lst.add(size);
                    }


                    addHeaders();
                    addData(lst);
                }

            }

            @Override
            public void onFailure(Call<List<LiveStock>> call, Throwable t) {

                ProgressDialogUtils.closeProgressDialog();
                // Log error here since request failed
                common.showUserDefinedAlertType(errorMessages.EMC_0008, getActivity(), getContext(), "Warning");
                clearFields();
            }
        });

    }


    public void clearFields() {

        etPartNo.setText("");
        etBatch.setText("");
        etLocation.setText("");

        tl.removeAllViews();

        part = "";
        batch = "";
        location = "";


    }


    private TextView getTextView(String title, int color, int typeface, int bgColor) {
        TextView tv = new TextView(getContext());
        tv.setText(title);
        tv.setTextColor(color);
        tv.setPadding(10, 10, 10, 10);
        tv.setTypeface(Typeface.DEFAULT, typeface);
        tv.setBackgroundColor(bgColor);
        tv.setLayoutParams(getLayoutParams());
        tv.setOnClickListener(this);
        return tv;
    }

    @NonNull
    private TableRow.LayoutParams getLayoutParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(2, 0, 0, 2);
        return params;
    }

    @NonNull
    private TableLayout.LayoutParams getTblLayoutParams() {
        return new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
    }

    /**
     * This function add the headers to the table
     **/
    public void addHeaders() {

        TableRow tr = new TableRow(getContext());

        tr.setLayoutParams(getLayoutParams());
        if(etLocation.getText().toString().isEmpty()) {
            tr.addView(getTextView("Location", Color.WHITE, Typeface.BOLD, Color.BLUE));
        }
        tr.addView(getTextView("OEMPartNo", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("AvailableQty", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("IsDamaged", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("HasDiscrepancy", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("IsNonConfirmity", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("AsIs", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("IsPositiveRecall", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("MfgDate", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("ExpDate", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("SerialNo", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("Batch", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("OEMBatchNo", Color.WHITE, Typeface.BOLD, Color.BLUE));


        tl.addView(tr, getTblLayoutParams());
    }

    /**
     * This function add the data to the table
     **/
    public void addData(List<LiveStock> liveStock) {

        TableLayout tl = (TableLayout) rootView.findViewById(R.id.table);
        for (LiveStock data : liveStock) {
            TableRow tr = new TableRow(getContext());
            tr.setLayoutParams(getLayoutParams());
            if(etLocation.getText().toString().isEmpty()) {
                tr.addView(getTextView(data.getLocation(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            }
            tr.addView(getTextView(data.getOEMPartNo(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(data.getAvailableQty().toString(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(data.getIsDamaged(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(data.getHasDiscrepancy(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(data.getIsNonConfirmity(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(data.getAsIs(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(data.getIsPositiveRecall(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(data.getMfgDate(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(data.getExpDate(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(data.getSerialNo(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(data.getBatchNo(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(data.getOEMBatchNo(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));


            tl.addView(tr, getTblLayoutParams());
        }


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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_live_stock));
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