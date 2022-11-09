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

public class MFilterListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private DBMethods fDBMethods;

    android.support.design.widget.FloatingActionButton fabFilterList;
    LinearLayoutManager llManager;
    RecyclerView rv;
    FilterListsRVAdapter rvFilterListsRVAdapter;
    AppBarLayout mFilterListsAppbarLayout;
    CollapsingToolbarLayout mFilterListsCollapsingToolbarLayout;
    TextView filtersListsHelp;
    Toolbar toolbar;

    int mCurrentItemID = -1;
    int mCurrentItemPosition = -1;

    private Paint p = new Paint();
    Drawable singleDrawable;

    SharedPreferences mSettings;
    Boolean showFiltersHelp = true;

    // Google analytics (GA)
    private Tracker mTracker; // еще в onCreate и onResume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // настройки
        mSettings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.m_activity_filter_list);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA

        mFilterListsAppbarLayout = (AppBarLayout)findViewById(R.id.mFilterListsAppbarLayout);

        mFilterListsCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.mFilterListsCollapsingToolbarLayout);
        mFilterListsCollapsingToolbarLayout.setTitle(getResources().getString(R.string.filter_list_title));
        mFilterListsCollapsingToolbarLayout.setContentScrim(Common.getCurrentContentScrimDawable(MFilterListActivity.this));

        toolbar = (Toolbar) findViewById(R.id.mFilterListsToolbar);
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

        rv = (RecyclerView)findViewById(R.id.mFilterListsRecyclerView);
        if (rv != null) {
            rv.setHasFixedSize(true);
        }

        llManager = new LinearLayoutManager(this);
        rv.setLayoutManager(llManager);

        int gettedID = getIntent().getIntExtra("current_filter_id", -1);
        if (gettedID != -1){
            mCurrentItemID = gettedID;
        }

        // создаем лоадер для чтения данных
        getLoaderManager().initLoader(0, null, this);

        rvFilterListsRVAdapter = new FilterListsRVAdapter(null);
        rv.setAdapter(rvFilterListsRVAdapter);

        initSwipe();

        fabFilterList = (android.support.design.widget.FloatingActionButton) findViewById(R.id.fabFilterList);
        fabFilterList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Add")
                        .setAction("Add расширенный фильтр.")
                        .build());

                Intent intent = new Intent(MFilterListActivity.this, MFilterExtActivity.class);
                startActivityForResult(intent, Constants.FILTER_EXT_DATA);
            }
        });

        // обычные item-ы (для выделения)
        singleDrawable = Common.getOrdinaryItemsDawable(MFilterListActivity.this);

        // не показывать обучающую информацию -->
        filtersListsHelp = (TextView)findViewById(R.id.filtersListsHelp);

        if (mSettings.contains(Constants.APP_PREFERENCES_SHOW_FILTERS_HELP)) {
            // Получаем число из настроек
            showFiltersHelp = mSettings.getBoolean(Constants.APP_PREFERENCES_SHOW_FILTERS_HELP, true);
            if (!showFiltersHelp){
                filtersListsHelp.setVisibility(View.GONE);
            }
        }

        filtersListsHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = getResources().getString(R.string.do_not_show_help_info);
                //String message = getResources().getString(R.string.do_not_show_help_info);
                String btnPositiveString = getResources().getString(R.string.but_continue_help);
                String btnNegativeString = getResources().getString(R.string.but_cancel_help);

                AlertDialog.Builder ad = new AlertDialog.Builder(MFilterListActivity.this);
                ad.setTitle(title);  // заголовок
                //ad.setMessage(message); // сообщение
                ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {

                        // Google analytics (GA)
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Action help")
                                .setAction("help filtersList отключить.")
                                .build());

                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putBoolean(Constants.APP_PREFERENCES_SHOW_FILTERS_HELP, false);
                        editor.apply();
                        filtersListsHelp.setVisibility(View.GONE);
                    }
                });
                ad.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        // Google analytics (GA)
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Action help")
                                .setAction("help filtersList пусть будет пока.")
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
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "Список сохраненных фильтров (MFilterListActivity)");
        mTracker.setScreenName("Список сохраненных фильтров (MFilterListActivity)");
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
        if (requestCode == Constants.FILTER_EXT_DATA){
            if (resultCode == RESULT_OK){
                // сразу же применим сформированный фмльтр
                int chosenFiltersID = data.getIntExtra("chosenFiltersID", -1);
                Intent intent = new Intent();
                intent.putExtra("chosenFiltersID", chosenFiltersID);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    public class FilterListsRVAdapter extends RecyclerView.Adapter<FilterListsRVAdapter.FilterListsViewHolders> {

        Cursor dataCursor;

        FilterListsRVAdapter(Cursor cursor) {
            this.dataCursor = cursor;
        }

        @Override
        public FilterListsViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_common_single, null);
            FilterListsViewHolders filtersListVH = new FilterListsViewHolders(v);

            return filtersListVH;
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
        public void onBindViewHolder(FilterListsViewHolders holder, int position) {

            dataCursor.moveToPosition(position);

            int filtersID = dataCursor.getInt(dataCursor.getColumnIndex(DBMethods.TableFilters._ID));
            String filtersName = dataCursor.getString(dataCursor.getColumnIndex(DBMethods.TableFilters.COLUMN_FILTERS_NAME));

            holder.tvSimpleCommonCardView.setText(filtersName);

            holder.filtersID = filtersID;

            if (filtersID == mCurrentItemID){
                holder.llSimpleCommonCardView.setBackground(Common.getChoosenItemsDrawable(MFilterListActivity.this));
            }else {
                holder.llSimpleCommonCardView.setBackground(singleDrawable);
            }
        }

        @Override
        public int getItemCount() {
            return (dataCursor == null) ? 0 : dataCursor.getCount();
        }

        class FilterListsViewHolders extends RecyclerView.ViewHolder {

            LinearLayout llSimpleCommonCardView; // для выделения цветом
            TextView tvSimpleCommonCardView;
            int filtersID;

            FilterListsViewHolders(final View itemView) {
                super(itemView);
                llSimpleCommonCardView = (LinearLayout) itemView.findViewById(R.id.llSimpleCommonCardView);
                tvSimpleCommonCardView = (TextView) itemView.findViewById(R.id.tvSimpleCommonCardView);

                llSimpleCommonCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //itemView.setSelected(true);

                        // отправляем ID выбранной позиции вида фильтра
                        Intent intent = new Intent();
                        intent.putExtra("chosenFiltersID", filtersID);
                        setResult(RESULT_OK, intent);
                        finish();

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
                    final int currentFiltersID = ((FilterListsRVAdapter.FilterListsViewHolders) viewHolder).filtersID;
                    mCurrentItemID = currentFiltersID;
                    String title = getResources().getString(R.string.deleting_visit_question_mini);
                    //String message = getResources().getString(R.string.do_not_show_help_info);
                    String btnPositiveString = getResources().getString(R.string.dialog_positive_button_text);
                    String btnNegativeString = getResources().getString(R.string.dialog_negative_button_text);

                    AlertDialog.Builder ad = new AlertDialog.Builder(MFilterListActivity.this);
                    ad.setTitle(title);  // заголовок
                    //ad.setMessage(message); // сообщение
                    ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            // нельзя удалять текущий установленный фильтр (а то больше НИКОГДА не откроешь приложение); сбросим его:
                            int mCurrentFilterID = -1;
                            if (mSettings.contains(Constants.APP_PREFERENCES_CURRENT_FILTER_ID)) {
                                mCurrentFilterID = mSettings.getInt(Constants.APP_PREFERENCES_CURRENT_FILTER_ID, -1);
                            }
                            if(currentFiltersID == mCurrentFilterID){
                                SharedPreferences.Editor editor = mSettings.edit();
                                editor.putBoolean(Constants.APP_PREFERENCES_FILTER_OFF, false);
                                editor.putInt(Constants.APP_PREFERENCES_CURRENT_FILTER_ID, -1);  // id примененного фильтра
                                editor.apply();
                            }

                            fDBMethods.deleteFilter(currentFiltersID);
                            getLoaderManager().restartLoader(0, null, MFilterListActivity.this);
                        }
                    });
                    ad.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            getLoaderManager().getLoader(0).forceLoad();
                        }
                    });
                    ad.setCancelable(false);
                    ad.show();

                } else {
                    // изменить
                    final int currentFiltersID = ((FilterListsRVAdapter.FilterListsViewHolders) viewHolder).filtersID;
                    mCurrentItemID = currentFiltersID;
                    Intent intent = new Intent(MFilterListActivity.this, MFilterExtActivity.class);
                    intent.putExtra("current_filter_id", currentFiltersID);
                    startActivityForResult(intent, Constants.FILTER_EXT_DATA);

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
                        RectF icon_dest = new RectF((float) itemView.getRight() - 1*width,(float) itemView.getBottom() - width, (float) itemView.getRight() - 0*width,(float)itemView.getTop() + width);
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
        return new FiltersCursorLoader(this, fDBMethods);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // код для определения позиции адаптера, соответствующей текущему filtersID (это нужно при добавлении новой позиции в список)
        if (mCurrentItemID != -1){
            if (data.getCount()>0){
                data.moveToFirst();
                do {
                    int filtersID = data.getInt(data.getColumnIndex(DBMethods.TableFilters._ID));
                    if (filtersID == mCurrentItemID){
                        mCurrentItemPosition = data.getPosition();
                        break;
                    }
                }while (data.moveToNext());
            }
        }

        rvFilterListsRVAdapter.swapCursor(data);

        if (mCurrentItemPosition != -1){
            mFilterListsAppbarLayout.setExpanded(false);
            rv.smoothScrollToPosition(mCurrentItemPosition +5);
        }else {
            rv.smoothScrollToPosition(0);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        rvFilterListsRVAdapter.swapCursor(null);
    }

    static class FiltersCursorLoader extends CursorLoader {
        DBMethods scldbMethods;

        FiltersCursorLoader(Context context, DBMethods dbMethods) {
            super(context);
            this.scldbMethods = dbMethods;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = scldbMethods.getAllFilters();
            return cursor;
        }
    }

}
