package ru.kai.mcard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import ru.kai.mcard.utility.Common;

public class MClinicActivity extends AppCompatActivity {

    private DBMethods fDBMethods;
    int gettedID;
    EditText editClinicsName;
    EditText editClinicsPhones;
    EditText editClinicsAdress;
    RelativeLayout rlClinicsContainer;
    Toolbar mToolBar;

    String mClinicsName;
    String mClinicsPhones;
    String mClinicsDescr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.m_activity_clinic);

        mToolBar = (Toolbar) findViewById(R.id.mClinicToolbar);
        setSupportActionBar(mToolBar);

        rlClinicsContainer = (RelativeLayout) findViewById(R.id.rlClinicsContainer);

        editClinicsName = (EditText)findViewById(R.id.editClinicsName);
        editClinicsPhones = (EditText)findViewById(R.id.editClinicsPhones);
        editClinicsAdress = (EditText)findViewById(R.id.editClinicsAdress);

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        gettedID = getIntent().getIntExtra("choosen_clinic_id", -1);
        if (gettedID != -1){
            Bundle bundle = fDBMethods.getClinicsFildsByClinicsID(gettedID);
            mClinicsName = bundle.getString("mClinicsName", "");
            mClinicsPhones = bundle.getString("mClinicsPhones", "");
            mClinicsDescr = bundle.getString("mClinicsDescr", "");
            editClinicsName.setText(mClinicsName);
            editClinicsPhones.setText(mClinicsPhones);
            editClinicsAdress.setText(mClinicsDescr);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_common, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

/*
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
*/

        switch (id) {
            case R.id.common_img_OK:
                clinicsSave(); // сохраним с закрытием
                break;
            case R.id.common_img_Close:
                clinicsClose();
                break;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fDBMethods != null) {
            fDBMethods.close();
        }
    }

    private void clinicsClose() {

        Intent intent = new Intent();
        intent.putExtra("currentItemID", gettedID); // для правильного позиционирования в списке
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void clinicsSave(){

        if (editClinicsName.getText().toString().isEmpty()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mToolBar.getWindowToken(), 0);
            Snackbar snackbar = Common.getCustomSnackbar(MClinicActivity.this, rlClinicsContainer, getResources().getString(R.string.clinik_save_error));
            snackbar.show();
            return;
        }
        // записать в БД и вернуть "OK" в вызвавшую activity, а там CursorLoader сам обновит список
        if (gettedID == -1){
            // создание нового
            String strName = editClinicsName.getText().toString().trim();
            String strPhones = editClinicsPhones.getText().toString();
            String strAdress = editClinicsAdress.getText().toString().trim();
            if (!strName.isEmpty())
                gettedID = (int) fDBMethods.insertClinic(strName, strAdress);
        }
        else {
            // для update вставим также и полученный id
            fDBMethods.updateClinic(gettedID, editClinicsName.getText().toString().trim(), editClinicsAdress.getText().toString().trim());
        }

        Intent intent = new Intent();
        intent.putExtra("currentItemID", gettedID);
        setResult(RESULT_OK, intent);
        finish();

    }





}
