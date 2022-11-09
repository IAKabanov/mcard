package ru.kai.mcard;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.*;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ru.kai.mcard.utility.Common;

public class MClinicsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private DBMethods fDBMethods;

    FloatingActionButton fabClinics;
    LinearLayoutManager llManager;
    RecyclerView rv;
    ClinicsRVAdapter rvClinicsRVAdapter;
    AppBarLayout mClinicsAppbarLayout;
    CollapsingToolbarLayout mClinicsCollapsingToolbarLayout;

    int mCurrentItemID = -1;
    int mCurrentItemPosition = -1;

    private Paint p = new Paint();

    Drawable singleDrawable;

    SharedPreferences mSettings;
    TextView clinicsListsHelp;
    Boolean showClinicsHelp = true;
    Boolean canChoosenItems = true; // если список открыт из NavigationDrawer-а то при выборе item-а ничего делать не будем. Закрывают пусть через "назад"

    // Google analytics (GA)
    private Tracker mTracker; // еще в onCreate и onResume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // настройки
        mSettings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.m_activity_clinics);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA

        mClinicsAppbarLayout = (AppBarLayout)findViewById(R.id.mClinicsAppbarLayout);

        mClinicsCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.mClinicsCollapsingToolbarLayout);
        mClinicsCollapsingToolbarLayout.setTitle(getResources().getString(R.string.cliniks_title_activity));
        mClinicsCollapsingToolbarLayout.setContentScrim(Common.getCurrentContentScrimDawable(MClinicsActivity.this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.mClinicsToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        rv = (RecyclerView)findViewById(R.id.mClinicsRecyclerView);
        if (rv != null) {
            rv.setHasFixedSize(true);
        }

        llManager = new LinearLayoutManager(this);
        rv.setLayoutManager(llManager);

        int gettedID = getIntent().getIntExtra("choosen_clinic_id", -1);
        if (gettedID != -1){
            mCurrentItemID = gettedID;
        }
        canChoosenItems = getIntent().getBooleanExtra("can_choosen_items", true);

        // создаем лоадер для чтения данных
        getLoaderManager().initLoader(0, null, this);

        rvClinicsRVAdapter = new ClinicsRVAdapter(null);
        rv.setAdapter(rvClinicsRVAdapter);

        initSwipe();

        fabClinics = (FloatingActionButton) findViewById(R.id.fabClinics);
        fabClinics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Add")
                        .setAction("Add клинику.")
                        .build());

                Intent intent = new Intent(MClinicsActivity.this, MClinicActivity.class);
                startActivityForResult(intent, Constants.CLINIC_DATA);
            }
        });

        // обычные item-ы (для выделения)
        singleDrawable = Common.getOrdinaryItemsDawable(MClinicsActivity.this);

        // не показывать обучающую информацию -->
        clinicsListsHelp = (TextView)findViewById(R.id.clinicsListsHelp);

        if (mSettings.contains(Constants.APP_PREFERENCES_SHOW_CLINICS_HELP_B)) {
            // Получаем число из настроек
            showClinicsHelp = mSettings.getBoolean(Constants.APP_PREFERENCES_SHOW_CLINICS_HELP_B, true);
            if (showClinicsHelp == false)clinicsListsHelp.setVisibility(View.GONE);
        }

        clinicsListsHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = getResources().getString(R.string.do_not_show_help_info);
                //String message = getResources().getString(R.string.do_not_show_help_info);
                String btnPositiveString = getResources().getString(R.string.but_continue_help);
                String btnNegativeString = getResources().getString(R.string.but_cancel_help);

                AlertDialog.Builder ad = new AlertDialog.Builder(MClinicsActivity.this);
                ad.setTitle(title);  // заголовок
                //ad.setMessage(message); // сообщение
                ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {

                        // Google analytics (GA)
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Action help")
                                .setAction("help clinic отключить.")
                                .build());

                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putBoolean(Constants.APP_PREFERENCES_SHOW_CLINICS_HELP_B, false);
                        editor.apply();
                        clinicsListsHelp.setVisibility(View.GONE);
                    }
                });
                ad.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        // Google analytics (GA)
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Action help")
                                .setAction("help clinic пусть будет пока.")
                                .build());
                    }
                });
                ad.show();
            }
        });
        // <--

    }

    @Override
    protected void onResume() {
        super.onResume();
        // обновим (перезагрузим) loader
        getLoaderManager().restartLoader(0, null, this);

        // Google analytics (GA)
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "Список клиник (MClinicsActivity)");
        mTracker.setScreenName("Список клиник (MClinicsActivity)");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fDBMethods != null) {
            fDBMethods.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CLINIC_DATA) {
            if (resultCode == RESULT_OK) {
                mCurrentItemID = data.getIntExtra("currentItemID", -1);
            }
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    public class ClinicsRVAdapter extends RecyclerView.Adapter<ClinicsRVAdapter.ClinicsListViewHolders> {

        Cursor dataCursor;

        ClinicsRVAdapter(Cursor cursor) {
            this.dataCursor = cursor;
        }

        @Override
        public ClinicsListViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_common_dubble, null);
            ClinicsListViewHolders analisesTypesListVH = new ClinicsListViewHolders(v);

            return analisesTypesListVH;
        }

        public Cursor swapCursor(Cursor cursor) {
            if (dataCursor == cursor) {
                return null;
            }
            Cursor oldCursor = dataCursor;
            this.dataCursor = cursor;
            if (cursor != null) {
                this.notifyDataSetChanged();
            }
            return oldCursor;
        }
        @Override
        public void onBindViewHolder(ClinicsListViewHolders holder, int position) {

            dataCursor.moveToPosition(position);

            int clinicsID = dataCursor.getInt(dataCursor.getColumnIndex(DBMethods.TableClinics._ID));
            String clinicsName = dataCursor.getString(dataCursor.getColumnIndex(DBMethods.TableClinics.COLUMN_CLINICS_NAME));
            String clinicsDescr = dataCursor.getString(dataCursor.getColumnIndex(DBMethods.TableClinics.COLUMN_CLINICS_DESCR));

            holder.tvDubbleCommonCardViewTitle.setText(clinicsName);
            holder.tvDubbleCommonCardViewDescr.setText(clinicsDescr);

            holder.clinicsID = clinicsID;

            if (clinicsID == mCurrentItemID){
                holder.llDubbleCommonCardView.setBackground(Common.getChoosenItemsDrawable(MClinicsActivity.this));
            }else {
                holder.llDubbleCommonCardView.setBackground(singleDrawable);
            }
        }

        @Override
        public int getItemCount() {
            return (dataCursor == null) ? 0 : dataCursor.getCount();
        }

        public class ClinicsListViewHolders extends RecyclerView.ViewHolder {

            LinearLayout llDubbleCommonCardView; // для выделения цветом
            TextView tvDubbleCommonCardViewTitle;
            TextView tvDubbleCommonCardViewDescr;
            int clinicsID;

            public ClinicsListViewHolders(final View itemView) {
                super(itemView);
                llDubbleCommonCardView = (LinearLayout) itemView.findViewById(R.id.llDubbleCommonCardView);
                tvDubbleCommonCardViewTitle = (TextView) itemView.findViewById(R.id.tvDubbleCommonCardViewTitle);
                tvDubbleCommonCardViewDescr = (TextView) itemView.findViewById(R.id.tvDubbleCommonCardViewDescr);

                llDubbleCommonCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //itemView.setSelected(true);
                        if (canChoosenItems) {
                            // отправляем ID выбранной позиции вида исследования
                            Intent intent = new Intent();
                            intent.putExtra("choosen_clinic_id", clinicsID);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                });

            }

        }
    }

    private void initSwipe(){

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT){
                    // удалить
                    final int currentClinicsID = ((ClinicsRVAdapter.ClinicsListViewHolders) viewHolder).clinicsID;
                    mCurrentItemID = currentClinicsID;
                    String title = getResources().getString(R.string.deleting_visit_question_mini);
                    //String message = getResources().getString(R.string.do_not_show_help_info);
                    String btnPositiveString = getResources().getString(R.string.dialog_positive_button_text);
                    String btnNegativeString = getResources().getString(R.string.dialog_negative_button_text);

                    AlertDialog.Builder ad = new AlertDialog.Builder(MClinicsActivity.this);
                    ad.setTitle(title);  // заголовок
                    //ad.setMessage(message); // сообщение
                    ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            if (fDBMethods.checkOfUsageClinic(currentClinicsID) == true){
                                // вид анализа используется где-то. Сообщим об этом:
                                Snackbar snackbar = Common.getCustomSnackbar(MClinicsActivity.this, rv, getResources().getString(R.string.cliniks_must_not_delete));
                                snackbar.show();
                            }else {
                                fDBMethods.deleteClinic(currentClinicsID);
                            }
                            getLoaderManager().restartLoader(0, null, MClinicsActivity.this);
                            //getLoaderManager().getLoader(0).forceLoad();
                            //rvAnalisesTypesRVAdapter.notifyDataSetChanged();
                        }
                    });
                    ad.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            getLoaderManager().restartLoader(0, null, MClinicsActivity.this);
                            //rvAnalisesTypesRVAdapter.notifyDataSetChanged();
                            //setResult(RESULT_CANCELED);
                            //finish();
                        }
                    });
                    ad.setCancelable(false);
                    ad.show();

                } else {
                    // изменить
                    final int currentClinicsID = ((ClinicsRVAdapter.ClinicsListViewHolders) viewHolder).clinicsID;
                    mCurrentItemID = currentClinicsID;
                    Intent intent = new Intent(MClinicsActivity.this, MClinicActivity.class);
                    intent.putExtra("choosen_clinic_id", currentClinicsID);
                    startActivityForResult(intent, Constants.CLINIC_DATA);

                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                //int alternativeThemeColor = Common.getAlternativeThemeColor(getApplicationContext());

                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 1;

                    if(dX > 0){
                        // edit
                        p.setColor(Color.parseColor("#20ab1e"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.btn_edit_black);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + 0*width ,(float) itemView.getBottom() - width,(float) itemView.getLeft()+ 1*width,(float)itemView.getTop() + width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    } else {
                        // delete
                        p.setColor(Color.parseColor("#FF4081"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.btn_delete_black);
                        RectF icon_dest = new RectF((float) ((float) itemView.getRight() - 1*width),(float) itemView.getBottom() - width, (float) ((float) itemView.getRight() - 0*width),(float)itemView.getTop() + width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rv);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new ClinicsCursorLoader(this, fDBMethods);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // код для определения позиции адаптера, соответствующей текущему clinicsID (это нужно при добавлении новой позиции в список)
        if (mCurrentItemID != -1){
            if (data.getCount()>0){
                data.moveToFirst();
                do {
                    int clinicsID = data.getInt(data.getColumnIndex(DBMethods.TableClinics._ID));
                    if (clinicsID == mCurrentItemID){
                        mCurrentItemPosition = data.getPosition();
                        break;
                    }
                }while (data.moveToNext());
            }
        }

        rvClinicsRVAdapter.swapCursor(data);

        if (mCurrentItemPosition != -1){
            mClinicsAppbarLayout.setExpanded(false);
            rv.smoothScrollToPosition(mCurrentItemPosition +7);
        }else {
            rv.smoothScrollToPosition(0);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        rvClinicsRVAdapter.swapCursor(null);
    }

    static class ClinicsCursorLoader extends CursorLoader {
        DBMethods scldbMethods;

        public ClinicsCursorLoader(Context context, DBMethods dbMethods) {
            super(context);
            this.scldbMethods = dbMethods;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = scldbMethods.getAllClinics();
            return cursor;
        }
    }





}
