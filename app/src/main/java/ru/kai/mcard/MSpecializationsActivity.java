package ru.kai.mcard;

import android.animation.LayoutTransition;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
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

public class MSpecializationsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    // lifecycle loader => http://developer.alexanderklimov.ru/android/theory/loader.php
    private DBMethods fDBMethods;

    android.support.design.widget.FloatingActionButton fabSpecializations;
    LinearLayoutManager llManager;
    RecyclerView rv;
    SpecializationsRVAdapter rvSpecializationsRVAdapter;
    AppBarLayout mSpecializationsAppbarLayout;
    CollapsingToolbarLayout mSpecializationsCollapsingToolbarLayout;

    int mCurrentItemID = -1;
    int mCurrentItemPosition = -1;

    private Paint p = new Paint();

    Drawable singleDrawable;

    Boolean canChoosenItems = true; // если список открыт из NavigationDrawer-а то при выборе item-а ничего делать не будем. Закрывают пусть через "назад"

    // Google analytics (GA)
    private Tracker mTracker; // еще в onCreate и onResume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.m_activity_specializations);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA

        mSpecializationsAppbarLayout = (AppBarLayout)findViewById(R.id.mSpecializationsAppbarLayout);

        mSpecializationsCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.mSpecializationsCollapsingToolbarLayout);
        mSpecializationsCollapsingToolbarLayout.setTitle(getResources().getString(R.string.specializations_title_activity));
        mSpecializationsCollapsingToolbarLayout.setContentScrim(Common.getCurrentContentScrimDawable(MSpecializationsActivity.this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.mSpecializationsToolbar);
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

        rv = (RecyclerView)findViewById(R.id.mSpecializationsRecyclerView);
        if (rv != null) {
            rv.setHasFixedSize(true);
        }

        llManager = new LinearLayoutManager(this);
        rv.setLayoutManager(llManager);

        int gettedID = getIntent().getIntExtra("choosen__specializations_id", -1);
        if (gettedID != -1){
            mCurrentItemID = gettedID;
        }
        canChoosenItems = getIntent().getBooleanExtra("can_choosen_items", true);

        // создаем лоадер для чтения данных
        getLoaderManager().initLoader(0, null, this);

        rvSpecializationsRVAdapter = new SpecializationsRVAdapter(null);
        rv.setAdapter(rvSpecializationsRVAdapter);

        initSwipe();

        fabSpecializations = (FloatingActionButton) findViewById(R.id.fabSpecializations);
        fabSpecializations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Add")
                        .setAction("Add специализацию.")
                        .build());

                Intent intent = new Intent(MSpecializationsActivity.this, MSpecializationActivity.class);
                startActivityForResult(intent, Constants.SPECIALIZATION_DATA);
            }
        });

        // обычные item-ы (для выделения)
        singleDrawable = Common.getOrdinaryItemsDawable(MSpecializationsActivity.this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // обновим (перезагрузим) loader
        getLoaderManager().restartLoader(0, null, this);

        // Google analytics (GA)
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "Список специализаций (MSpecializationsActivity)");
        mTracker.setScreenName("Список специализаций (MSpecializationsActivity)");
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
        if (requestCode == Constants.SPECIALIZATION_DATA) {
            if (resultCode == RESULT_OK) {
                mCurrentItemID = data.getIntExtra("currentItemID", -1);
            }
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    public class SpecializationsRVAdapter extends RecyclerView.Adapter<SpecializationsRVAdapter.SpecializationsListViewHolders> {

        Cursor dataCursor;

        SpecializationsRVAdapter(Cursor cursor) {
            this.dataCursor = cursor;
        }

        @Override
        public SpecializationsListViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_common_single, null);
            SpecializationsListViewHolders specializationsListVH = new SpecializationsListViewHolders(v);

            return specializationsListVH;
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
        public void onBindViewHolder(SpecializationsListViewHolders holder, int position) {

            if (!dataCursor.moveToPosition(position)){
                return;
            }
            int specializationsID = dataCursor.getInt(dataCursor.getColumnIndex(DBMethods.TableSpecializations._ID));
            String specializationsName = dataCursor.getString(dataCursor.getColumnIndex(DBMethods.TableSpecializations.COLUMN_SPECIALIZATIONS_NAME));

            holder.tvSimpleCommonCardView.setText(specializationsName);

            holder.specializationsID = specializationsID;

            if (specializationsID == mCurrentItemID){
                holder.llSimpleCommonCardView.setBackground(Common.getChoosenItemsDrawable(MSpecializationsActivity.this));
            }else {
                holder.llSimpleCommonCardView.setBackground(singleDrawable);
            }
        }

        @Override
        public int getItemCount() {
            return (dataCursor == null) ? 0 : dataCursor.getCount();
        }

        public class SpecializationsListViewHolders extends RecyclerView.ViewHolder {

            LinearLayout llSimpleCommonCardView; // для выделения цветом текущего item-а
            TextView tvSimpleCommonCardView;
            int specializationsID;

            public SpecializationsListViewHolders(final View itemView) {
                super(itemView);
                llSimpleCommonCardView = (LinearLayout) itemView.findViewById(R.id.llSimpleCommonCardView);
                tvSimpleCommonCardView = (TextView) itemView.findViewById(R.id.tvSimpleCommonCardView);

                llSimpleCommonCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        itemView.setSelected(true);
                        if (canChoosenItems) {
                            // отправляем ID выбранной позиции вида исследования
                            Intent intent = new Intent();
                            intent.putExtra("choosen_specializations_id", specializationsID);
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
                    final int currentSpecializationsID = ((SpecializationsRVAdapter.SpecializationsListViewHolders) viewHolder).specializationsID;
                    mCurrentItemID = currentSpecializationsID;
                    String title = getResources().getString(R.string.deleting_visit_question_mini);
                    //String message = getResources().getString(R.string.do_not_show_help_info);
                    String btnPositiveString = getResources().getString(R.string.dialog_positive_button_text);
                    String btnNegativeString = getResources().getString(R.string.dialog_negative_button_text);

                    AlertDialog.Builder ad = new AlertDialog.Builder(MSpecializationsActivity.this);
                    ad.setTitle(title);  // заголовок
                    //ad.setMessage(message); // сообщение
                    ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            if (fDBMethods.checkOfUsageSpecialization(currentSpecializationsID) == true){
                                // вид анализа используется где-то. Сообщим об этом:
                                Snackbar snackbar = Common.getCustomSnackbar(MSpecializationsActivity.this, rv, getResources().getString(R.string.specializations_must_not_delete));
                                snackbar.show();
                            }else {
                                fDBMethods.deleteSpecialization(currentSpecializationsID);
                            }
                            getLoaderManager().restartLoader(0, null, MSpecializationsActivity.this);
                            //getLoaderManager().getLoader(0).forceLoad();
                            //rvAnalisesTypesRVAdapter.notifyDataSetChanged();
                        }
                    });
                    ad.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            getLoaderManager().restartLoader(0, null, MSpecializationsActivity.this);
                            //rvAnalisesTypesRVAdapter.notifyDataSetChanged();
                            //setResult(RESULT_CANCELED);
                            //finish();
                        }
                    });
                    ad.setCancelable(false);
                    ad.show();

                } else {
                    // изменить
                    final int currentSpecializationsID = ((SpecializationsRVAdapter.SpecializationsListViewHolders) viewHolder).specializationsID;
                    mCurrentItemID = currentSpecializationsID;
                    Intent intent = new Intent(MSpecializationsActivity.this, MSpecializationActivity.class);
                    intent.putExtra("choosen_specialization_id", currentSpecializationsID);
                    startActivityForResult(intent, Constants.SPECIALIZATION_DATA);

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
        return new SpecializationsCursorLoader(this, fDBMethods);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // код для определения позиции адаптера, соответствующей текущему specializationsID (это нужно при добавлении новой позиции в список)
        if (mCurrentItemID != -1){
            if (data.getCount()>0){
                data.moveToFirst();
                do {
                    int specializationsID = data.getInt(data.getColumnIndex(DBMethods.TableSpecializations._ID));
                    if (specializationsID == mCurrentItemID){
                        mCurrentItemPosition = data.getPosition();
                        break;
                    }
                }while (data.moveToNext());
            }
        }

        rvSpecializationsRVAdapter.swapCursor(data);

        if (mCurrentItemPosition != -1){
            mSpecializationsAppbarLayout.setExpanded(false);
            rv.smoothScrollToPosition(mCurrentItemPosition +7);
        }else {
            rv.smoothScrollToPosition(0);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        rvSpecializationsRVAdapter.swapCursor(null);
    }

    static class SpecializationsCursorLoader extends CursorLoader {
        DBMethods scldbMethods;

        public SpecializationsCursorLoader(Context context, DBMethods dbMethods) {
            super(context);
            this.scldbMethods = dbMethods;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = scldbMethods.getAllSpecialisations();
            return cursor;
        }
    }





}
