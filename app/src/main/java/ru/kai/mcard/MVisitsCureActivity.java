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

public class MVisitsCureActivity extends AppCompatActivity {

    private DBMethods fDBMethods;

    AutoCompleteTextView editVisitsCure;
    TextView tvVisitsCuresFeature;
    EditText editVisitsCuresDescr;

    RelativeLayout rlVisitsCuresContainer;
    Toolbar mToolBar;

    int gettedID; // visitCuresID
    int mCuresID = -1;
    String mCuresName;
    String mCuresFeature;
    String mVisitsCuresDescr;

    // для watcherSpecialization
    SimpleCursorAdapter editVisitsCuresAdapter;
    Cursor cursorCures;

    int currentCuresListPosition = -1; // транслируем через себя позицию адаптера
    int basicVisitsID = -1;     // // транслируем через себя ID визита основания

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.m_activity_visits_cure);

        mToolBar = (Toolbar) findViewById(R.id.mVisitsCuresToolbar);
        setSupportActionBar(mToolBar);

        rlVisitsCuresContainer = (RelativeLayout) findViewById(R.id.rlVisitsCuresContainer);

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        editVisitsCure = (AutoCompleteTextView)findViewById(R.id.editVisitsCure);
        tvVisitsCuresFeature = (TextView)findViewById(R.id.tvVisitsCuresFeature);
        editVisitsCuresDescr = (EditText)findViewById(R.id.editVisitsCuresDescr);

        Bundle bundle = getIntent().getBundleExtra("visitsCuresModel");
        if (bundle!=null)currentCuresListPosition = bundle.getInt("currentCuresListPosition", -1);
        // установим значения, если визит открыт для изменения (откроем из БД по ID):
        if (currentCuresListPosition != -1){
            if (bundle != null) {
                basicVisitsID = bundle.getInt("basicVisitsID", -1);
                mCuresID = bundle.getInt("visitsCuresID", -1);
                mVisitsCuresDescr = bundle.getString("visitsCuresDescr", "");
            }

            Bundle bundleC = fDBMethods.getCuresFildsByCuresID(mCuresID);
            mCuresName = bundleC.getString("curesName");
            mCuresFeature = bundleC.getString("curesFeature");

            editVisitsCure.setText(mCuresName);
            setCuresFeature();
            editVisitsCuresDescr.setText(mVisitsCuresDescr);
        }else {
            tvVisitsCuresFeature.setVisibility(View.GONE);
        }


        ////////////////////////////////////////////////////////////////////////////////////////////
        // предзагрузка списка лекарств для организации поиска
        cursorCures = fDBMethods.getFilteredCures("");  // полная таблица
        String[]fromCures = {DBMethods.TableCures.COLUMN_CURES_NAME};
        int[]toCures = {R.id.lrdd_adapter_name};
        editVisitsCuresAdapter = new SimpleCursorAdapter(MVisitsCureActivity.this, R.layout.list_row_dropdown, cursorCures, fromCures, toCures, 0); // новый адаптер
        editVisitsCuresAdapter.setStringConversionColumn(cursorCures.getColumnIndexOrThrow("name"));  // чтобы привыборе из выпадающего списка подставлялось значение, а не козябры
        editVisitsCure.setAdapter(editVisitsCuresAdapter);       // установка адаптера полю ввода
        editVisitsCure.setOnEditorActionListener(new TextView.OnEditorActionListener() {   // слушаем нажатие "Далее" у поля ввода
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {         // обработка нажатия Enter
                    String newAdd = editVisitsCure.getText().toString();
                    String text = newAdd.toLowerCase(Locale.getDefault());
                    if (!newAdd.isEmpty()){
                        // проверим, содержится ли в БД
                        if (fDBMethods.hasDBCure(text) == false) {
                            // добавим введенное слово в БД
                            fDBMethods.insertCure(newAdd, "");
                        }
                    }
                    // передаем фокус и скрываем клавиатуру
                    editVisitsCuresDescr.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editVisitsCuresDescr.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        editVisitsCure.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   // выбор из выпадающего списка
                mCuresID = fDBMethods.getIDCuresByName(editVisitsCure.getText().toString());
                mCuresFeature = fDBMethods.getCuresDescrByID(mCuresID);
                setCuresFeature();

                // передаем фокус и скрываем клавиатуру
                editVisitsCuresDescr.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editVisitsCuresDescr.getWindowToken(), 0);
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
                    editVisitsCuresAdapter.swapCursor(cursorCures);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                //if (loadedFromCures)return;
            }
        };
        editVisitsCure.addTextChangedListener(watcherCures);
        ////////////////////////////////////////////////////////////////////////////////////////////
        if (!editVisitsCure.getText().toString().isEmpty()){
            editVisitsCuresDescr.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editVisitsCuresDescr.getWindowToken(), 0);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fDBMethods != null) {
            fDBMethods.close();
        }
        if (cursorCures!=null)
            cursorCures.close();
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
                visitsCuresSave(); // сохраним с закрытием
                break;
            case R.id.common_img_Close:
                visitsCuresClose();
                break;
        }

        return true;
    }

    private void setCuresFeature(){
        if (mCuresFeature == null){
            tvVisitsCuresFeature.setVisibility(View.GONE);
        }else {
            if(mCuresFeature.isEmpty()){
                tvVisitsCuresFeature.setVisibility(View.GONE);
            }else {
                tvVisitsCuresFeature.setText(mCuresFeature);
                tvVisitsCuresFeature.setVisibility(View.VISIBLE);
            }
        }

    }

    public void btnVisitsCureOpen(View view) {
        Intent intent = new Intent(MVisitsCureActivity.this, MCuresActivity.class);
        intent.putExtra("choosen_cures_id", mCuresID);
        startActivityForResult(intent, Constants.CURES_DATA);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            mCuresID = data.getIntExtra("choosen_cures_id", -1);
            editVisitsCure.setText(fDBMethods.getCuresNameByID(mCuresID));
            mCuresFeature = fDBMethods.getCuresDescrByID(mCuresID);
            setCuresFeature();
            // передаем фокус и скрываем клавиатуру
            editVisitsCuresDescr.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editVisitsCuresDescr.getWindowToken(), 0);

        }
    }

    public void visitsCuresClose() {
        Intent intent = new Intent();
        //intent.putExtra("currentItemID", gettedID); // для правильного позиционирования в списке
        //intent.putExtra("choosen_visits_cure_data", gettedID); // для правильного позиционирования в списке
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void visitsCuresSave() {
        if (editVisitsCure.getText().toString().isEmpty()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mToolBar.getWindowToken(), 0);
            Snackbar snackbar = Common.getCustomSnackbar(MVisitsCureActivity.this, rlVisitsCuresContainer, getResources().getString(R.string.visits_cures_save_error));
            snackbar.show();
            return;
        }

        // сохраним в списке специализаций также введенную специализацию, если ее там еще нет...
        // ... раз юзер хочет все сохранить
        String newAdd = editVisitsCure.getText().toString();
        String text = newAdd.toLowerCase(Locale.getDefault());
        // проверим, содержится ли в БД
        if (fDBMethods.hasDBCure(text) == false) {
            // добавим введенное слово в БД
            mCuresID = fDBMethods.insertCure(newAdd, "");
            mCuresFeature = "";
        }

        // назначение, выданное на визите, принадлежит визиту, и записывать его мы будем в визите, а пока только передадим в визит данные:

        // если назначение только что записали, то у нас есть только его наименование; получим его id:
        if (mCuresID == -1){
            mCuresID = fDBMethods.getIDCuresByName(editVisitsCure.getText().toString().trim());
        }
        Bundle bundle = new Bundle();

        bundle.putInt("currentCuresListPosition", currentCuresListPosition);
        bundle.putInt("basicVisitsID", basicVisitsID);
        bundle.putInt("visitsCuresID", mCuresID); // id типа самого назначения (наименование нужно будет получить по id)
        bundle.putString("visitsCuresDescr", editVisitsCuresDescr.getText().toString().trim());


        Intent intent = new Intent();
        //intent.putExtra("currentItemID", gettedID);
        intent.putExtra("choosen_visits_cure_data", bundle);
        setResult(RESULT_OK, intent);
        finish();
    }






}
