package project.bsts.semut;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;
import project.bsts.semut.connections.rest.IConnectionResponseHandler;
import project.bsts.semut.connections.rest.RequestRest;
import project.bsts.semut.pojo.RequestStatus;
import project.bsts.semut.setup.Constants;
import project.bsts.semut.ui.CommonAlerts;
import project.bsts.semut.utilities.CustomDrawable;
import project.bsts.semut.utilities.FieldValidator;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

public class SignupActivity extends AppCompatActivity implements IConnectionResponseHandler{

    @BindView(R.id.edit_username)
    EditText mEditUsername;
    @BindView(R.id.edit_email)
    EditText mEditEmail;
    @BindView(R.id.edit_password)
    EditText mEditPassword;
    @BindView(R.id.edit_password_repeat)
    EditText mEditRepeatPassword;
    @BindView(R.id.edit_full_name)
    EditText mEditFullName;
    @BindView(R.id.button_signup)
    Button mButtonSignup;
    @BindView(R.id.input_email)
    TextInputLayout mInputEmail;

    private int loginType = 0;
    private int genderType = 0;

    private Context context;
    private final String TAG = this.getClass().getSimpleName();
    private ProgressDialog dialog;
    private RequestRest requestRest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        context = this;

        dialog = new ProgressDialog(context);
        dialog.setMessage("Memuat...");

        initIcon();
        mButtonSignup.setOnClickListener(v -> doLogin());
    }


    private void doLogin(){


        boolean valid = FieldValidator.userNameValidator(mEditUsername.getText().toString());
        if(!valid) Snackbar.make(mEditEmail, "Username tidak valid. Harus huruf kecil dan minimal terdiri dari 6 karakter", Snackbar.LENGTH_LONG).show();
        else {
            if (mEditPassword.getText().toString().equals("") ||
                    mEditRepeatPassword.getText().toString().equals("") ||
                    mEditUsername.getText().toString().equals("") ||
                    mEditFullName.getText().equals("")) {
                Snackbar.make(mButtonSignup, "Kolom tidak kosong", Snackbar.LENGTH_LONG).show();
            } else {
                if (!mEditPassword.getText().toString().equals(mEditRepeatPassword.getText().toString())) {
                    Snackbar.make(mButtonSignup, "password tidak sama, periksa kembali", Snackbar.LENGTH_LONG).show();
                } else {
                    dialog.show();
                    requestRest = new RequestRest(context, this);
                    requestRest.register(mEditEmail.getText().toString(), mEditPassword.getText().toString(),
                            mEditFullName.getText().toString(), mEditUsername.getText().toString());
                }
            }
        }
    }


    private void initIcon() {
        mEditUsername.setCompoundDrawables(CustomDrawable.create(context,
                GoogleMaterial.Icon.gmd_person, 24, R.color.primary_dark), null, null, null);
        mEditEmail.setCompoundDrawables(CustomDrawable.create(context,
                GoogleMaterial.Icon.gmd_contacts, 24, R.color.primary_dark), null, null, null);
        mEditPassword.setCompoundDrawables(CustomDrawable.create(context,
                GoogleMaterial.Icon.gmd_https, 24, R.color.primary_dark), null, null, null);
        mEditRepeatPassword.setCompoundDrawables(CustomDrawable.create(context,
                GoogleMaterial.Icon.gmd_https, 24, R.color.primary_dark), null, null, null);
        mEditFullName.setCompoundDrawables(CustomDrawable.create(context,
                GoogleMaterial.Icon.gmd_account_box, 24, R.color.primary_dark), null, null, null);
    }





    @Override
    public void onSuccessRequest(String pResult, String type) {
        dialog.dismiss();
        switch (type){
            case Constants.REST_USER_REGISTER:
                RequestStatus requestStatus = new Gson().fromJson(pResult, RequestStatus.class);
                if(requestStatus.getSuccess()) {
                    Toast.makeText(context, "Berhasil membuat akun, silahkan login", Toast.LENGTH_LONG).show();
                    finish();
                }else CommonAlerts.commonError(context, requestStatus.getMessage());
                break;
            case Constants.REST_ERROR:
                CommonAlerts.commonError(context, Constants.MESSAGE_HTTP_ERROR);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }


        return super.onOptionsItemSelected(item);
    }
}
