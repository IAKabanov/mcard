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

public class MDiagnosisActivity extends AppCompatActivity {

    private DBMethods fDBMethods;
    int gettedID;
    EditText editDiagnonisName;
    RelativeLayout rlDiagnonisContainer;
    Toolbar mToolBar;

    String mDiagnonisName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.m_activity_diagnosis);

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        mToolBar = (Toolbar) findViewById(R.id.mDiagnonisToolbar);
        setSupportActionBar(mToolBar);

        rlDiagnonisContainer = (RelativeLayout) findViewById(R.id.rlDiagnonisContainer);

        editDiagnonisName = (EditText)findViewById(R.id.editDiagnonisName);

        gettedID = getIntent().getIntExtra("current_diagnosis_id", -1);
        if (gettedID != -1){
            mDiagnonisName = fDBMethods.getDiagnosesNameByID(gettedID);
            editDiagnonisName.setText(mDiagnonisName);
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

        switch (id) {
            case R.id.common_img_OK:
                diagnosisSave(); // сохраним с закрытием
                break;
            case R.id.common_img_Close:
                diagnosisClose();
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

    private void diagnosisClose() {

        Intent intent = new Intent();
        intent.putExtra("currentItemID", gettedID); // для правильного позиционирования в списке
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void diagnosisSave(){

        if (editDiagnonisName.getText().toString().isEmpty()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mToolBar.getWindowToken(), 0);
            Snackbar snackbar = Common.getCustomSnackbar(MDiagnosisActivity.this, rlDiagnonisContainer, getResources().getString(R.string.diagnosis_save_error));
            snackbar.show();
            return;
        }
        // записать в БД и вернуть "OK" в вызвавшую activity, а там CursorLoader сам обновит список
        if (gettedID == -1){
            // создание нового
            String strName = editDiagnonisName.getText().toString().trim();
            if (!strName.isEmpty())
                gettedID = (int) fDBMethods.insertDiagnoses(strName);
        }
        else {
            // для update вставим также и полученный id
            fDBMethods.updateDiagnoses(gettedID, editDiagnonisName.getText().toString().trim());
        }

        Intent intent = new Intent();
        intent.putExtra("currentItemID", gettedID);
        setResult(RESULT_OK, intent);
        finish();

    }



}
