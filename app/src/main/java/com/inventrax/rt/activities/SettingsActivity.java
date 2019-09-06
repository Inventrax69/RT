package com.inventrax.rt.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inventrax.rt.R;
import com.inventrax.rt.common.Common;
import com.inventrax.rt.common.constants.ErrorMessages;
import com.inventrax.rt.common.constants.ServiceURL;
import com.inventrax.rt.interfaces.ApiInterface;
import com.inventrax.rt.services.RestService;
import com.inventrax.rt.util.DialogUtils;
import com.inventrax.rt.util.ExceptionLoggerUtils;
import com.inventrax.rt.util.SharedPreferencesUtils;
import com.inventrax.rt.util.SoundUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Prasanna.ch on 06/06/2018.
 */

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private String classCode = "WMSCore_Android_Activity_002";

    private TextInputLayout inputLayoutServiceUrl;
    private EditText inputService;
    private Button btnSave, btnClose;
    private Spinner spinnerSelectPrinter;

    private String url = null, printer = "";
    List valuesList;

    private Common common = null;

    String printerIp = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    SoundUtils sound = null;
    private Gson gson;
    HashMap<String, String> hashMap;

    private SharedPreferencesUtils sharedPreferencesUtils;
    ServiceURL serviceUrl = new ServiceURL();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loadFormControls();
    }

    public void loadFormControls() {
        btnSave = (Button) findViewById(R.id.btnSave);
        btnClose = (Button) findViewById(R.id.btnClose);

        inputLayoutServiceUrl = (TextInputLayout) findViewById(R.id.txtInputLayoutServiceUrl);
        inputService = (EditText) findViewById(R.id.etServiceUrl);

        spinnerSelectPrinter = (Spinner) findViewById(R.id.spinnerSelectPrinter);
        spinnerSelectPrinter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                printer = spinnerSelectPrinter.getSelectedItem().toString();

                if (!printer.equalsIgnoreCase("Select Printer")) {

                    printer = hashMap.get(printer);

                    sharedPreferencesUtils.savePreference("printer", printer);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnSave.setOnClickListener(this);
        btnClose.setOnClickListener(this);

        hashMap = new HashMap<String, String>();
        valuesList = new ArrayList();

        sharedPreferencesUtils = new SharedPreferencesUtils("SettingsActivity", getApplicationContext());
        inputService.setText(sharedPreferencesUtils.loadPreference("url"));

        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();


        if(inputService.getText().toString().isEmpty()){

        }else {
            getPrinters();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:

                if (!inputService.getText().toString().isEmpty()) {
                    serviceUrl.setServiceUrl("");
                    SharedPreferences sp = this.getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
                    sharedPreferencesUtils.removePreferences("url");
                    sharedPreferencesUtils.savePreference("url", inputService.getText().toString());

                    DialogUtils.showAlertDialog(SettingsActivity.this, "Saved successfully");

                    if(printer.equals("")) {
                        serviceUrl.setServiceUrl(inputService.getText().toString());
                        getPrinters();
                    }


                } else {
                    DialogUtils.showAlertDialog(SettingsActivity.this, "Service Url  not be empty");
                }


                break;

            case R.id.btnClose:
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void getPrinters() {


        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

        Call<String> call = null;

        call = apiService.getSettings();

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                Gson gson = new Gson();

                String jsons =response.body(); //gson.toJson(response.body());

                try {

                    if(jsons!=null) {
                        // get JSONObject from JSON file
                        JSONObject obj = new JSONObject(jsons);
                        // fetch JSONObject named employee
                        JSONArray employee = new JSONArray(obj.getString("settings"));
                        // get employee name and salary

                        for (int i = 0; i < employee.length(); i++) {
                            JSONObject jsonobject = employee.getJSONObject(i);
                            String id = jsonobject.getString("DeviceIP");
                            String name = jsonobject.getString("ClientResourceName");

                            hashMap.put(name, id);

                        }

                        for (String list : hashMap.keySet()) {
                            // do something with tab

                            valuesList.add(list);
                        }


                        ArrayAdapter arrayAdapterPrinters = new ArrayAdapter(SettingsActivity.this, R.layout.support_simple_spinner_dropdown_item, valuesList);
                        spinnerSelectPrinter.setAdapter(arrayAdapterPrinters);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call<String> call, Throwable throwable) {
                common.showUserDefinedAlertType(errorMessages.EMC_0008, SettingsActivity.this, getApplicationContext(), "Warning");
            }
        });

    }


}