package ru.kai.mcard;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import ru.kai.mcard.utility.Common;

public class MProfileEditActivity extends AppCompatActivity {

    EditText editProfileEditName;
    TextView tvProfileEditBirthdayDate;
    RadioGroup rgProfileEditSex;
    EditText editProfileEditComment;
    RadioButton rbProfileEditSexM, rbProfileEditSexW;

    private DBMethods fDBMethods;

    Calendar vDateAndTime;

    int mProfilesID;
    String mProfilesSex;
    Long mProfileEditBirthdayDate = 0L;

    Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.m_activity_profile_edit);

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        mToolBar = (Toolbar) findViewById(R.id.mProfileEditToolbar);
        setSupportActionBar(mToolBar);

        editProfileEditName = (EditText) findViewById(R.id.editProfileEditName);
        rgProfileEditSex = (RadioGroup) findViewById(R.id.rgProfileEditSex);
        tvProfileEditBirthdayDate = (TextView)findViewById(R.id.tvProfileEditBirthdayDate);
        editProfileEditComment = (EditText)findViewById(R.id.editProfileEditComment);
        rbProfileEditSexM = (RadioButton)findViewById(R.id.rbProfileEditSexM);
        rbProfileEditSexW = (RadioButton)findViewById(R.id.rbProfileEditSexW);

        vDateAndTime = Calendar.getInstance();

        mProfilesID = getIntent().getIntExtra("given_profiles_id", -1);
        if (mProfilesID > 0){
            Bundle bundle = fDBMethods.getProfilesFildsByProfilesID(mProfilesID);
            editProfileEditName.setText(bundle.getString("profilesName"));

            mProfilesSex = bundle.getString("profilesSex");
            if (mProfilesSex != null){
                if (mProfilesSex.equals("m")){
                    rbProfileEditSexM.setChecked(true);
                    rbProfileEditSexW.setChecked(false);
                }
                if (mProfilesSex.equals("w")){
                    rbProfileEditSexM.setChecked(false);
                    rbProfileEditSexW.setChecked(true);
                }
            }

            mProfileEditBirthdayDate = bundle.getLong("profilesBirthday");
            setBirthdayDate();

            editProfileEditComment.setText(bundle.getString("profilesComment"));
        }

        if (mProfileEditBirthdayDate == 0L){
            mProfileEditBirthdayDate = 329920870046L;
            setBirthdayDate();
        }

        rgProfileEditSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                switch (checkedId) {
                    case -1:
                        //Toast.makeText(getApplicationContext(), "No choice", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.rbProfileEditSexM:
                        mProfilesSex = "m";
                        break;
                    case R.id.rbProfileEditSexW:
                        mProfilesSex = "w";
                        break;

                    default:
                        break;
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
                profileEditSave(); // сохраним с закрытием
                break;
            case R.id.common_img_Close:
                profileEditClose();
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

    public void profileEditClose() {
        Intent intent = new Intent();
        intent.putExtra("currentItemID", mProfilesID); // для правильного позиционирования в списке
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void profileEditSave() {
        Bundle bundle = new Bundle();
        bundle.putString("profilesName", editProfileEditName.getText().toString());
        if (rbProfileEditSexM.isChecked())bundle.putString("profilesSex", "m");
        if (rbProfileEditSexW.isChecked())bundle.putString("profilesSex", "w");
        bundle.putLong("profilesBirthday", mProfileEditBirthdayDate);
        bundle.putString("profilesComment", editProfileEditComment.getText().toString());

        if (mProfilesID > 0){
            fDBMethods.updateProfiles(mProfilesID, bundle);
        }else {
            mProfilesID = fDBMethods.insertProfiles(bundle);
        }

        Intent intent = new Intent();
        intent.putExtra("currentItemID", mProfilesID);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void setProfileEditBirthdayDate(View view) {
        Date vDate;
        if (mProfileEditBirthdayDate == 0L) {
            vDate = new Date();
        } else {
            vDate = new Date(mProfileEditBirthdayDate);
        }

        vDateAndTime.setTime(vDate);

        new DatePickerDialog(MProfileEditActivity.this, d,
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

            mProfileEditBirthdayDate = vDateAndTime.getTimeInMillis();
            setBirthdayDate();

        }
    };

    private void setBirthdayDate() {
        if (mProfileEditBirthdayDate == 0L) {
            tvProfileEditBirthdayDate.setText("");
        } else {
            CharSequence date = DateFormat.format("dd.MM.yyyy", mProfileEditBirthdayDate);
            tvProfileEditBirthdayDate.setText(date);

        }
    }

}
