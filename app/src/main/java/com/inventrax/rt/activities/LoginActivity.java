package com.inventrax.rt.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inventrax.rt.R;
import com.inventrax.rt.application.AbstractApplication;
import com.inventrax.rt.common.Common;
import com.inventrax.rt.common.constants.ErrorMessages;
import com.inventrax.rt.common.constants.ServiceURL;
import com.inventrax.rt.interfaces.ApiInterface;
import com.inventrax.rt.login.LoginPresenter;
import com.inventrax.rt.login.LoginPresenterImpl;
import com.inventrax.rt.login.LoginView;
import com.inventrax.rt.pojos.LoginDTO;
import com.inventrax.rt.services.RestService;
import com.inventrax.rt.util.AndroidUtils;
import com.inventrax.rt.util.DialogUtils;
import com.inventrax.rt.util.ExceptionLoggerUtils;
import com.inventrax.rt.util.NetworkUtils;
import com.inventrax.rt.util.ProgressDialogUtils;
import com.inventrax.rt.util.SharedPreferencesUtils;
import com.inventrax.rt.util.SoundUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Padmaja.B on 20/12/2018.
 */
public class LoginActivity extends AppCompatActivity implements LoginView {

    private static final String classCode = "API_ACT_LOGIN";

    private EditText inputUserId, inputPassword;
    private TextInputLayout inputLayoutUserId, inputLayoutPassword;
    private Button btnLogin, btnClear;
    private CheckBox chkRememberPassword;
    private TextView txtVersion, txtReleaseDate;

    private ProgressDialogUtils progressDialogUtils;
    private LoginPresenter loginPresenter;
    private SharedPreferencesUtils sharedPreferencesUtils;
    private Gson gson;

    private Common common;
    private SoundUtils soundUtils;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    RestService restService;
    private String scanType = null;
    private String userId = "", password = "", ipAddress = "";


    public static final int MULTIPLE_PERMISSIONS = 10;

    // if the android mobile version is greater than 6.0 we are giving the following permissions
    String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.WAKE_LOCK, Manifest.permission.VIBRATE, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS, Manifest.permission.READ_PHONE_STATE};

    ImageView settings;
    String serviceUrlString = null;
    ServiceURL serviceURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        requestforpermissions(permissions);
        //versioncontrol();
        loadFormControls();

        loginPresenter = new LoginPresenterImpl(this);
    }

    //Loading all the form controls
    private void loadFormControls() {

        try {

            sharedPreferencesUtils = new SharedPreferencesUtils("LoginActivity", getApplicationContext());

            inputUserId = (EditText) findViewById(R.id.etUsername);
            inputPassword = (EditText) findViewById(R.id.etPass);
            chkRememberPassword = (CheckBox) findViewById(R.id.cbRememberMe);
            btnLogin = (Button) findViewById(R.id.btnLogin);
            /*btnClear = (Button) findViewById(R.id.btnClear);
            btnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LoginActivity.this.finish();
                    System.exit(0);
                }
            });*/
            txtReleaseDate = (TextView) findViewById(R.id.txtDate);
            txtVersion = (TextView) findViewById(R.id.txtVersionName);
            txtVersion.setText("Version:" + " " + AndroidUtils.getVersionName().toString());
            txtReleaseDate.setText("Release Date:" + " " + "17-09-2019");


            SharedPreferences sp = this.getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
            serviceUrlString = sp.getString("url", "");

            common = new Common();
            errorMessages = new ErrorMessages();
            serviceURL = new ServiceURL();

            exceptionLoggerUtils = new ExceptionLoggerUtils();
            restService = new RestService();
            soundUtils = new SoundUtils();
            inputUserId.addTextChangedListener(new LoginViewTextWatcher(inputUserId));
            inputPassword.addTextChangedListener(new LoginViewTextWatcher(inputPassword));
            gson = new GsonBuilder().create();


            ServiceURL.setServiceUrl(serviceUrlString);


            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (serviceUrlString != null && serviceUrlString != "") {

                        if (!inputUserId.getText().toString().isEmpty() && !inputPassword.getText().toString().isEmpty()) {
                            //materialDialogUtil.showErrorDialog(LoginActivity.this,"Failed Failed Failed Failed Failed Failed Failed Failed Failed Failed Failed Failed");
                            if (submitForm()) {
                                // Checking Internet Connection


                                validateUserSession();

                                /*Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(i);*/


                                //If User Clicks on remember me username,Password is stored in Shared preferences
                                if (chkRememberPassword.isChecked()) {
                                    sharedPreferencesUtils.savePreference("userName", inputUserId.getText().toString().trim());
                                    sharedPreferencesUtils.savePreference("password", inputPassword.getText().toString().trim());
                                    sharedPreferencesUtils.savePreference("isRememberPasswordChecked", true);
                                }
                            } else {

                                //Toast.makeText(getApplicationContext(),"Enter credentials",Toast.LENGTH_LONG).show();
                            }
                        } else {
                            common.showUserDefinedAlertType("Login ID and Password fields cannot be blank", LoginActivity.this, getApplicationContext(), "Warning");

                        }
                    } else {

                        common.showUserDefinedAlertType("Please set the service URL in settings before you login", LoginActivity.this, getApplicationContext(), "Warning");
                        //return;
                    }
                }
            });
        } catch (Exception ex) {
            Log.d("", "");
        }

        settings = (ImageView) findViewById(R.id.ivSettings);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
                startActivity(intent);

            }
        });

        progressDialogUtils = new ProgressDialogUtils(this);


        if (sharedPreferencesUtils.loadPreferenceAsBoolean("isRememberPasswordChecked", false)) {
            inputUserId.setText(sharedPreferencesUtils.loadPreference("userName", ""));
            inputPassword.setText(sharedPreferencesUtils.loadPreference("password", ""));
            chkRememberPassword.setChecked(true);
        } else {
            inputUserId.setText(sharedPreferencesUtils.loadPreference("userName", ""));
            inputPassword.setText(sharedPreferencesUtils.loadPreference("password", ""));
            sharedPreferencesUtils.loadPreferenceAsBoolean("isRememberPasswordChecked", true);
        }
        AbstractApplication.CONTEXT = getApplicationContext();

    }

    @Override
    protected void onDestroy() {
        loginPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void showProgress() {
        progressDialogUtils.showProgressDialog("Please Wait ...");
    }

    @Override
    public void hideProgress() {
        progressDialogUtils.closeProgressDialog();
    }

    @Override
    public void setUsernameError() {
        //inputLayoutUserId.setError(getString(R.string.));
        //requestFocus(inputUserId);
    }

    @Override
    public void setPasswordError() {
        //  inputLayoutPassword.setError(getString(R.string.err_msg_password));
        // requestFocus(inputPassword);
    }

    @Override
    public void showLoginError(String message) {
        DialogUtils.showAlertDialog(this, message);
        return;
    }

    @Override
    public void navigateToHome() {

        // sharedPreferencesUtils.savePreference("login_status", true);

        showProgress();
        hideProgress();

        this.startActivity(new Intent(this, MainActivity.class));
        //  this.finish();
    }

    /**
     * Validating form
     */
    private boolean submitForm() {

        String userId = inputUserId.getText().toString().trim();

        if (userId.isEmpty() || !isValidUserId(userId)) {
            inputLayoutUserId.setError(getString(R.string.userHint));
            inputLayoutUserId.setErrorEnabled(true);
            return false;
        }
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.passHint));
            inputLayoutPassword.setErrorEnabled(true);
            return false;
        }

            /*if (NetworkUtils.isInternetAvailable()){

                loginPresenter.validateCredentials(inputUserId.getText().toString(), inputPassword.getText().toString(),chkRememberPassword.isChecked());

            }else {
                DialogUtils.showAlertDialog(this,"Please enable internet");
                return ;

            }*/

        return true;
    }


    //Validating the User credentials and Calling the API method
    public void validateUserSession() {
        if (NetworkUtils.isInternetAvailable(this)) {
        } else {
            DialogUtils.showAlertDialog(this, "Please enable internet");
            // soundUtils.alertSuccess(LoginActivity.this,getBaseContext());
            return;
        }

        ProgressDialogUtils.showProgressDialog("Connecting..");

        userId = inputUserId.getText().toString();
        password = inputPassword.getText().toString();
        ipAddress = AndroidUtils.getIPAddress(true);
        ipAddress = ipAddress.replace(".", "_");

        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
        Call<String> call = null;
        call = apiService.get(userId, password, ipAddress);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                Gson gson = new Gson();
                LoginDTO dto = gson.fromJson(response.body(), LoginDTO.class);

                ProgressDialogUtils.closeProgressDialog();

                if (dto.getUserID() != 0) {


                    if (dto.getUserID() != 0) {
                        sharedPreferencesUtils.savePreference("UserId", String.valueOf(dto.getUserID()));

                    }
                    if (dto.getFirstName() != null) {

                        sharedPreferencesUtils.savePreference("User", dto.getFirstName());

                    }

                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                } else {
                    ProgressDialogUtils.closeProgressDialog();
                    common.showUserDefinedAlertType(errorMessages.EMC_0005, LoginActivity.this, getApplicationContext(), "Warning");
                    return;
                }


            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0008, LoginActivity.this, getApplicationContext(), "Warning");
            }
        });

    }


    private static boolean isValidUserId(String userId) {
        return !TextUtils.isEmpty(userId);
    }

    private boolean validateUserId() {
        String userId = inputUserId.getText().toString().trim();
        if (userId.isEmpty() || !isValidUserId(userId)) {
            // inputLayoutUserId.setError(getString(R.string.err_msg_user_id));
            inputLayoutUserId.setErrorEnabled(false);
            return false;
        } else {
            return true;
        }
    }

    private boolean validatePassword() {
        if (inputPassword.getText().toString().trim().isEmpty()) {
            // inputLayoutPassword.setError(getString(R.string.err_msg_password));
            inputLayoutPassword.setErrorEnabled(false);
            return false;
        } else {
            return true;
        }
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class LoginViewTextWatcher implements TextWatcher {

        private View view;

        private LoginViewTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.etUsername:
                    //validateUserId();
                    break;
                case R.id.etPass:
                    //validatePassword();
                    break;
            }
        }
    }

    private void versioncontrol() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Do something for lollipop and above versions
        } else {
            // Toast.makeText(getApplicationContext(), "Android version is not supported.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void requestforpermissions(String[] permissions) {
        if (checkPermissions()) {
        }
        //  permissions  granted.
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(LoginActivity.this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadFormControls();
                    // permissions granted.
                } else {
                    String permission = "";
                    for (String per : permissions) {
                        permission += "\n" + per;

                    }
                    // permissions list of don't granted permission
                }
                return;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


}