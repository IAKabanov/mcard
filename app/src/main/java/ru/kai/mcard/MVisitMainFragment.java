package ru.kai.mcard;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class MVisitMainFragment extends Fragment  implements View.OnClickListener{

    // поля визита, которые будут сохранены в БД
    AutoCompleteTextView editVisitsSpecialization;
    AutoCompleteTextView editVisitsClinic;
    AutoCompleteTextView editVisitsDoctor;
    AutoCompleteTextView editVisitsDiagnosis;
    EditText editVisitsComment;
    TextView tvVisitsDate;
    TextView tvVisitsTime;
    EditText editRefVisit;

    EditText editVisitsMDate;
    EditText editVisitsMTime;

    LinearLayout llVisitsMRef;

    Button butOpenVisitMClinics;
    Button butOpenVisitsMDoctor;
    Button butOpenVisitsMSpecialization;
    Button butOpenVisitsMDiagnosis;
    Button butShowVisitMPhoto;
    ImageButton btn_browseRefVisit;

    private DBMethods fDBMethods;

    // для watcherClinic
    SimpleCursorAdapter editVisitsClinicsAdapter;
    Cursor cursorClinics;
    // для watcherDoctor
    SimpleCursorAdapter editVisitsDoctorsAdapter;
    Cursor cursorDoctors;
    // для watcherSpecialization
    SimpleCursorAdapter editVisitsSpecializationsAdapter;
    Cursor cursorSpecializations;
    // для watcherDiagnosis
    SimpleCursorAdapter editVisitsDiagnosesAdapter;
    Cursor cursorDiagnoses;

    int gettedID; // для идентификации визита и update или delete
    Calendar vDateAndTime;

    // профиль
    int mCurrentProfileID;

    // поля для записи (реквизиты)
    public Long mVisitsDateTime = 0L;
    int mClinicsID;
    int mDoctorsID;
    int mSpecializationsID;
    int mDiagnosisID;
    String mComment;
    int mReferencesID;

    MVisitActivity activity;

    public MVisitMainFragment() {
        // Required empty public constructor
    }

    public static MVisitMainFragment newInstance(Bundle bundle) {
        // получение аргументов из активности
        MVisitMainFragment currentFragment = new MVisitMainFragment();
        Bundle args = new Bundle();
        args.putBundle("gettedArgs", bundle);
        currentFragment.setArguments(args);
        return currentFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_visit_m_main, container, false);

        activity = (MVisitActivity)getActivity();

        editVisitsClinic = (AutoCompleteTextView) rootView.findViewById(R.id.editVisitsMClinic);
        editVisitsDoctor = (AutoCompleteTextView) rootView.findViewById(R.id.editVisitsMDoctor);
        editVisitsSpecialization = (AutoCompleteTextView) rootView.findViewById(R.id.editVisitsMSpecialization);
        editVisitsDiagnosis = (AutoCompleteTextView) rootView.findViewById(R.id.editVisitsMDiagnosis);
        editVisitsComment = (EditText) rootView.findViewById(R.id.editVisitsMComment);
        tvVisitsDate = (TextView) rootView.findViewById(R.id.tvVisitsMDate);
        tvVisitsTime = (TextView) rootView.findViewById(R.id.tvVisitsMTime);
        editRefVisit = (EditText) rootView.findViewById(R.id.editRefVisit);

        llVisitsMRef = (LinearLayout) rootView.findViewById(R.id.llVisitsMRef);

        butOpenVisitMClinics = (Button)rootView.findViewById(R.id.butOpenVisitsMClinics);
        butOpenVisitsMDoctor = (Button)rootView.findViewById(R.id.butOpenVisitsMDoctor);
        butOpenVisitsMSpecialization = (Button)rootView.findViewById(R.id.butOpenVisitsMSpecialization);
        butOpenVisitsMDiagnosis = (Button)rootView.findViewById(R.id.butOpenVisitsMDiagnosis);
        butShowVisitMPhoto = (Button)rootView.findViewById(R.id.butShowVisitsMPhoto);
        btn_browseRefVisit = (ImageButton)rootView.findViewById(R.id.btn_browseRefVisit);

        vDateAndTime = Calendar.getInstance();

        fDBMethods = new DBMethods(getContext());
        fDBMethods.open();

        // при первом открытии (не поворот экрана)
        if (savedInstanceState == null) {
            // получаем из активности
            Bundle bundle = getArguments().getBundle("gettedArgs");
            gettedID = bundle.getInt("gettedID", 0);
            mCurrentProfileID = bundle.getInt("currentProfileID", -1);
            Long currDateTime = bundle.getLong("mVisitsDateTime", 0L); // чтобы при листании фрагментов не терялись дата и время
            if (mVisitsDateTime != 0L){
                int aa = -1;
            }else {
                mVisitsDateTime = currDateTime;
            }
            //mVisitsDateTime = bundle.getLong("mVisitsDateTime", 0L);
            mClinicsID = bundle.getInt("clinicsID", 0);
            mDoctorsID = bundle.getInt("doctorsID", 0);
            mSpecializationsID = bundle.getInt("specializationsID", 0);
            mDiagnosisID = bundle.getInt("diagnosisID", 0);
            mComment = bundle.getString("vComment");
            mReferencesID = bundle.getInt("referencesID", 0);
        }

        editVisitsComment.setText(mComment);


/*
        if (savedInstanceState != null){
            mClinicsID = savedInstanceState.getInt("mClinicsID", mClinicsID);
        }
*/


        setVisitsDate();
        setVisitsTime();
        setClinicsName();
        setDoctorsName();
        setSpecializationsName();
        setDiagnosisName();
        setReference();

        prepareInitializationsLists();

        tvVisitsDate.setOnClickListener(this);
        tvVisitsTime.setOnClickListener(this);
        butOpenVisitMClinics.setOnClickListener(this);
        butOpenVisitsMDoctor.setOnClickListener(this);
        butOpenVisitsMSpecialization.setOnClickListener(this);
        butOpenVisitsMDiagnosis.setOnClickListener(this);
        butShowVisitMPhoto.setOnClickListener(this);
        btn_browseRefVisit.setOnClickListener(this);

        // если удалили в поле спомощь кн. "Backspace" (клавиатурой)
        editVisitsClinic.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    try {
                        mClinicsID = fDBMethods.getIDClinicsByName(editVisitsClinic.getText().toString());
                    }catch (Throwable t){
                        mClinicsID = 0;
                    }
                }
            }
        });
        editVisitsDoctor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    try {
                        mDoctorsID = fDBMethods.getIDDoctorsByName(editVisitsDoctor.getText().toString());
                    }catch (Throwable t){
                        mDoctorsID = 0;
                    }
                }
            }
        });
        editVisitsSpecialization.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        mSpecializationsID = fDBMethods.getIDSpecializationsByName(editVisitsSpecialization.getText().toString());
                    } catch (Throwable t) {
                        mSpecializationsID = 0;
                    }
                }
            }
        });


        return rootView;
    }

    @Override  // получаем ответ от открытых ранее активностей
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.VISIT_CLINICS_DATA:
                // если выбрали клинику
                if (resultCode == activity.RESULT_OK) {
                    mClinicsID = data.getIntExtra("choosen_clinic_id", 0);
                }
                setClinicsName(); // может так случиться, что название клиники изменили, но нажали "Закрыть", а мы все-таки отобразим правильнок имя

                if (editVisitsDoctor.getText().toString().isEmpty()){
                    editVisitsDoctor.requestFocus();
                }else editVisitsComment.requestFocus();

                break;
            case Constants.VISIT_DOCTORS_DATA:
                // если выбрали доктора
                if (resultCode == activity.RESULT_OK) {
                    mDoctorsID = data.getIntExtra("choosen_doctors_id", 0);
                    // установим также и специализацию доктора
                    mSpecializationsID = fDBMethods.getDoctorsSpecializationIDByDoctorsID(mDoctorsID);
                }
                setDoctorsName(); // может так случиться, что название клиники изменили, но нажали "Закрыть", а мы все-таки отобразим правильнок имя
                setSpecializationsName();

                break;
            case Constants.VISIT_SPECIALIZATIONS_DATA:
                // если выбрали специализацию доктора
                if (resultCode == activity.RESULT_OK) {
                    mSpecializationsID = data.getIntExtra("choosen_specializations_id", 0);
                }
                setSpecializationsName(); // может так случиться, что название клиники изменили, но нажали "Закрыть", а мы все-таки отобразим правильнок имя

                break;
            case Constants.VISITS_DIAGNOSES_DATA:
                // если выбрали диагноз
                if (resultCode == activity.RESULT_OK) {
                    mDiagnosisID = data.getIntExtra("choosen_diagnoses_id", 0);
                }
                setDiagnosisName(); // может так случиться, что название клиники изменили, но нажали "Закрыть", а мы все-таки отобразим правильнок имя

                break;
            case Constants.REFERENCE_BASIC_VISIT:
                Bundle bundle = fDBMethods.getVisitsDirectionsFildsByVisitsDirectionsID(Constants.DIRECTIONS_VISITS_TYPE_0, gettedID);
                mReferencesID = bundle.getInt("basicVisitsID", 0);
                if (mReferencesID == 0){
                    llVisitsMRef.setVisibility(View.GONE);
                }
        }

    }

    private void setVisitsDate() {
        if (mVisitsDateTime == 0L) {
            tvVisitsDate.setText("");
        } else {
            CharSequence date = DateFormat.format("dd.MM.yy", mVisitsDateTime);
            tvVisitsDate.setText(date);
            // установим также и текущее время, если оно еще не установлено
            if (tvVisitsTime.getText().toString().isEmpty()) setVisitsTime();
        }
    }

    private void setVisitsTime() {
        if (mVisitsDateTime == 0L) {
            tvVisitsTime.setText("");
        } else {
            //CharSequence date = DateFormat.format("dd.MM.yy", mVisitsTime);
            CharSequence date = DateUtils.formatDateTime(getContext(), mVisitsDateTime, DateUtils.FORMAT_SHOW_TIME);
            tvVisitsTime.setText(date);
            // установим также и текущую дату, если она еще не установлена
            if (tvVisitsDate.getText().toString().isEmpty()) setVisitsDate();
        }
    }

    private void setClinicsName() {
        if (mClinicsID == 0) {
            editVisitsClinic.setText("");
        } else editVisitsClinic.setText(fDBMethods.getClinicsNameByID(mClinicsID));
    }

    private void setDoctorsName() {
        if (mDoctorsID == 0) {
            editVisitsDoctor.setText("");
        } else {
            // здесь все сложнее: user может установить специализацию доктора в визите отличную от специализации доктора
            editVisitsDoctor.setText(fDBMethods.getDoctorsNameByID(mDoctorsID));
        }
    }

    private void setSpecializationsName() {
        if (mSpecializationsID == 0) {
            editVisitsSpecialization.setText("");
        } else
            editVisitsSpecialization.setText(fDBMethods.getSpecializationsNameByID(mSpecializationsID));
    }

    private void setDiagnosisName() {
        if (mDiagnosisID == 0) {
            editVisitsDiagnosis.setText("");
        } else editVisitsDiagnosis.setText(fDBMethods.getDiagnosesNameByID(mDiagnosisID));
    }

    private void setReference() {
        if (mReferencesID == 0) {
            editRefVisit.setText("");
            llVisitsMRef.setVisibility(View.GONE);
            //editRefVisit.setVisibility(View.GONE);
            //btn_browseRefVisit.setVisibility(View.GONE);
        } else{
            llVisitsMRef.setVisibility(View.VISIBLE);
            //editRefVisit.setVisibility(View.VISIBLE);
            //btn_browseRefVisit.setVisibility(View.VISIBLE);
            editRefVisit.setText(fDBMethods.getVisitsViewByID(mReferencesID));
        }
    }

/*
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mClinicsID", mClinicsID);
    }
*/

    @Override
    public void onResume() {
        super.onResume();
        OnGetVisitsID listener = (OnGetVisitsID)getActivity();
        gettedID = listener.onGetVisitsID();
    }

    @Override
    public void onDestroy() {
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
        if (cursorDiagnoses != null)
            cursorDiagnoses.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvVisitsMDate:
                setDate();
                break;
            case R.id.tvVisitsMTime:
                setTime();
                break;
            case R.id.butOpenVisitsMClinics:
                Intent intentC = new Intent(getContext(), MClinicsActivity.class);
                intentC.putExtra("choosen_clinic_id", mClinicsID);
                startActivityForResult(intentC, Constants.VISIT_CLINICS_DATA);
                break;
            case R.id.butOpenVisitsMDoctor:
                Intent intentD = new Intent(getContext(), MDoctorsActivity.class);
                intentD.putExtra("choosen_doctors_id", mDoctorsID);
                startActivityForResult(intentD, Constants.VISIT_DOCTORS_DATA);
                break;
            case R.id.butOpenVisitsMSpecialization:
                Intent intentS = new Intent(getContext(), MSpecializationsActivity.class);
                intentS.putExtra("choosen__specializations_id", mSpecializationsID);
                startActivityForResult(intentS, Constants.VISIT_SPECIALIZATIONS_DATA);
                break;
            case R.id.butOpenVisitsMDiagnosis:
                Intent intent = new Intent(getContext(), MDiagnosesActivity.class);
                intent.putExtra("choosen_diagnosis_id", mDiagnosisID);
                startActivityForResult(intent, Constants.VISITS_DIAGNOSES_DATA);
                break;
            case R.id.butShowVisitsMPhoto:
                visitsPhotoOpen();
                break;
            case R.id.btn_browseRefVisit:
                Intent intentR = new Intent(getContext(), MVisitActivity.class);
                intentR.putExtra("choosen_visits_id", mReferencesID);
                intentR.putExtra("currentProfileID", mCurrentProfileID);
                startActivityForResult(intentR, Constants.REFERENCE_BASIC_VISIT);
                break;
        }

    }

    public void setDate() {
        Date vDate;
        if (mVisitsDateTime == 0L) {
            vDate = new Date();
        } else {
            vDate = new Date(mVisitsDateTime);
        }

        vDateAndTime.setTime(vDate);

        new DatePickerDialog(getContext(), d,
                vDateAndTime.get(Calendar.YEAR),
                vDateAndTime.get(Calendar.MONTH),
                vDateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();

    }

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            vDateAndTime.set(Calendar.YEAR, year);
            vDateAndTime.set(Calendar.MONTH, monthOfYear);
            vDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            mVisitsDateTime = vDateAndTime.getTimeInMillis();
            setVisitsDate();

        }
    };

    public void setTime() {
        Date vTime;
        if (mVisitsDateTime == 0L) {
            vTime = new Date();
        } else {
            vTime = new Date(mVisitsDateTime);
        }

        vDateAndTime.setTime(vTime);

        new TimePickerDialog(getContext(), t,
                vDateAndTime.get(Calendar.HOUR_OF_DAY),
                vDateAndTime.get(Calendar.MINUTE), true)
                .show();
    }

    // установка обработчика выбора времени
    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            vDateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            vDateAndTime.set(Calendar.MINUTE, minute);

            mVisitsDateTime = vDateAndTime.getTimeInMillis();
            setVisitsTime();
        }
    };

    public void visitsPhotoOpen() {

        if (gettedID > 0){
            Bundle bundle = new Bundle();
            bundle.putInt("typeOfVisit", Constants.DIRECTIONS_VISITS_TYPE_0);
            bundle.putInt("basicVisitsID", gettedID);
            bundle.putInt("currentProfileID", mCurrentProfileID);
            bundle.putInt("whyPhotoOpened", Constants.PHOTO_ADD);

            Intent intent = new Intent(getContext(), PhotoScrollingActivity.class);
            intent.putExtra("photosBundle", bundle);
            startActivityForResult(intent, Constants.PHOTO_LIST_OPEN);
        }else {
            String title = getResources().getString(R.string.save_visit_question);
            //String message = getResources().getString(R.string.do_not_show_help_info);
            String btnPositiveString = getResources().getString(R.string.dialog_positive_button_text);
            String btnNegativeString = getResources().getString(R.string.dialog_negative_button_text);

            AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
            ad.setTitle(title);  // заголовок
            //ad.setMessage(message); // сообщение
            ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    // запишем визит и продолжим:
                    OnGetSaveVisitCommand listener = (OnGetSaveVisitCommand)getActivity();
                    gettedID = listener.onGetSaveVisitCommand();

                    Bundle bundle = new Bundle();
                    bundle.putInt("typeOfVisit", Constants.DIRECTIONS_VISITS_TYPE_0);
                    bundle.putInt("basicVisitsID", gettedID);
                    bundle.putInt("currentProfileID", mCurrentProfileID);
                    bundle.putInt("whyPhotoOpened", Constants.PHOTO_ADD);

                    Intent intent = new Intent(getContext(), PhotoScrollingActivity.class);
                    intent.putExtra("photosBundle", bundle);
                    startActivityForResult(intent, Constants.PHOTO_LIST_OPEN);
                }
            });
            ad.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    //setResult(RESULT_CANCELED);
                    //finish();
                }
            });
            ad.show();

        }

/*
        if (gettedID == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            //builder.setTitle("Параметры экрана.")
            builder .setMessage("Визит еще не сохранен. Фотографий также нет.")
                    .setCancelable(false)
                    .setNegativeButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }else {
            Bundle bundle = new Bundle();
            bundle.putInt("typeOfVisit", Constants.DIRECTIONS_VISITS_TYPE_0);
            bundle.putInt("basicVisitsID", gettedID);
            bundle.putInt("whyPhotoOpened", Constants.PHOTO_ADD);

            Intent intent = new Intent(getContext(), PhotoScrollingActivity.class);
            intent.putExtra("photosBundle", bundle);
            startActivityForResult(intent, Constants.PHOTO_LIST_OPEN);
        }
*/

    }

    // загружаем список для watcherClinic
    private void prepareInitializationsLists() {
        // prepare your list of words for AutoComplete
        //////////////////////////////////////////////
        // clinics
        cursorClinics = fDBMethods.getFilteredClinics("");  // полная таблица
        String[] fromClinics = {DBMethods.TableClinics._ID, DBMethods.TableClinics.COLUMN_CLINICS_NAME};
        int[] toClinics = {R.id.lrdd_adapter_id, R.id.lrdd_adapter_name};
        editVisitsClinicsAdapter = new SimpleCursorAdapter(getContext(), R.layout.list_row_dropdown, cursorClinics, fromClinics, toClinics, 0); // новый адаптер
        editVisitsClinicsAdapter.setStringConversionColumn(cursorClinics.getColumnIndexOrThrow("name"));  // чтобы привыборе из выпадающего списка подставлялось значение, а не козябры
        editVisitsClinic.setAdapter(editVisitsClinicsAdapter);       // установка адаптера полю ввода
        editVisitsClinic.setOnEditorActionListener(new TextView.OnEditorActionListener() {   // слушаем нажатие "Далее" у поля ввода
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {         // обработка нажатия Enter
                    String newAdd = editVisitsClinic.getText().toString();
                    //String text = newAdd.toLowerCase(Locale.getDefault());
                    if (!newAdd.isEmpty()){
                        // проверим, содержится ли в БД
                        if (!fDBMethods.hasDBClinic(newAdd)) {
                            // добавим введенное слово в БД
                            fDBMethods.insertClinic(newAdd, "");
                            mClinicsID = fDBMethods.getIDClinicsByName(newAdd);
                        }
                    }
                    // передаем фокус и скрываем клавиатуру
                    editVisitsDoctor.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editVisitsDoctor.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });
        editVisitsClinic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   // выбор из выпадающего списка
                TextView tvClinicID = (TextView) view.findViewById(R.id.lrdd_adapter_id);
                mClinicsID = Integer.parseInt(tvClinicID.getText().toString());
                // передаем фокус и скрываем клавиатуру
                editVisitsDoctor.requestFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editVisitsDoctor.getWindowToken(), 0);
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
                    //String text = s.toString().toLowerCase(Locale.getDefault());
                    cursorClinics = fDBMethods.getFilteredClinics(s.toString());
                    editVisitsClinicsAdapter.swapCursor(cursorClinics);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                //if (loadedFromClinics) return;
            }
        };
        editVisitsClinic.addTextChangedListener(watcherClinic);

        //////////////////////////////////////////////
        // doctors
        cursorDoctors = fDBMethods.getFilteredDoctors("");  // полная таблица
        String[] fromDoctors = {DBMethods.TableDoctors._ID, DBMethods.TableDoctors.COLUMN_DOCTORS_NAME};
        int[] toDoctors = {R.id.lrdd_adapter_id, R.id.lrdd_adapter_name};
        editVisitsDoctorsAdapter = new SimpleCursorAdapter(getContext(), R.layout.list_row_dropdown, cursorDoctors, fromDoctors, toDoctors, 0); // новый адаптер
        editVisitsDoctorsAdapter.setStringConversionColumn(cursorDoctors.getColumnIndexOrThrow("name"));  // чтобы привыборе из выпадающего списка подставлялось значение, а не козябры
        editVisitsDoctor.setAdapter(editVisitsDoctorsAdapter);       // установка адаптера полю ввода
        editVisitsDoctor.setOnEditorActionListener(new TextView.OnEditorActionListener() {   // слушаем нажатие "Далее" у поля ввода
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {         // обработка нажатия Enter
                    String newAdd = editVisitsDoctor.getText().toString();
                    //String text = newAdd.toLowerCase(Locale.getDefault());
                    if (!newAdd.isEmpty()) {
                        // проверим, содержится ли в БД
                        if (!fDBMethods.hasDBDoctor(newAdd)) {
                            // добавим введенное слово в БД
                            fDBMethods.insertDoctor(newAdd, 0);
                            mDoctorsID = fDBMethods.getIDDoctorsByName(newAdd);
                        }
                    }
                    // установим специализацию доктора
                    editVisitsSpecialization.setText(fDBMethods.getDoctorsSpecializationByName(editVisitsDoctor.getText().toString()));

                    // передаем фокус и скрываем клавиатуру
                    if (editVisitsSpecialization.getText().toString().isEmpty()){
                        editVisitsSpecialization.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editVisitsSpecialization.getWindowToken(), 0);
                    }else {
                        editVisitsComment.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editVisitsComment.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });
        editVisitsDoctor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   // выбор из выпадающего списка
                TextView tvDoctorID = (TextView) view.findViewById(R.id.lrdd_adapter_id);
                mDoctorsID = Integer.parseInt(tvDoctorID.getText().toString());
                // установим специализацию доктора
                mSpecializationsID = fDBMethods.getDoctorsSpecializationIDByDoctorsID(mDoctorsID);
                editVisitsSpecialization.setText(fDBMethods.getDoctorsSpecializationByName(editVisitsDoctor.getText().toString()));
                // передаем фокус и скрываем клавиатуру
                if (editVisitsSpecialization.getText().toString().isEmpty()){
                    editVisitsSpecialization.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editVisitsSpecialization.getWindowToken(), 0);
                }else {
                    editVisitsComment.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editVisitsComment.getWindowToken(), 0);
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
                    //String text = s.toString().toLowerCase(Locale.getDefault());
                    cursorDoctors = fDBMethods.getFilteredDoctors(s.toString());
                    editVisitsDoctorsAdapter.swapCursor(cursorDoctors);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                //if (loadedFromDoctors) return;
            }
        };
        editVisitsDoctor.addTextChangedListener(watcherDoctors);

        //////////////////////////////////////////////
        // specializations
        cursorSpecializations = fDBMethods.getFilteredSpecialisations("");  // полная таблица
        String[] fromSpecializations = {DBMethods.TableSpecializations._ID, DBMethods.TableSpecializations.COLUMN_SPECIALIZATIONS_NAME};
        int[] toSpecializations = {R.id.lrdd_adapter_id, R.id.lrdd_adapter_name};
        editVisitsSpecializationsAdapter = new SimpleCursorAdapter(getContext(), R.layout.list_row_dropdown, cursorSpecializations, fromSpecializations, toSpecializations, 0); // новый адаптер
        editVisitsSpecializationsAdapter.setStringConversionColumn(cursorSpecializations.getColumnIndexOrThrow("name"));  // чтобы привыборе из выпадающего списка подставлялось значение, а не козябры
        editVisitsSpecialization.setAdapter(editVisitsSpecializationsAdapter);       // установка адаптера полю ввода
        editVisitsSpecialization.setOnEditorActionListener(new TextView.OnEditorActionListener() {   // слушаем нажатие "Далее" у поля ввода
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {         // обработка нажатия Enter
                    String newAdd = editVisitsSpecialization.getText().toString();
                    //String text = newAdd.toLowerCase(Locale.getDefault());
                    if (!newAdd.isEmpty()){
                        // проверим, содержится ли в БД
                        if (!fDBMethods.hasDBSpecialization(newAdd)) {
                            // добавим введенное слово в БД
                            fDBMethods.insertSpecialization(newAdd);
                            mSpecializationsID = fDBMethods.getIDSpecializationsByName(newAdd);
                        }
                    }
                    // передаем фокус и скрываем клавиатуру
                    editVisitsDiagnosis.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editVisitsDiagnosis.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        editVisitsSpecialization.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   // выбор из выпадающего списка
                TextView tvSpecializationsID = (TextView) view.findViewById(R.id.lrdd_adapter_id);
                mSpecializationsID = Integer.parseInt(tvSpecializationsID.getText().toString());
                // передаем фокус и скрываем клавиатуру
                editVisitsDiagnosis.requestFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editVisitsDiagnosis.getWindowToken(), 0);
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
                    if (!fDBMethods.hasDBSpecialization(text)) {
                        editVisitsSpecializationsAdapter.swapCursor(cursorSpecializations);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        };
        editVisitsSpecialization.addTextChangedListener(watcherSpecialization);

        //////////////////////////////////////////////
        // diagnosis
        cursorDiagnoses = fDBMethods.getFilteredDiagnoses("");  // полная таблица
        String[] fromDiagnoses = {DBMethods.TableDiagnoses._ID, DBMethods.TableDiagnoses.COLUMN_DIAGNOSES_NAME};
        int[] toDiagnoses = {R.id.lrdd_adapter_id, R.id.lrdd_adapter_name};
        editVisitsDiagnosesAdapter = new SimpleCursorAdapter(getContext(), R.layout.list_row_dropdown, cursorDiagnoses, fromDiagnoses, toDiagnoses, 0); // новый адаптер
        editVisitsDiagnosesAdapter.setStringConversionColumn(cursorDiagnoses.getColumnIndexOrThrow("name"));  // чтобы привыборе из выпадающего списка подставлялось значение, а не козябры
        editVisitsDiagnosis.setAdapter(editVisitsDiagnosesAdapter);       // установка адаптера полю ввода
        editVisitsDiagnosis.setOnEditorActionListener(new TextView.OnEditorActionListener() {   // слушаем нажатие "Далее" у поля ввода
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {         // обработка нажатия Enter
                    String newAdd = editVisitsDiagnosis.getText().toString();
                    //String text = newAdd.toLowerCase(Locale.getDefault());
                    if (!newAdd.isEmpty()){
                        // проверим, содержится ли в БД
                        if (!fDBMethods.hasDBDiagnoses(newAdd)) {
                            // добавим введенное слово в БД
                            fDBMethods.insertDiagnoses(newAdd);
                            mDiagnosisID = fDBMethods.getIDDiagnosesByName(newAdd);
                        }
                    }
                    // передаем фокус и скрываем клавиатуру
                    editVisitsComment.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editVisitsComment.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        editVisitsDiagnosis.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   // выбор из выпадающего списка
                // передаем фокус и скрываем клавиатуру
                TextView tvDiagnosisID = (TextView) view.findViewById(R.id.lrdd_adapter_id);
                mDiagnosisID = Integer.parseInt(tvDiagnosisID.getText().toString());
                editVisitsComment.requestFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editVisitsComment.getWindowToken(), 0);
            }
        });
        TextWatcher watcherDiagnosis = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                //if (loadedFromDiagnoses) return;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
                //if (loadedFromDiagnoses) return;
                if (s.length() <= start) {
                    //String text = s.toString().toLowerCase(Locale.getDefault());
                    cursorDiagnoses = fDBMethods.getFilteredDiagnoses(s.toString());
                    editVisitsDiagnosesAdapter.swapCursor(cursorDiagnoses);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                //if (loadedFromDiagnoses) return;
            }
        };
        editVisitsDiagnosis.addTextChangedListener(watcherDiagnosis);

    }

    public interface OnGetSaveVisitCommand{
        int onGetSaveVisitCommand();
    }

    public interface OnGetVisitsID{
        int onGetVisitsID();
    }


}
