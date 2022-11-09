package ru.kai.mcard;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class MAnalisisVisitMainFragment extends Fragment implements View.OnClickListener{

    TextView tvAnalisisVisitsMDate;
    TextView tvAnalisisVisitsMTime;
    AutoCompleteTextView editAnalisisVisitsMClinic;
    EditText editAnalisisVisitsMComment;
    Button butOpenAnalisisMClinics;
    Button butshowActivityVisitMPhotoTest;
    Button butshowAnalisisVisitMPhoto;

    int gettingID; // для идентификации визита и update или delete
    Calendar vDateAndTime;

    private DBMethods fDBMethods;

    // для watcherClinic
    SimpleCursorAdapter editAnalisisVisitsClinicsAdapter;
    Cursor cursorClinics;

    int mClinicsID;
    Long mAnalisisVisitsMDateTime = 0L;
    String mComment;
    int mCurrentProfileID;

    MAnalisisVisitActivity activity;

    public MAnalisisVisitMainFragment() {
        // Required empty public constructor
    }

    public static MAnalisisVisitMainFragment newInstance(Bundle bundle) {
        MAnalisisVisitMainFragment currentFragment = new MAnalisisVisitMainFragment();
        Bundle args = new Bundle();
        args.putBundle("gettedArgs", bundle);
        currentFragment.setArguments(args);
        return currentFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_analisis_visit_m_main, container, false);

        activity = (MAnalisisVisitActivity)getActivity();

        tvAnalisisVisitsMDate = (TextView)rootView.findViewById(R.id.tvAnalisisVisitsMDate);
        tvAnalisisVisitsMTime = (TextView)rootView.findViewById(R.id.tvAnalisisVisitsMTime);
        editAnalisisVisitsMClinic = (AutoCompleteTextView)rootView.findViewById(R.id.editAnalisisVisitsMClinic);
        editAnalisisVisitsMComment = (EditText)rootView.findViewById(R.id.editAnalisisVisitsMComment);
        butOpenAnalisisMClinics = (Button) rootView.findViewById(R.id.butOpenAnalisisMClinics);
        butshowAnalisisVisitMPhoto = (Button) rootView.findViewById(R.id.butshowActivityVisitMPhoto);
        butshowActivityVisitMPhotoTest = (Button) rootView.findViewById(R.id.butshowActivityVisitMPhotoTest);

        vDateAndTime = Calendar.getInstance();

        fDBMethods = new DBMethods(getContext());
        fDBMethods.open();

        // получаем из активности
        Bundle bundle = getArguments().getBundle("gettedArgs");
        if (bundle != null) {
            gettingID = bundle.getInt("gettingID", 0);
            mClinicsID = bundle.getInt("mClinicsID", 0);
            mCurrentProfileID = bundle.getInt("currentProfileID", -1);
            mAnalisisVisitsMDateTime = bundle.getLong("mAnalisisVisitsMDateTime", 0L);
            mComment = bundle.getString("mComment");
        }else {
            gettingID = 0;
            mClinicsID = 0;
            mCurrentProfileID = -1;
            mAnalisisVisitsMDateTime = 0L;
            mComment = "";
        }
        setAnalisisVisitsDate();
        setAnalisisVisitsTime();
        setClinicsName();
        editAnalisisVisitsMComment.setText(mComment);

        prepareInitializationsLists();

        butOpenAnalisisMClinics.setOnClickListener(this);
        tvAnalisisVisitsMDate.setOnClickListener(this);
        tvAnalisisVisitsMTime.setOnClickListener(this);
        butshowAnalisisVisitMPhoto.setOnClickListener(this);
//        butshowActivityVisitMPhotoTest.setOnClickListener(this);  // временно, чтобы случайно не срабатывала в продуктиве

        // чтобы при открытии не выпадал список клиник
        if (!editAnalisisVisitsMClinic.getText().toString().isEmpty()){
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editAnalisisVisitsMComment.getWindowToken(), 0);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // чтобы при повороте не выпадал список клиник
        if (!editAnalisisVisitsMClinic.getText().toString().isEmpty()){
            editAnalisisVisitsMClinic.clearFocus();
            //editAnalisisVisitsMComment.requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editAnalisisVisitsMComment.getWindowToken(), 0);
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fDBMethods != null) {
            fDBMethods.close();
        }
        if (cursorClinics != null)
            cursorClinics.close();
    }

    @Override  // получаем ответ от открытых ранее активностей
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case Constants.ANALISIS_VISIT_CLINICS_DATA:
                // если выбрали клинику
                if (resultCode == activity.RESULT_OK) {
                    mClinicsID = data.getIntExtra("choosen_clinic_id", 0);
                }
                setClinicsName(); // может так случиться, что название клиники изменили, но нажали "Закрыть", а мы все-таки отобразим правильнок имя

/*
                if (editAnalisisVisitsAnalisisType.getText().toString().isEmpty()){
                    editAnalisisVisitsAnalisisType.requestFocus();
                }else editAnalisisVisitFocus.requestFocus();
*/

                break;
            case 1233:
                onOtherFileChoose(data.getData());
                break;
        }

    }

    private void setAnalisisVisitsDate() {
        if (mAnalisisVisitsMDateTime == 0L) {
            tvAnalisisVisitsMDate.setText("");
        } else {
            CharSequence date = DateFormat.format("dd.MM.yy", mAnalisisVisitsMDateTime);
            tvAnalisisVisitsMDate.setText(date);
            // установим также и текущее время, если оно еще не установлено
            if (tvAnalisisVisitsMTime.getText().toString().isEmpty()) setAnalisisVisitsTime();
        }
    }

    private void setAnalisisVisitsTime() {
        if (mAnalisisVisitsMDateTime == 0L) {
            tvAnalisisVisitsMTime.setText("");
        } else {
            //CharSequence date = DateFormat.format("dd.MM.yy", mVisitsTime);
            CharSequence date = DateUtils.formatDateTime(getContext(), mAnalisisVisitsMDateTime, DateUtils.FORMAT_SHOW_TIME);
            tvAnalisisVisitsMTime.setText(date);
            // установим также и текущую дату, если она еще не установлена
            if (tvAnalisisVisitsMDate.getText().toString().isEmpty()) setAnalisisVisitsDate();
        }
    }

    private void setClinicsName() {
        if (mClinicsID == 0) {
            editAnalisisVisitsMClinic.setText("");
        } else editAnalisisVisitsMClinic.setText(fDBMethods.getClinicsNameByID(mClinicsID));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvAnalisisVisitsMDate:
                setAnalisisDate();
                break;
            case R.id.tvAnalisisVisitsMTime:
                setAnalisisTime();
                break;
            case R.id.butOpenAnalisisMClinics:
                Intent intent = new Intent(getContext(), MClinicsActivity.class);
                intent.putExtra("choosen_clinic_id", mClinicsID);
                startActivityForResult(intent, Constants.ANALISIS_VISIT_CLINICS_DATA);
                break;
            case R.id.butshowActivityVisitMPhoto:
                avPhotoOpen();
                break;
            case R.id.butshowActivityVisitMPhotoTest:
                // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
                // browser.
                Intent intentC = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                intentC.addCategory(Intent.CATEGORY_OPENABLE);

                // Filter to show only images, using the image MIME data type.
                // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
                // To search for all documents available via installed storage providers,
                // it would be "*/*".
//                intentC.setType("image/*");
//                final int READ_REQUEST_CODE = 42;
//                startActivityForResult(intentC, READ_REQUEST_CODE);

                Intent localIntent = new Intent();
                localIntent.setType("*/*");
                localIntent.setAction("android.intent.action.GET_CONTENT");
                startActivityForResult(localIntent, 1233);

                break;
        }
    }

    public void setAnalisisDate() {
        Date vDate;
        if (mAnalisisVisitsMDateTime == 0L) {
            vDate = new Date();
        } else {
            vDate = new Date(mAnalisisVisitsMDateTime);
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

            mAnalisisVisitsMDateTime = vDateAndTime.getTimeInMillis();
            setAnalisisVisitsDate();

        }
    };

    public void setAnalisisTime() {
        Date vTime;
        if (mAnalisisVisitsMDateTime == 0L) {
            vTime = new Date();
        } else {
            vTime = new Date(mAnalisisVisitsMDateTime);
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

            mAnalisisVisitsMDateTime = vDateAndTime.getTimeInMillis();
            setAnalisisVisitsTime();
        }
    };


    public void avPhotoOpen() {

        if (gettingID > 0){
            Bundle bundle = new Bundle();
            bundle.putInt("typeOfVisit", Constants.DIRECTIONS_ANALISIS_TYPE_1);
            bundle.putInt("basicVisitsID", gettingID);
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
                    gettingID = listener.onGetSaveVisitCommand();
                    if (gettingID == -1){
                        return;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putInt("typeOfVisit", Constants.DIRECTIONS_ANALISIS_TYPE_1);
                    bundle.putInt("basicVisitsID", gettingID);
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
        if (gettingID == 0){
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
            bundle.putInt("typeOfVisit", Constants.DIRECTIONS_ANALISIS_TYPE_1);
            bundle.putInt("basicVisitsID", gettingID);
            bundle.putInt("whyPhotoOpened", Constants.PHOTO_ADD);

            Intent intent = new Intent(getContext(), PhotoScrollingActivity.class);
            intent.putExtra("photosBundle", bundle);
            startActivityForResult(intent, Constants.PHOTO_LIST_OPEN);
        }
*/

    }

    public String[] getFileInfoByUri(Context paramContext1, Uri paramUri)
    {
        String[] arrayOfString = new String[2];
        Cursor paramContextC;
        String paramContextS = "";

        if (paramUri.getScheme().toString().compareTo("content") == 0)
        {
            //paramContext = (Context) paramContext.getContentResolver().query(paramUri, null, null, null, null);
            paramContextC = (Cursor) paramContext1.getContentResolver().query(paramUri, null, null, null, null);
            if (paramContextC.moveToFirst())
            {
                int i = paramContextC.getColumnIndexOrThrow("_display_name");
                int j = paramContextC.getColumnIndexOrThrow("mime_type");
                arrayOfString[0] = paramContextC.getString(i);
                arrayOfString[1] = paramContextC.getString(j);
            }
        }
/*
        do
        {
            do
            {
                do
                {
                    return arrayOfString;
                } while (paramUri.getScheme().toString().compareTo("file") != 0);
                arrayOfString[0] = paramUri.getLastPathSegment();
                paramContextS = arrayOfString[0].toString().toLowerCase();
            } while (paramContextS == null);
            if ((paramContextS.endsWith("jpeg")) || (paramContextS.endsWith("jpg")))
            {
                arrayOfString[1] = "image/jpeg";
                return arrayOfString;
            }
            if (paramContextS.endsWith("pdf"))
            {
                arrayOfString[1] = "application/pdf";
                return arrayOfString;
            }
            if (paramContextS.endsWith("mp3"))
            {
                arrayOfString[1] = "audio/mpeg";
                return arrayOfString;
            }
        } while ((!paramContextS.endsWith("mp4")) && (!paramContextS.endsWith("3gp")));
        arrayOfString[1] = "video/mpeg";
*/

        return arrayOfString;
    }

    public void onOtherFileChoose(Uri paramUri)
    {
        //Object localObject1 = getFileInfoByUri(this.context, paramUri);
        String[] localObject1 = getFileInfoByUri(getActivity(), paramUri);

/*
        if (localObject1[0] != null) {
            try
            {
                File localFile = createFileWithoutExtension(localObject1[0]);
                copyFile(getActivity().getContentResolver().openInputStream(paramUri), new FileOutputStream(localFile));
                paramUri = EventBus.getDefault();
                Object localObject2 = new ViewEvents();
                localObject2.getClass();
                localObject2 = new ViewEvents.Gallery((ViewEvents)localObject2);
                localObject2.getClass();
                paramUri.post(new ViewEvents.Gallery.OnChooseOtherFileFinished((ViewEvents.Gallery)localObject2, localFile, localObject1[1]));
                return;
            }
            catch (IOException paramUri)
            {
                paramUri.printStackTrace();
                paramUri = EventBus.getDefault();
                localObject1 = new ViewEvents();
                localObject1.getClass();
                localObject1 = new ViewEvents.Gallery((ViewEvents)localObject1);
                localObject1.getClass();
                paramUri.post(new ViewEvents.Gallery.OnChooseOtherFileSomeErrorOccurred((ViewEvents.Gallery)localObject1));
                return;
            }
        }
        paramUri = EventBus.getDefault();
        localObject1 = new ViewEvents();
        localObject1.getClass();
        localObject1 = new ViewEvents.Gallery((ViewEvents)localObject1);
        localObject1.getClass();
        paramUri.post(new ViewEvents.Gallery.OnChooseOtherFileSomeErrorOccurred((ViewEvents.Gallery)localObject1));
*/

    }

    // загружаем список для watcherSpecialization
    private void prepareInitializationsLists() {
        // prepare your list of words for AutoComplete
        //////////////////////////////////////////////
        // clinics
        cursorClinics = fDBMethods.getFilteredClinics("");  // полная таблица
        String[] fromClinics = {DBMethods.TableClinics._ID, DBMethods.TableClinics.COLUMN_CLINICS_NAME};
        int[] toClinics = {R.id.lrdd_adapter_id, R.id.lrdd_adapter_name};
        editAnalisisVisitsClinicsAdapter = new SimpleCursorAdapter(getContext(), R.layout.list_row_dropdown, cursorClinics, fromClinics, toClinics, 0); // новый адаптер
        editAnalisisVisitsClinicsAdapter.setStringConversionColumn(cursorClinics.getColumnIndexOrThrow("name"));  // чтобы привыборе из выпадающего списка подставлялось значение, а не козябры
        editAnalisisVisitsMClinic.setAdapter(editAnalisisVisitsClinicsAdapter);       // установка адаптера полю ввода
        editAnalisisVisitsMClinic.setOnEditorActionListener(new TextView.OnEditorActionListener() {   // слушаем нажатие "Далее" у поля ввода
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {         // обработка нажатия Enter
                    String newAdd = editAnalisisVisitsMClinic.getText().toString();
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
                    //editAnalisisVisitsMComment.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editAnalisisVisitsMComment.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });
        editAnalisisVisitsMClinic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   // выбор из выпадающего списка
                // передаем фокус и скрываем клавиатуру
                TextView tvClinicID = (TextView) view.findViewById(R.id.lrdd_adapter_id);
                mClinicsID = Integer.parseInt(tvClinicID.getText().toString());
                //editAnalisisVisitsMComment.requestFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editAnalisisVisitsMComment.getWindowToken(), 0);
            }
        });
        TextWatcher watcherClinic = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() <= start) {
                    cursorClinics = fDBMethods.getFilteredClinics(s.toString());
                    editAnalisisVisitsClinicsAdapter.swapCursor(cursorClinics);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //String text = s.toString().toLowerCase(Locale.getDefault());
            }
        };
        editAnalisisVisitsMClinic.addTextChangedListener(watcherClinic);


    }

    public interface OnGetSaveVisitCommand{
        int onGetSaveVisitCommand();
    }




}
