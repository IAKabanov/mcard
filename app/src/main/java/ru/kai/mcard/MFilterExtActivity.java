package ru.kai.mcard;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ru.kai.mcard.utility.Common;


public class MFilterExtActivity extends AppCompatActivity {

    private DBMethods fDBMethods;

    Toolbar mToolBar;

    EditText editFilterName;

    AutoCompleteTextView editFilterExtClinic;
    AutoCompleteTextView editFilterExtDoctor;
    AutoCompleteTextView editFilterExtSpecialization;
    AutoCompleteTextView editFilterExtCure;
    AutoCompleteTextView editFilterExtAnalisisType;

    CheckBox checkBoxWithoutDate;
    CheckBox checkBoxUseDate;
    CheckBox checkBoxClinic;
    CheckBox checkBoxDoctor;
    CheckBox checkBoxSpecialization;
    CheckBox checkBoxCure;
    CheckBox checkBoxAnalisisType;

    TextView tvFilterExtFirstDate;
    TextView tvFilterExtLastDate;

    // для watcherClinic
    SimpleCursorAdapter filterExtClinicsAdapter;
    Cursor cursorClinics;
    // для watcherDoctor
    SimpleCursorAdapter filterExtDoctorsAdapter;
    Cursor cursorDoctors;
    // для watcherSpecialization
    SimpleCursorAdapter filterExtSpecializationsAdapter;
    Cursor cursorSpecializations;
    // для watcherCures
    SimpleCursorAdapter filterExtCuresAdapter;
    Cursor cursorCures;
    // для watcherAnalisisTypes
    SimpleCursorAdapter filterExtAnalisisTypesAdapter;
    Cursor cursorAnalisisTypes;

    int currentFilterID;

    // поля для записи (реквизиты)
    Long mFilterExtFirstDate = 0L;
    Long mFilterExtLastDate = 0L;
    int mClinicsID;
    int mDoctorsID;
    int mSpecializationsID;
    int mCuresID;
    int mDiagnosisID;
    int mAnalisesTypesID;
    int mRecommendationsID;

    Calendar vDateAndTime;

    // Google analytics (GA)
    private Tracker mTracker; // еще в onCreate и onResume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.m_activity_filter_ext);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA

        mToolBar = (Toolbar) findViewById(R.id.mFilterExtToolbar);
        setSupportActionBar(mToolBar);

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        editFilterName = (EditText)findViewById(R.id.editFilterName);

        editFilterExtClinic = (AutoCompleteTextView) findViewById(R.id.editFilterExtClinic);
        editFilterExtDoctor = (AutoCompleteTextView) findViewById(R.id.editFilterExtDoctor);
        editFilterExtSpecialization = (AutoCompleteTextView) findViewById(R.id.editFilterExtSpecialization);
        editFilterExtCure = (AutoCompleteTextView) findViewById(R.id.editFilterExtCure);
        editFilterExtAnalisisType = (AutoCompleteTextView) findViewById(R.id.editFilterExtAnalisisType);

        tvFilterExtFirstDate = (TextView) findViewById(R.id.tvFilterExtFirstDate);
        tvFilterExtLastDate = (TextView) findViewById(R.id.tvFilterExtLastDate);

        checkBoxWithoutDate = (CheckBox) findViewById(R.id.checkBoxWithoutDate);
        checkBoxUseDate = (CheckBox) findViewById(R.id.checkBoxUseDate);
        checkBoxClinic = (CheckBox) findViewById(R.id.checkBoxClinic);
        checkBoxDoctor = (CheckBox) findViewById(R.id.checkBoxDoctor);
        checkBoxSpecialization = (CheckBox) findViewById(R.id.checkBoxSpecialization);
        checkBoxCure = (CheckBox) findViewById(R.id.checkBoxCure);
        checkBoxAnalisisType = (CheckBox) findViewById(R.id.checkBoxAnalisisType);

        vDateAndTime = Calendar.getInstance();

        currentFilterID = getIntent().getIntExtra("current_filter_id", -1);
        if (currentFilterID != -1){
            Bundle bundle = fDBMethods.getFiltersFildsByFiltersID(currentFilterID);
            if (bundle != null){
                setFields(bundle);
            }
        }

        prepareInitializationsLists();

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
                filterExtSave(); // сохраним с закрытием
                break;
            case R.id.common_img_Close:
                filterExtClose();
                break;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Google analytics (GA)
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "Фильтр расширенный (MFilterExtActivity)");
        mTracker.setScreenName("Фильтр расширенный (MFilterExtActivity)");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fDBMethods != null) {
            fDBMethods.close();
        }
        if (cursorClinics != null)
            cursorClinics.close();
        if (cursorDoctors != null)
            cursorDoctors.close();
        if (cursorSpecializations != null)
            cursorSpecializations.close();
        if (cursorCures != null)
            cursorCures.close();
    }

    @Override  // получаем ответ от открытых ранее активностей
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.VISIT_CLINICS_DATA:
                // если выбрали клинику
                if (resultCode == RESULT_OK) {
                    mClinicsID = data.getIntExtra("choosen_clinic_id", 0);
                    checkBoxClinic.setChecked(true);
                }
                setClinicsName(); // может так случиться, что название клиники изменили, но нажали "Закрыть", а мы все-таки отобразим правильнок имя

                if (editFilterExtDoctor.getText().toString().isEmpty()){
                    editFilterExtDoctor.requestFocus();
                }else tvFilterExtFirstDate.requestFocus();

                break;
            case Constants.VISIT_DOCTORS_DATA:
                // если выбрали доктора
                if (resultCode == RESULT_OK) {
                    mDoctorsID = data.getIntExtra("choosen_doctors_id", 0);
                    checkBoxDoctor.setChecked(true);
                }
                setDoctorsName();

                if (editFilterExtSpecialization.getText().toString().isEmpty()){
                    editFilterExtSpecialization.requestFocus();
                }else tvFilterExtFirstDate.requestFocus();

                break;
            case Constants.VISIT_SPECIALIZATIONS_DATA:
                // если выбрали специализацию доктора
                if (resultCode == RESULT_OK) {
                    mSpecializationsID = data.getIntExtra("choosen_specializations_id", 0);
                    checkBoxSpecialization.setChecked(true);
                }
                setSpecializationsName();
                break;
            case Constants.VISIT_CURES_DATA:
                // если выбрали назначение
                if (resultCode == RESULT_OK) {
                    mCuresID = data.getIntExtra("choosen_cures_id", 0);
                    //mCuresID = Integer.parseInt(data.getStringExtra("choosen_cures_id"));
                    checkBoxCure.setChecked(true);
                }
                setCuresName();
                break;
            case Constants.ANALISES_TYPES_DATA:
                // если выбрали назначение
                if (resultCode == RESULT_OK) {
                    mAnalisesTypesID = data.getIntExtra("given_analises_types_id", 0);
                    checkBoxAnalisisType.setChecked(true);
                }
                setAnalisisTypesName();
                break;
            case Constants.FILTER_CHOICE:
                // если выбрали назначение
                if (resultCode == RESULT_OK) {
                    int chosenFiltersID = data.getIntExtra("chosenFiltersID", 0);
                    Intent intent = new Intent();
                    intent.putExtra("chosenFiltersID", chosenFiltersID);
                    setResult(RESULT_OK, intent);
                    finish();
                }
        }
    }

    private void setFields(Bundle bundle){

        editFilterName.setText(bundle.getString("filterName"));
        checkBoxWithoutDate.setChecked(bundle.getBoolean("onlyWithDate"));

        checkBoxUseDate.setChecked(false);
        mFilterExtFirstDate = bundle.getLong("firstDate");
        setFilterExtFirstDate();
        mFilterExtLastDate = bundle.getLong("lastDate");
        setFilterExtLastDate();

        checkBoxClinic.setChecked(false);
        mClinicsID = bundle.getInt("clinicsID");
        setClinicsName();

        checkBoxDoctor.setChecked(false);
        mDoctorsID = bundle.getInt("doctorsID");
        setDoctorsName();

        checkBoxSpecialization.setChecked(false);
        mSpecializationsID = bundle.getInt("specializationsID");
        setSpecializationsName();

        checkBoxCure.setChecked(false);
        mCuresID = bundle.getInt("curesID");
        setCuresName();

        checkBoxAnalisisType.setChecked(false);
        mAnalisesTypesID = bundle.getInt("analisesTypesID");
        setAnalisisTypesName();

    }

    private void setClinicsName() {
        if (mClinicsID == 0) {
            editFilterExtClinic.setText("");
        } else{
            editFilterExtClinic.setText(fDBMethods.getClinicsNameByID(mClinicsID));
            checkBoxClinic.setChecked(true);
        }
    }

    private void setDoctorsName() {
        if (mDoctorsID == 0) {
            editFilterExtDoctor.setText("");
        } else {
            editFilterExtDoctor.setText(fDBMethods.getDoctorsNameByID(mDoctorsID));
            checkBoxDoctor.setChecked(true);
        }
    }

    private void setSpecializationsName() {
        if (mSpecializationsID == 0) {
            editFilterExtSpecialization.setText("");
        } else {
            editFilterExtSpecialization.setText(fDBMethods.getSpecializationsNameByID(mSpecializationsID));
            checkBoxSpecialization.setChecked(true);
        }
    }

    private void setCuresName() {
        if (mCuresID == 0) {
            editFilterExtCure.setText("");
        } else {
            editFilterExtCure.setText(fDBMethods.getCuresNameByID(mCuresID));
            checkBoxCure.setChecked(true);
        }
    }

    private void setAnalisisTypesName() {
        if (mAnalisesTypesID == 0) {
            editFilterExtAnalisisType.setText("");
        } else {
            editFilterExtAnalisisType.setText(fDBMethods.getAnalisesTypesNameByID(mAnalisesTypesID));
            checkBoxAnalisisType.setChecked(true);
        }
    }

    public void openDataForFilterExt(View view) {
        switch (view.getId()) {
            case (R.id.butFilterExtOpenClinics): {
                Intent intent = new Intent(MFilterExtActivity.this, MClinicsActivity.class);
                intent.putExtra("choosen_clinic_id", mClinicsID);
                startActivityForResult(intent, Constants.VISIT_CLINICS_DATA);
                break;
            }
            case (R.id.butFilterExtOpenDoctors): {
                Intent intent = new Intent(MFilterExtActivity.this, MDoctorsActivity.class);
                intent.putExtra("choosen_doctors_id", mDoctorsID);
                startActivityForResult(intent, Constants.VISIT_DOCTORS_DATA);
                break;
            }
            case (R.id.butFilterExtOpenSpecializations): {
                Intent intent = new Intent(MFilterExtActivity.this, MSpecializationsActivity.class);
                intent.putExtra("choosen__specializations_id", mSpecializationsID);
                startActivityForResult(intent, Constants.VISIT_SPECIALIZATIONS_DATA);
                break;
            }
            case (R.id.butFilterExtOpenCures): {
                Intent intent = new Intent(MFilterExtActivity.this, MCuresActivity.class);
                intent.putExtra("choosen_cures_id", mCuresID);
                startActivityForResult(intent, Constants.VISIT_CURES_DATA);
                break;
            }
            case (R.id.butFilterExtOpenAnalisisTypes): {
                Intent intent = new Intent(MFilterExtActivity.this, MAnalisesTypesActivity.class);
                intent.putExtra("choosen_analisis_type_id", mAnalisesTypesID);
                startActivityForResult(intent, Constants.ANALISES_TYPES_DATA);
                break;
            }
        }
    }

    public void chbChecked(View view) {
        switch (view.getId()) {
            case (R.id.checkBoxUseDate): {
                mFilterExtFirstDate = 0L;
                mFilterExtLastDate = 0L;
                if (!checkBoxUseDate.isChecked()){
                    tvFilterExtFirstDate.setText("");
                    tvFilterExtLastDate.setText("");
                }
                break;
            }
            case (R.id.checkBoxClinic): {
                mClinicsID = 0;
                if (!checkBoxClinic.isChecked())editFilterExtClinic.setText("");
                break;
            }
            case (R.id.checkBoxDoctor): {
                mDoctorsID = 0;
                if (!checkBoxDoctor.isChecked())editFilterExtDoctor.setText("");
                break;
            }
            case (R.id.checkBoxSpecialization): {
                mSpecializationsID = 0;
                if (!checkBoxSpecialization.isChecked())editFilterExtSpecialization.setText("");
                break;
            }
            case (R.id.checkBoxCure): {
                mCuresID = 0;
                if (!checkBoxCure.isChecked())editFilterExtCure.setText("");
                break;
            }
            case (R.id.checkBoxAnalisisType): {
                mAnalisesTypesID = 0;
                if (!checkBoxAnalisisType.isChecked())editFilterExtAnalisisType.setText("");
                break;
            }
        }
    }

    // загружаем список для watcherClinic
    private void prepareInitializationsLists() {
        // prepare your list of words for AutoComplete
        //////////////////////////////////////////////
        // clinics
        cursorClinics = fDBMethods.getFilteredClinics("");  // полная таблица
        String[] fromClinics = {DBMethods.TableClinics._ID, DBMethods.TableClinics.COLUMN_CLINICS_NAME};
        int[] toClinics = {R.id.lrdd_adapter_id, R.id.lrdd_adapter_name};
        filterExtClinicsAdapter = new SimpleCursorAdapter(MFilterExtActivity.this, R.layout.list_row_dropdown, cursorClinics, fromClinics, toClinics, 0); // новый адаптер
        filterExtClinicsAdapter.setStringConversionColumn(cursorClinics.getColumnIndexOrThrow("name"));  // чтобы привыборе из выпадающего списка подставлялось значение, а не козябры
        editFilterExtClinic.setAdapter(filterExtClinicsAdapter);       // установка адаптера полю ввода
        editFilterExtClinic.setOnEditorActionListener(new TextView.OnEditorActionListener() {   // слушаем нажатие "Далее" у поля ввода
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {         // обработка нажатия Enter
                    // передаем фокус и скрываем клавиатуру
                    editFilterExtDoctor.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editFilterExtDoctor.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });
        editFilterExtClinic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   // выбор из выпадающего списка
                TextView tvClinicID = (TextView) view.findViewById(R.id.lrdd_adapter_id);
                mClinicsID = Integer.parseInt(tvClinicID.getText().toString());
                checkBoxClinic.setChecked(true);
                // передаем фокус и скрываем клавиатуру
                editFilterExtDoctor.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editFilterExtDoctor.getWindowToken(), 0);
            }
        });
        TextWatcher watcherClinic = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                //if (loadedFromClinics) return;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
                //if (loadedFromClinics) return;
                if (s.length() <= start) {
                    cursorClinics = fDBMethods.getFilteredClinics(s.toString());
                    filterExtClinicsAdapter.swapCursor(cursorClinics);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                //if (loadedFromClinics) return;

                //String text = s.toString().toLowerCase(Locale.getDefault());
            }
        };
        editFilterExtClinic.addTextChangedListener(watcherClinic);

        //////////////////////////////////////////////
        // doctors
        cursorDoctors = fDBMethods.getFilteredDoctors("");  // полная таблица
        String[] fromDoctors = {DBMethods.TableDoctors._ID, DBMethods.TableDoctors.COLUMN_DOCTORS_NAME};
        int[] toDoctors = {R.id.lrdd_adapter_id, R.id.lrdd_adapter_name};
        filterExtDoctorsAdapter = new SimpleCursorAdapter(MFilterExtActivity.this, R.layout.list_row_dropdown, cursorDoctors, fromDoctors, toDoctors, 0); // новый адаптер
        filterExtDoctorsAdapter.setStringConversionColumn(cursorDoctors.getColumnIndexOrThrow("name"));  // чтобы привыборе из выпадающего списка подставлялось значение, а не козябры
        editFilterExtDoctor.setAdapter(filterExtDoctorsAdapter);       // установка адаптера полю ввода
        editFilterExtDoctor.setOnEditorActionListener(new TextView.OnEditorActionListener() {   // слушаем нажатие "Далее" у поля ввода
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {         // обработка нажатия Enter
                    // передаем фокус и скрываем клавиатуру
                    if (editFilterExtSpecialization.getText().toString().isEmpty()){
                        editFilterExtSpecialization.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editFilterExtSpecialization.getWindowToken(), 0);
                    }else {
                        tvFilterExtFirstDate.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(tvFilterExtFirstDate.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });
        editFilterExtDoctor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   // выбор из выпадающего списка
                TextView tvDoctorID = (TextView) view.findViewById(R.id.lrdd_adapter_id);
                mDoctorsID = Integer.parseInt(tvDoctorID.getText().toString());
                checkBoxDoctor.setChecked(true);
                // передаем фокус и скрываем клавиатуру
                if (editFilterExtSpecialization.getText().toString().isEmpty()){
                    editFilterExtSpecialization.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editFilterExtSpecialization.getWindowToken(), 0);
                }else {
                    tvFilterExtFirstDate.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(tvFilterExtFirstDate.getWindowToken(), 0);
                }
            }
        });
        TextWatcher watcherDoctors = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
                //if (loadedFromDoctors) return;
                if (s.length() <= start) {
                    cursorDoctors = fDBMethods.getFilteredDoctors(s.toString());
                    filterExtDoctorsAdapter.swapCursor(cursorDoctors);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                //if (loadedFromDoctors) return;

                //String text = s.toString().toLowerCase(Locale.getDefault());
            }
        };
        editFilterExtDoctor.addTextChangedListener(watcherDoctors);

        //////////////////////////////////////////////
        // specializations
        cursorSpecializations = fDBMethods.getFilteredSpecialisations("");  // полная таблица
        String[] fromSpecializations = {DBMethods.TableSpecializations._ID, DBMethods.TableSpecializations.COLUMN_SPECIALIZATIONS_NAME};
        int[] toSpecializations = {R.id.lrdd_adapter_id, R.id.lrdd_adapter_name};
        filterExtSpecializationsAdapter = new SimpleCursorAdapter(MFilterExtActivity.this, R.layout.list_row_dropdown, cursorSpecializations, fromSpecializations, toSpecializations, 0); // новый адаптер
        filterExtSpecializationsAdapter.setStringConversionColumn(cursorSpecializations.getColumnIndexOrThrow("name"));  // чтобы привыборе из выпадающего списка подставлялось значение, а не козябры
        editFilterExtSpecialization.setAdapter(filterExtSpecializationsAdapter);       // установка адаптера полю ввода
        editFilterExtSpecialization.setOnEditorActionListener(new TextView.OnEditorActionListener() {   // слушаем нажатие "Далее" у поля ввода
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {         // обработка нажатия Enter
                    // передаем фокус и скрываем клавиатуру
                    tvFilterExtFirstDate.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(tvFilterExtFirstDate.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        editFilterExtSpecialization.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   // выбор из выпадающего списка
                TextView tvSpecializationsID = (TextView) view.findViewById(R.id.lrdd_adapter_id);
                mSpecializationsID = Integer.parseInt(tvSpecializationsID.getText().toString());
                checkBoxSpecialization.setChecked(true);
                // передаем фокус и скрываем клавиатуру
                tvFilterExtFirstDate.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tvFilterExtFirstDate.getWindowToken(), 0);
            }
        });
        TextWatcher watcherSpecialization = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                //if (loadedFromSpecializations) return;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
                //if (loadedFromSpecializations) return;
                if (s.length() <= start) {
                    String text = s.toString().toLowerCase(Locale.getDefault());
                    cursorSpecializations = fDBMethods.getFilteredSpecialisations(text);
                    if (fDBMethods.hasDBSpecialization(text) == false) {
                        filterExtSpecializationsAdapter.swapCursor(cursorSpecializations);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        };
        editFilterExtSpecialization.addTextChangedListener(watcherSpecialization);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // предзагрузка списка лекарств для организации поиска
        cursorCures = fDBMethods.getFilteredCures("");  // полная таблица
        String[]fromCures = {DBMethods.TableCures.COLUMN_CURES_NAME};
        int[]toCures = {R.id.lrdd_adapter_name};
        filterExtCuresAdapter = new SimpleCursorAdapter(MFilterExtActivity.this, R.layout.list_row_dropdown, cursorCures, fromCures, toCures, 0); // новый адаптер
        filterExtCuresAdapter.setStringConversionColumn(cursorCures.getColumnIndexOrThrow("name"));  // чтобы привыборе из выпадающего списка подставлялось значение, а не козябры
        editFilterExtCure.setAdapter(filterExtCuresAdapter);       // установка адаптера полю ввода
        editFilterExtCure.setOnEditorActionListener(new TextView.OnEditorActionListener() {   // слушаем нажатие "Далее" у поля ввода
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {         // обработка нажатия Enter
                    // передаем фокус и скрываем клавиатуру
                    tvFilterExtFirstDate.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(tvFilterExtFirstDate.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        editFilterExtCure.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   // выбор из выпадающего списка
                mCuresID = fDBMethods.getIDCuresByName(editFilterExtCure.getText().toString());
                checkBoxCure.setChecked(true);
                // передаем фокус и скрываем клавиатуру
                tvFilterExtFirstDate.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tvFilterExtFirstDate.getWindowToken(), 0);
            }
        });
        TextWatcher watcherCures = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                //if (loadedFromCures)return;
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
                //if (loadedFromCures)return;
                if (s.length() <= start) {
                    String text = s.toString().toLowerCase(Locale.getDefault());
                    cursorCures = fDBMethods.getFilteredCures(text);
                    filterExtCuresAdapter.swapCursor(cursorCures);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                //if (loadedFromCures)return;
            }
        };
        editFilterExtCure.addTextChangedListener(watcherCures);
        ////////////////////////////////////////////////////////////////////////////////////////////
        // предзагрузка списка исследований (анализов) для организации поиска
        cursorAnalisisTypes = fDBMethods.getFilteredAnalisesTypes("");  // полная таблица
        String[]fromAnalisisTypes = {DBMethods.TableAnalisesTypes.COLUMN_ANALISES_TYPES_NAME};
        int[]toAnalisisTypes = {R.id.lrdd_adapter_name};
        filterExtAnalisisTypesAdapter = new SimpleCursorAdapter(MFilterExtActivity.this, R.layout.list_row_dropdown, cursorAnalisisTypes, fromAnalisisTypes, toAnalisisTypes, 0); // новый адаптер
        filterExtAnalisisTypesAdapter.setStringConversionColumn(cursorAnalisisTypes.getColumnIndexOrThrow("name"));  // чтобы привыборе из выпадающего списка подставлялось значение, а не козябры
        editFilterExtAnalisisType.setAdapter(filterExtAnalisisTypesAdapter);       // установка адаптера полю ввода
        editFilterExtAnalisisType.setOnEditorActionListener(new TextView.OnEditorActionListener() {   // слушаем нажатие "Далее" у поля ввода
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {         // обработка нажатия Enter
                    // передаем фокус и скрываем клавиатуру
                    tvFilterExtFirstDate.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(tvFilterExtFirstDate.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        editFilterExtAnalisisType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   // выбор из выпадающего списка
                mAnalisesTypesID = fDBMethods.getIDAnalisesTypesByName(editFilterExtAnalisisType.getText().toString());
                checkBoxAnalisisType.setChecked(true);
                // передаем фокус и скрываем клавиатуру
                tvFilterExtFirstDate.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tvFilterExtFirstDate.getWindowToken(), 0);
            }
        });
        TextWatcher watcherAnalisisTypes = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                //if (loadedFromCures)return;
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
                //if (loadedFromCures)return;
                if (s.length() <= start) {
                    String text = s.toString().toLowerCase(Locale.getDefault());
                    cursorAnalisisTypes = fDBMethods.getFilteredAnalisesTypes(text);
                    filterExtAnalisisTypesAdapter.swapCursor(cursorAnalisisTypes);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                //if (loadedFromCures)return;
            }
        };
        editFilterExtAnalisisType.addTextChangedListener(watcherAnalisisTypes);
        ////////////////////////////////////////////////////////////////////////////////////////////

    }

    public void setFirstDate(View view) {
        Date vDate;
        if (mFilterExtFirstDate == 0L) {
            vDate = new Date();
        } else {
            vDate = new Date(mFilterExtFirstDate);
        }

        vDateAndTime.setTime(vDate);

        new DatePickerDialog(MFilterExtActivity.this, fd,
                vDateAndTime.get(Calendar.YEAR),
                vDateAndTime.get(Calendar.MONTH),
                vDateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // установка обработчика выбора начальной даты
    DatePickerDialog.OnDateSetListener fd = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            vDateAndTime.set(Calendar.YEAR, year);
            vDateAndTime.set(Calendar.MONTH, monthOfYear);
            vDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            Long mFirstDate = vDateAndTime.getTimeInMillis();

            // начальная дата не может быть больше конечной
            if (mFilterExtLastDate !=0){
                if (mFirstDate > mFilterExtLastDate){
                    Toast.makeText(MFilterExtActivity.this, R.string.filter_ext_error_first_date, Toast.LENGTH_LONG).show();
                    return;
                }
            }

            mFilterExtFirstDate = vDateAndTime.getTimeInMillis();
            setFilterExtFirstDate();
        }
    };

    private void setFilterExtFirstDate() {
        if (mFilterExtFirstDate == 0L) {
            tvFilterExtFirstDate.setText("");
        } else {
            CharSequence date = DateFormat.format("dd.MM.yy", mFilterExtFirstDate);
            tvFilterExtFirstDate.setText(date);
            checkBoxUseDate.setChecked(true);
        }
    }

    public void setLastDate(View view) {
        Date vDate;
        if (mFilterExtLastDate == 0L) {
            vDate = new Date();
        } else {
            vDate = new Date(mFilterExtLastDate);
        }

        vDateAndTime.setTime(vDate);

        new DatePickerDialog(MFilterExtActivity.this, ld,
                vDateAndTime.get(Calendar.YEAR),
                vDateAndTime.get(Calendar.MONTH),
                vDateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // установка обработчика выбора конечной даты
    DatePickerDialog.OnDateSetListener ld = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            vDateAndTime.set(Calendar.YEAR, year);
            vDateAndTime.set(Calendar.MONTH, monthOfYear);
            vDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            Long mLastDate = vDateAndTime.getTimeInMillis();

            // конечная дата не может быть меньше начальной
            if (mFilterExtFirstDate !=0){
                if (mFilterExtFirstDate > mLastDate){
                    Toast.makeText(MFilterExtActivity.this, R.string.filter_ext_error_last_date, Toast.LENGTH_LONG).show();
                    return;
                }
            }

            mFilterExtLastDate = vDateAndTime.getTimeInMillis();
            setFilterExtLastDate();
        }
    };

    private void setFilterExtLastDate() {
        if (mFilterExtLastDate == 0L) {
            tvFilterExtLastDate.setText("");
        } else {
            CharSequence date = DateFormat.format("dd.MM.yy", mFilterExtLastDate);
            tvFilterExtLastDate.setText(date);
            checkBoxUseDate.setChecked(true);
        }
    }


    public void delFilterExtFirstDate(View view) {
        mFilterExtFirstDate = 0L;
        tvFilterExtFirstDate.setText("");
        if (mFilterExtLastDate == 0L)checkBoxUseDate.setChecked(false);
    }

    public void delFilterExtLastDate(View view) {
        mFilterExtLastDate = 0L;
        tvFilterExtLastDate.setText("");
        if (mFilterExtFirstDate == 0L)checkBoxUseDate.setChecked(false);
    }

    private void setFilterName(){
        if (!editFilterName.getText().toString().isEmpty())return;

        StringBuffer stringBuffer = new StringBuffer();
        if (checkBoxWithoutDate.isChecked())stringBuffer.append("Только с датой");
        if (mFilterExtFirstDate != 0L){
            if (!stringBuffer.toString().isEmpty())stringBuffer.append("; ");
            stringBuffer.append("с ").append(tvFilterExtFirstDate.getText().toString());
        }
        if (mFilterExtLastDate != 0L){
            if (stringBuffer.toString().isEmpty())stringBuffer.append("; ");
            stringBuffer.append("до ").append(tvFilterExtLastDate.getText().toString());
        }
        if (mClinicsID != 0){
            if (!stringBuffer.toString().isEmpty())stringBuffer.append("; ");
            stringBuffer.append(editFilterExtClinic.getText().toString());
        }
        if (mDoctorsID != 0){
            if (!stringBuffer.toString().isEmpty())stringBuffer.append("; ");
            stringBuffer.append(editFilterExtDoctor.getText().toString());
        }
        if (mSpecializationsID != 0){
            if (!stringBuffer.toString().isEmpty())stringBuffer.append("; ");
            stringBuffer.append(editFilterExtSpecialization.getText().toString());
        }
        if (mCuresID != 0){
            if (!stringBuffer.toString().isEmpty()) stringBuffer.append("; ");
            stringBuffer.append(editFilterExtCure.getText().toString());
        }
        if (mAnalisesTypesID != 0){
            if (!stringBuffer.toString().isEmpty()) stringBuffer.append("; ");
            stringBuffer.append(editFilterExtAnalisisType.getText().toString());
        }

        editFilterName.setText(stringBuffer.toString());
    }

    public void filterExtSave() {

        // Google analytics (GA)
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Применили расширенный фильтр.")
                .build());


        setFilterName();
        String currentFilterName = editFilterName.getText().toString();

        Bundle bundle = new Bundle();
        bundle.putString("filterName", currentFilterName);
        bundle.putBoolean("onlyWithDate", checkBoxWithoutDate.isChecked());
        bundle.putLong("firstDate", mFilterExtFirstDate);
        bundle.putLong("lastDate", mFilterExtLastDate);
        bundle.putInt("clinicsID", mClinicsID);
        bundle.putInt("doctorsID", mDoctorsID);
        bundle.putInt("specializationsID", mSpecializationsID);
        bundle.putInt("curesID", mCuresID);
        bundle.putInt("diagnosisID", mDiagnosisID);
        bundle.putInt("analisesTypesID", mAnalisesTypesID);
        bundle.putInt("recommendationsID", mRecommendationsID);

        if (currentFilterID == -1){

            if (!fDBMethods.hasDBFilter(currentFilterName)) {
                // добавим введенное слово в БД
                currentFilterID = fDBMethods.insertFilter(bundle);
            }else currentFilterID = fDBMethods.getIDFiltersByName(currentFilterName);

        }else fDBMethods.updateFilter(currentFilterID, bundle);

        Intent intent = new Intent();
        intent.putExtra("chosenFiltersID", currentFilterID);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void filterExtClose() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void btnFilterExtSave(View view) {
        filterExtSave();
    }


}
