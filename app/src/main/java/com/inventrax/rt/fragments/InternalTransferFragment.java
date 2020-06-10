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
import android.widget.Toast;

import com.cipherlab.barcode.GeneralString;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
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
import com.inventrax.rt.pojos.DetailsDTO;
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
 * Created by padmaja.b on 20/12/2018.
 */

public class InternalTransferFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {


    private static final String classCode = "API_FRAG_LIVESTOCK";
    private View rootView;

    private TextInputLayout txtInputLayoutPartNo, txtInputLayoutLocation, txtInputLayoutQty;
    private EditText etPartNo, etLocation, etQty;
    private Button btnClear, btnDetails, btnClose, btnTransfer;

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
    private String part = "", location = "", batch = "", gmdId = "", categoryId = "", qty = "";
    TextView tv;
    TableLayout tl;
    List<DetailsDTO> lstDetails;
    TableRow tr;

    int selectedItem = -1;

    // private boolean isPart = false, isLocation= false, isBatch = false;

    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public InternalTransferFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_internaltransfer, container, false);
        loadFormControls();

        return rootView;
    }

    private void loadFormControls() {

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("UserId", "");
        accountId = sp.getString("AccountId", "");

        tl = (TableLayout) rootView.findViewById(R.id.table);

        txtInputLayoutPartNo = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPartNo);
        txtInputLayoutLocation = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutLocation);
        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);


        etPartNo = (EditText) rootView.findViewById(R.id.etPartNo);
        etLocation = (EditText) rootView.findViewById(R.id.etLocation);
        etQty = (EditText) rootView.findViewById(R.id.etQty);


        btnClear = (Button) rootView.findViewById(R.id.btnClear);
        btnDetails = (Button) rootView.findViewById(R.id.btnDetails);
        btnClose = (Button) rootView.findViewById(R.id.btnClose);
        btnTransfer = (Button) rootView.findViewById(R.id.btnTransfer);

        btnClear.setOnClickListener(this);
        btnDetails.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnTransfer.setOnClickListener(this);
        tl.setOnClickListener(this);

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
            case R.id.btnClose:
                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnClear:
                clearFields();

                break;
            case R.id.btnDetails:
                if (!etPartNo.getText().toString().isEmpty()) {
                    tl.removeAllViews();
                    getDetails();
                } else {
                    common.showUserDefinedAlertType("Please enter part number.", getActivity(), getContext(), "Error");
                }

                break;
            case R.id.table:
                Toast.makeText(getActivity(), "table", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btnTransfer:

                if (!etQty.getText().toString().isEmpty()) {

                    location = etLocation.getText().toString();

                    double availqty = Double.parseDouble(qty);
                    double qtydouble = Double.parseDouble(etQty.getText().toString());

                    if (location.startsWith("PI") && categoryId.equalsIgnoreCase("3")) {
                        common.showUserDefinedAlertType(errorMessages.EMC_0020, getActivity(), getContext(), "Error");
                        return;
                    }

                    if (etQty.getText().toString().equalsIgnoreCase("0")) {
                        common.showUserDefinedAlertType(errorMessages.EMC_0017, getActivity(), getContext(), "Error");
                        return;
                    }

                    if (qtydouble > availqty) {
                        common.showUserDefinedAlertType(errorMessages.EMC_0018, getActivity(), getContext(), "Error");
                        return;
                    }

                    transfer();
                }

                break;

            default:
                if (!(v.getId() == -1)) {
                    Log.v("ABCDE", new Gson().toJson(lstDetails.get(v.getId())));

                    String selectedRow = new Gson().toJson(lstDetails.get(v.getId()));

                    //v.setBackgroundColor(Color.RED);


                    DetailsDTO detailsDTO;

                    TypeToken<DetailsDTO> token = new TypeToken<DetailsDTO>() {
                    };
                    detailsDTO = gson.fromJson(selectedRow, token.getType());

                    gmdId = detailsDTO.getGoodsMovementDetailsIDs();
                    categoryId = detailsDTO.getProductCategoryID();
                    qty = detailsDTO.getQuantity();

                   /* v.setSelected(selectedItem == v.getId());

                    selectedItem = v.getId();*/

                    // Here I am just highlighting the background
                    v.setBackgroundColor(selectedItem == v.getId() ? Color.TRANSPARENT : Color.RED);

                    /*if(selectedItem!=v.getId()){
                        v.setBackgroundColor(selectedItem == v.getId() ? Color.TRANSPARENT : Color.RED);
                    }*/


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


    public void getDetails() {

        part = etPartNo.getText().toString();
        location = "";

        ProgressDialogUtils.showProgressDialog("Please wait..");

        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
        Call<String> call = null;
        call = apiService.Getintertransferdata(part, location);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                Gson gson = new Gson();
                List<DetailsDTO> lst = new ArrayList<DetailsDTO>();
                ProgressDialogUtils.closeProgressDialog();

                String jsons = response.body(); //gson.toJson(response.body());


                TypeToken<List<DetailsDTO>> token = new TypeToken<List<DetailsDTO>>() {
                };
                lstDetails = gson.fromJson(jsons, token.getType());

                addHeaders();
                addData(lstDetails);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

                ProgressDialogUtils.closeProgressDialog();
                // Log error here since request failed
                common.showUserDefinedAlertType(errorMessages.EMC_0008, getActivity(), getContext(), "Warning");
                clearFields();
            }
        });

    }


    public void transfer() {

        part = etPartNo.getText().toString();
        String qtyToTransfer = etQty.getText().toString();

        ProgressDialogUtils.showProgressDialog("Please wait..");

        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
        Call<String> call = null;
        call = apiService.Internaltransfer(gmdId, location, qtyToTransfer, userId, part);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                Gson gson = new Gson();
                List<DetailsDTO> lst = new ArrayList<DetailsDTO>();
                ProgressDialogUtils.closeProgressDialog();

                String jsons = response.body(); //gson.toJson(response.body());

                Log.d("res", response.toString());

                if (response.body().equalsIgnoreCase("1")) {

                    common.showUserDefinedAlertType(errorMessages.EMC_0016, getActivity(), getContext(), "Success");

                    tl.removeAllViews();

                    etQty.setText("");

                    getDetails();

                } else {
                    common.showUserDefinedAlertType(response.body(), getActivity(), getContext(), "Warning");
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

                ProgressDialogUtils.closeProgressDialog();
                // Log error here since request failed
                common.showUserDefinedAlertType(errorMessages.EMC_0008, getActivity(), getContext(), "Warning");
                clearFields();
            }
        });

    }


    public void clearFields() {

        etPartNo.setText("");

        part = "";
        batch = "";
        location = "";

        gmdId = "";
        qty = "";
        etQty.setText("");
        etLocation.setText("");
        tl.removeAllViews();

    }


    private TextView getTextView(int id, String title, int color, int typeface, int bgColor) {
        tv = new TextView(getContext());
        tv.setText(title);
        tv.setTextColor(color);
        tv.setPadding(10, 10, 10, 10);
        tv.setTypeface(Typeface.DEFAULT, typeface);
        tv.setBackgroundColor(bgColor);
        tv.setLayoutParams(getLayoutParams());

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

        tr = new TableRow(getContext());

        tr.setClickable(true);
        tr.setLayoutParams(getLayoutParams());
        tr.addView(getTextView(0, "Location", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView(0, "Quantity", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView(0, "IsDamaged", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView(0, "SerialNo", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView(0, "BatchNo", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView(0, "OEMBatchNo", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView(0, "HasDiscrepancy", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView(0, "IsNonConfirmity", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.setId(-1);
        tr.setOnClickListener(this);
        tl.addView(tr, getTblLayoutParams());
    }

    /**
     * This function add the data to the table
     **/
    public void addData(final List<DetailsDTO> deatils) {

        int id = 0;
        for (final DetailsDTO data : deatils) {
            tr = new TableRow(getContext());
            tr.setId(id);

            tr.setClickable(true);
            tr.setLayoutParams(getLayoutParams());
            tr.addView(getTextView(id, data.getLocation(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(id, data.getQuantity(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(id, data.getIsDamaged(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(id, data.getSerialNo(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(id, data.getBatchNo(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(id, data.getOEMBatchNo(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(id, data.getHasDiscrepancy(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.addView(getTextView(id, data.getIsNonConfirmity(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.table)));
            tr.setOnClickListener(this);
            tl.addView(tr, getTblLayoutParams());
            id++;
        }


    }

    private View.OnClickListener tablerowOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            int clicked_id = v.getId();

            Toast.makeText(getActivity(), "" + clicked_id, Toast.LENGTH_SHORT).show();
        }
    };

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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_transfer));
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