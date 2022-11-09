package ru.kai.mcard;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.Locale;

import ru.kai.mcard.utility.Common;

public class MDoctorActivity extends AppCompatActivity {

    private DBMethods fDBMethods;
    int gettedID;
    EditText editDoctorName;
    AutoCompleteTextView editDoctorsSpecialisation;
    RelativeLayout rlDoctorContainer;
    Toolbar mToolBar;

    String mDoctorsName;
    String mDoctorsSpecialisationsName;
    int mDoctorsSpecialisationsID;

    // для watcherSpecialization
    SimpleCursorAdapter editDoctorSpecializationsAdapter;
    Cursor cursorSpecializations;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.m_activity_doctor);

        mToolBar = (Toolbar) findViewById(R.id.mDoctorToolbar);
        setSupportActionBar(mToolBar);

        rlDoctorContainer = (RelativeLayout) findViewById(R.id.rlDoctorContainer);

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        editDoctorName = (EditText)findViewById(R.id.editDoctorName);
        editDoctorsSpecialisation = (AutoCompleteTextView)findViewById(R.id.editDoctorsSpecialisation);

        gettedID = getIntent().getIntExtra("choosen_doctor_id", -1);
        if (gettedID != -1){
            mDoctorsName = fDBMethods.getDoctorsNameByID(gettedID);
            mDoctorsSpecialisationsID = fDBMethods.getDoctorsSpecializationIDByDoctorsID(gettedID);
            mDoctorsSpecialisationsName = fDBMethods.getSpecializationsNameByID(mDoctorsSpecialisationsID);

            editDoctorName.setText(mDoctorsName);
            editDoctorsSpecialisation.setText(mDoctorsSpecialisationsName);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // предзагрузка списка специализаций для организации поиска
        cursorSpecializations = fDBMethods.getFilteredSpecialisations("");  // полная таблица
        String[]fromSpecializations = {DBMethods.TableSpecializations._ID, DBMethods.TableSpecializations.COLUMN_SPECIALIZATIONS_NAME};
        int[]toSpecializations = {R.id.lrdd_adapter_id, R.id.lrdd_adapter_name};
        editDoctorSpecializationsAdapter = new SimpleCursorAdapter(MDoctorActivity.this, R.layout.list_row_dropdown, cursorSpecializations, fromSpecializations, toSpecializations, 0); // новый адаптер
        editDoctorSpecializationsAdapter.setStringConversionColumn(cursorSpecializations.getColumnIndexOrThrow("_id"));  // чтобы привыборе из выпадающего списка подставлялось значение, а не козябры
        editDoctorSpecializationsAdapter.setStringConversionColumn(cursorSpecializations.getColumnIndexOrThrow("name"));  // чтобы привыборе из выпадающего списка подставлялось значение, а не козябры
        editDoctorsSpecialisation.setAdapter(editDoctorSpecializationsAdapter);       // установка адаптера полю ввода
        editDoctorsSpecialisation.setOnEditorActionListener(new TextView.OnEditorActionListener() {   // слушаем нажатие "Далее" у поля ввода
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {         // обработка нажатия Enter
                    String newAdd = editDoctorsSpecialisation.getText().toString();
                    String text = newAdd.toLowerCase(Locale.getDefault());
                    // проверим, содержится ли в БД
                    if (fDBMethods.hasDBSpecialization(text) == false) {
                        // добавим введенное слово в БД
                        fDBMethods.insertSpecialization(newAdd);
                    }
                    // передаем фокус и скрываем клавиатуру
                    mToolBar.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editDoctorsSpecialisation.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        editDoctorsSpecialisation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   // выбор из выпадающего списка специализаций при поиске
                TextView tvSpecializationsID = (TextView) view.findViewById(R.id.lrdd_adapter_id);
                mDoctorsSpecialisationsID = Integer.parseInt(tvSpecializationsID.getText().toString());
                //mSpecializationsID = (int)id;   //???????????????????????? переделать на id из БД
                // передаем фокус и скрываем клавиатуру
                mToolBar.requestFocus();
                //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.hideSoftInputFromWindow(editVisitsComment.getWindowToken(), 0);
            }
        });
        TextWatcher watcherSpecialization = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
                if (s.length() <= start) {
                    String text = s.toString().toLowerCase(Locale.getDefault());
                    cursorSpecializations = fDBMethods.getFilteredSpecialisations(text);
                    if (fDBMethods.hasDBSpecialization(text)==false){
                        editDoctorSpecializationsAdapter.swapCursor(cursorSpecializations);
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        };
        editDoctorsSpecialisation.addTextChangedListener(watcherSpecialization);

        editDoctorsSpecialisation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        mDoctorsSpecialisationsID = fDBMethods.getIDSpecializationsByName(editDoctorsSpecialisation.getText().toString());
                    } catch (Throwable t) {
                        mDoctorsSpecialisationsID = 0;
                    }
                }
            }
        });

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
                doctorSave(); // сохраним с закрытием
                break;
            case R.id.common_img_Close:
                doctorClose();
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
        if (cursorSpecializations!=null)
            cursorSpecializations.close();
    }

    public void doctorClose() {
        Intent intent = new Intent();
        intent.putExtra("currentItemID", gettedID); // для правильного позиционирования в списке
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void doctorSave() {
        if (editDoctorName.getText().toString().isEmpty()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mToolBar.getWindowToken(), 0);
            Snackbar snackbar = Common.getCustomSnackbar(MDoctorActivity.this, rlDoctorContainer, getResources().getString(R.string.doctor_save_error));
            snackbar.show();
            return;
        }

        String newAdd = editDoctorsSpecialisation.getText().toString();
        if (newAdd.isEmpty()){
            mDoctorsSpecialisationsID = 0;
        }else {
            // сохраним в списке специализаций также введенную специализацию, если ее там еще нет...
            // ... раз юзер хочет все сохранить
            String text = newAdd.toLowerCase(Locale.getDefault());
            // проверим, содержится ли в БД
            if (fDBMethods.hasDBSpecialization(text) == false) {
                // добавим введенное слово в БД
                mDoctorsSpecialisationsID = fDBMethods.insertSpecialization(newAdd);
            }
        }

        // записать в БД и вернуть "OK" в вызвавшую activity, а там CursorLoader сам обновит список    mSpecializationsID
        if (gettedID == -1){
            // создание нового
            String strName = editDoctorName.getText().toString().trim();
            if (!strName.isEmpty())
                gettedID = (int) fDBMethods.insertDoctor(strName, mDoctorsSpecialisationsID);
        }
        else {   // update
            String strName = editDoctorName.getText().toString().trim();
            // для update вставим также и полученный id
            fDBMethods.updateDoctor(gettedID, strName, mDoctorsSpecialisationsID);
        }

        Intent intent = new Intent();
        intent.putExtra("currentItemID", gettedID);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void doctorSpecializationOpen(View view) {
        Intent intent = new Intent(MDoctorActivity.this, MSpecializationsActivity.class);
        intent.putExtra("choosen__specializations_id", mDoctorsSpecialisationsID);
        startActivityForResult(intent, Constants.SPECIALIZATION_DATA);
    }

    @Override  // получаем ответ от открытых ранее активностей
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            mDoctorsSpecialisationsID = data.getIntExtra("choosen_specializations_id", -1);
            setSpecializationsName();
            mToolBar.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mToolBar.getWindowToken(), 0);
        }
    }

    private void setSpecializationsName(){
        if (mDoctorsSpecialisationsID == 0) {
            editDoctorsSpecialisation.setText("");
        }
        else editDoctorsSpecialisation.setText(fDBMethods.getSpecializationsNameByID(mDoctorsSpecialisationsID));
    }





}
