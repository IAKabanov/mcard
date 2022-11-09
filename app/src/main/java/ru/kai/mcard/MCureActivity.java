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


public class MCureActivity extends AppCompatActivity {

    private DBMethods fDBMethods;
    int gettedID;
    RelativeLayout rlCureContainer;
    Toolbar mToolBar;
    EditText editCureName;
    EditText editCureDescr;

    String mCureName;
    String mCureDescr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.m_activity_cure);

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        mToolBar = (Toolbar) findViewById(R.id.mCureToolbar);
        setSupportActionBar(mToolBar);

        rlCureContainer = (RelativeLayout) findViewById(R.id.rlCureContainer);

        editCureName = (EditText)findViewById(R.id.editCureName);
        editCureDescr = (EditText)findViewById(R.id.editCuresFeature);

        gettedID = getIntent().getIntExtra("choosen_cures_id", -1);
        if (gettedID != -1){
            mCureName = fDBMethods.getCuresNameByID(gettedID);
            editCureName.setText(mCureName);
            mCureDescr = fDBMethods.getCuresDescrByID(gettedID);
            editCureDescr.setText(mCureDescr);
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
                cureSave(); // сохраним с закрытием
                break;
            case R.id.common_img_Close:
                cureClose();
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

    private void cureClose() {

        Intent intent = new Intent();
        intent.putExtra("currentItemID", gettedID); // для правильного позиционирования в списке
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void cureSave(){

        if (editCureName.getText().toString().isEmpty()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mToolBar.getWindowToken(), 0);
            Snackbar snackbar = Common.getCustomSnackbar(MCureActivity.this, rlCureContainer, getResources().getString(R.string.cures_save_error));
            snackbar.show();
            return;
        }
        // записать в БД и вернуть "OK" в вызвавшую activity, а там CursorLoader сам обновит список
        if (gettedID == -1){
            // создание нового
            String strName = editCureName.getText().toString().trim();
            if (!strName.isEmpty())
                gettedID = fDBMethods.insertCure(strName, editCureDescr.getText().toString().trim());
        }
        else {
            // для update вставим также и полученный id
            fDBMethods.updateCure(gettedID, editCureName.getText().toString().trim(), editCureDescr.getText().toString().trim());
        }

        Intent intent = new Intent();
        intent.putExtra("currentItemID", gettedID);
        setResult(RESULT_OK, intent);
        finish();

    }


}
