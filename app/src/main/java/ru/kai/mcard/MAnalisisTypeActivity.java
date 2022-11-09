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

// Вид исследований
public class MAnalisisTypeActivity extends AppCompatActivity {

    private DBMethods fDBMethods;
    EditText editAnalisisTypeName;
    int gettedID;
    RelativeLayout rlmAnalisisTypeContainer;
    Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));
/*
        SharedPreferences mSettings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        //int currentTheme = R.style.Theme_StandartGreen;
        if (mSettings.contains(Constants.APP_PREFERENCES_CURRENT_THEME)) {
            // Получаем число из настроек
            int currentTheme = mSettings.getInt(Constants.APP_PREFERENCES_CURRENT_THEME, R.style.Theme_StandartGreen);
            setTheme(currentTheme);
        }
*/

        setContentView(R.layout.m_activity_analisis_type);

        rlmAnalisisTypeContainer = (RelativeLayout) findViewById(R.id.rlmAnalisisTypeContainer);

        mToolBar = (Toolbar) findViewById(R.id.toolbar_mAnalisisType);
        setSupportActionBar(mToolBar);

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        editAnalisisTypeName = (EditText)findViewById(R.id.editMAnalisisTypeName);

        gettedID = getIntent().getIntExtra("given_analises_types_id", 0);
        //gettedAdapterPosition = getIntent().getIntExtra("getted_adapter_position", -1);

        // установка полученного для изменения значения:
        if (gettedID != 0){
            editAnalisisTypeName.setText(fDBMethods.getAnalisesTypesNameByID(gettedID));
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
        if (item == null){
            try {
                throw new NullPointerException("No menu item");
            } catch (NullPointerException e) {
                System.out.println(e.toString());
                e.printStackTrace();
                return false;
            }
        }

        int id = item.getItemId();

        switch (id) {
            case R.id.common_img_OK:
                analisisTypeActivitySave(); // сохраним с закрытием
                break;
            case R.id.common_img_Close:
                analisisTypeActivityClose();
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

    public void analisisTypeActivityClose() {

        Intent intent = new Intent();
        intent.putExtra("currentItemID", gettedID);
        //intent.putExtra("currentAdapterPosition", gettedAdapterPosition);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void analisisTypeActivitySave(){

        if (editAnalisisTypeName.getText().toString().isEmpty()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mToolBar.getWindowToken(), 0);
            Snackbar snackbar = Common.getCustomSnackbar(MAnalisisTypeActivity.this, rlmAnalisisTypeContainer, getResources().getString(R.string.analisis_type_error));
            snackbar.show();
            return;
        }
        // записать в БД и вернуть "OK" в вызвавшую activity, а там CursorLoader сам обновит список
        if (gettedID == 0){
            // создание нового
            String strName = editAnalisisTypeName.getText().toString().trim();
            if (!strName.isEmpty())
                gettedID = fDBMethods.insertAnalisesType(strName);
        }
        else {
            // для update вставим также и полученный id
            fDBMethods.updateAnalisesType(gettedID, editAnalisisTypeName.getText().toString());
        }

        Intent intent = new Intent();
        intent.putExtra("currentItemID", gettedID);
        //intent.putExtra("currentAdapterPosition", gettedAdapterPosition);
        setResult(RESULT_OK, intent);
        finish();

    }




}
