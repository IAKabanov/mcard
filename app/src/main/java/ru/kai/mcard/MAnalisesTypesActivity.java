package ru.kai.mcard;

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

// Виды исследований (список)
public class MAnalisesTypesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private DBMethods fDBMethods;

    FloatingActionButton fabMAnalisesTypes;

    LinearLayoutManager llManager;
    RecyclerView rv;
//    public ArrayList<AnalisesTypesModel> analisesTypesList;
    AnalisesTypesRVAdapter rvAnalisesTypesRVAdapter;
    AppBarLayout appBarLayout;
    CollapsingToolbarLayout mAnalisesTypesScrolling;

    int mCurrentItemID = -1;
    int mCurrentItemPosition = -1;

    private Paint p = new Paint();

    Drawable singleDrawable;
    //Drawable selectedDrawable;
    Boolean canChoosenItems = true; // если список открыт из NavigationDrawer-а то при выборе item-а ничего делать не будем. Закрывают пусть через "назад"

    // Google analytics (GA)
    private Tracker mTracker; // еще в onCreate и onResume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));
/*
        SharedPreferences mSettings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        //int currentTheme = R.style.Theme_StandartGreen;
        int currentTheme = 0;
        if (mSettings.contains(Constants.APP_PREFERENCES_CURRENT_THEME)) {
            // Получаем число из настроек
            currentTheme = mSettings.getInt(Constants.APP_PREFERENCES_CURRENT_THEME, R.style.Theme_StandartGreen);
            setTheme(currentTheme);
        }
*/

        setContentView(R.layout.m_activity_analises_types);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA

        appBarLayout = (AppBarLayout)findViewById(R.id.mAnalisesTypesAppbar);

        mAnalisesTypesScrolling = (CollapsingToolbarLayout) findViewById(R.id.mAnalisesTypesScrolling);
        mAnalisesTypesScrolling.setTitle(getResources().getString(R.string.analises_types_title));
        mAnalisesTypesScrolling.setContentScrim(Common.getCurrentContentScrimDawable(MAnalisesTypesActivity.this));
        //mAnalisesTypesScrolling.setContentScrimColor(Common.getCurrentContentScrimColor(MAnalisesTypesActivity.this));


        Toolbar toolbar = (Toolbar) findViewById(R.id.mAnalisesTypesToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        rv = (RecyclerView)findViewById(R.id.mAnalisesTypesRecyclerView);
        if (rv != null) {
            rv.setHasFixedSize(true);
        }

        llManager = new LinearLayoutManager(this);
        rv.setLayoutManager(llManager);
        //rv.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(this));

        int gettedID = getIntent().getIntExtra("choosen_analisis_type_id", -1);
        if (gettedID != -1){
            mCurrentItemID = gettedID;
        }
        canChoosenItems = getIntent().getBooleanExtra("can_choosen_items", true);

        // создаем лоадер для чтения данных
        getLoaderManager().initLoader(0, null, this);
/*
        loader.registerListener(0, new Loader.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(Loader loader, Object data) {
                rv.smoothScrollToPosition(rv.getAdapter().getItemCount()-1);
            }
        });
*/

        //rvAnalisesTypesRVAdapter = new AnalisesTypesRVAdapter(analisesTypesList, null);

        rvAnalisesTypesRVAdapter = new AnalisesTypesRVAdapter(null);
        rv.setAdapter(rvAnalisesTypesRVAdapter);

        initSwipe();

        fabMAnalisesTypes = (FloatingActionButton) findViewById(R.id.fabMAnalisesTypes);
        fabMAnalisesTypes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Add")
                        .setAction("Add AnalisesTypes.")
                        .build());

                Intent intent = new Intent(MAnalisesTypesActivity.this, MAnalisisTypeActivity.class);
                startActivityForResult(intent, Constants.ANALISES_TYPES_DATA);
            }
        });

        // обычные item-ы (для выделения)
        singleDrawable = Common.getOrdinaryItemsDawable(MAnalisesTypesActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // обновим (перезагрузим) loader
        getLoaderManager().restartLoader(0, null, this);

        // Google analytics (GA)
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "Список типов анализов (MAnalisesTypesActivity)");
        mTracker.setScreenName("Список типов анализов (MAnalisesTypesActivity)");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    protected void onDestroy() {
        super.onDestroy();
        if (fDBMethods != null) {
            fDBMethods.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ANALISES_TYPES_DATA){
            if (resultCode == RESULT_OK){
                //getLoaderManager().getLoader(0).forceLoad();
                //if (mCurrentItemPosition != -1){
                //    rv.smoothScrollToPosition(mCurrentItemPosition);
                //}
                mCurrentItemID = data.getIntExtra("currentItemID", -1);

            }
            //mCurrentItemPosition = data.getIntExtra("currentAdapterPosition", -1);

            getLoaderManager().restartLoader(0, null, this);
            //getLoaderManager().getLoader(0).forceLoad();
            //rvAnalisesTypesRVAdapter.notifyDataSetChanged();
        }
    }



/*
    public class AnalisesTypesModel {

        int analisisTypesID; // id
        //int adapterPosition;

        ArrayList<AnalisesTypesModel> analisesTypesList;

        AnalisesTypesModel(){
            this.analisesTypesList = new ArrayList<>();
        }

        AnalisesTypesModel(int analisisTypesID){
            this.analisisTypesID = analisisTypesID;
        }

        public void add(int analisisTypesID){
            analisesTypesList.add(new AnalisesTypesModel(analisisTypesID));
        }

    }
*/

    public class AnalisesTypesRVAdapter extends RecyclerView.Adapter<AnalisesTypesRVAdapter.AnalisesTypesListViewHolders> {

        Cursor dataCursor;

        //ArrayList<AnalisesTypesModel> analisesTypesList;

        AnalisesTypesRVAdapter(Cursor cursor) {
        //AnalisesTypesRVAdapter(ArrayList<AnalisesTypesModel> analisesTypesList, Cursor cursor) {
            //this.analisesTypesList = analisesTypesList;
            this.dataCursor = cursor;
        }

        @Override
        public AnalisesTypesListViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_common_single, null);
            AnalisesTypesListViewHolders analisesTypesListVH = new AnalisesTypesListViewHolders(v);

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
        public void onBindViewHolder(AnalisesTypesListViewHolders holder, int position) {
            //holder.analisesTypesModel = analisesTypesList.get(position);
            //String currentAnalisisTypesTitle = fDBMethods.getAnalisesTypesNameByID(holder.analisesTypesModel.analisisTypesID);
            //holder.tvDubbleCommonCardViewTitle.setText(currentAnalisisTypesTitle);

            dataCursor.moveToPosition(position);

            int analisisTypesID = dataCursor.getInt(dataCursor.getColumnIndex(DBMethods.TableAnalisesTypes._ID));
            //String analisisTypesName = dataCursor.getString(dataCursor.getColumnIndex(DBMethods.TableAnalisesTypes.COLUMN_ANALISES_TYPES_NAME));

            String currentAnalisisTypesTitle = fDBMethods.getAnalisesTypesNameByID(analisisTypesID);
            holder.tvSimpleCommonCardView.setText(currentAnalisisTypesTitle);

            holder.analisisTypesID = analisisTypesID;
            //holder.adapterPosition = position;

            if (analisisTypesID == mCurrentItemID){
                holder.llSimpleCommonCardView.setBackground(Common.getChoosenItemsDrawable(MAnalisesTypesActivity.this));
                //mCurrentItemPosition = position;
            }else {
                holder.llSimpleCommonCardView.setBackground(singleDrawable);
            }
        }

        @Override
        public int getItemCount() {
            //return analisesTypesList.size();
            return (dataCursor == null) ? 0 : dataCursor.getCount();
        }

        public class AnalisesTypesListViewHolders extends RecyclerView.ViewHolder {

            LinearLayout llSimpleCommonCardView; // для выделения цветом
            TextView tvSimpleCommonCardView;
            int analisisTypesID;
            //int adapterPosition;
            //AnalisesTypesModel analisesTypesModel;


            public AnalisesTypesListViewHolders(final View itemView) {
                super(itemView);
                llSimpleCommonCardView = (LinearLayout) itemView.findViewById(R.id.llSimpleCommonCardView);
                tvSimpleCommonCardView = (TextView) itemView.findViewById(R.id.tvSimpleCommonCardView);

                tvSimpleCommonCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        itemView.setSelected(true);
                        if (canChoosenItems) {
                            // отправляем ID выбранной позиции вида исследования
                            Intent intent = new Intent();
                            intent.putExtra("given_analises_types_id", analisisTypesID);
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                    }
                });
                // большая кнопка
/*
                btn_Visists_RecommendationsItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // нет листа визитов, выполяющих назначение, (графика приема лекарств) просто удаляем из списка
                        recommendationsList.remove(recommendationsModel);
                        rvVisitsRecommendationsAdapter.notifyDataSetChanged();

                        //ArrayList<MVisitRecommendationsFragment.VisitsRecommendationsModel> recommendationsList = fragmentVisitMrecommendations.recommendationsList;
                        if (recommendationsList != null) {
                            fDBMethods.updateVisitsRecommendationsList(gettedID, recommendationsList);
                        }

                    }
                });
*/

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
                    final int currentAnalisisTypeID = ((AnalisesTypesRVAdapter.AnalisesTypesListViewHolders) viewHolder).analisisTypesID;
                    mCurrentItemID = currentAnalisisTypeID;
                    String title = getResources().getString(R.string.deleting_visit_question_mini);
                    //String message = getResources().getString(R.string.do_not_show_help_info);
                    String btnPositiveString = getResources().getString(R.string.dialog_positive_button_text);
                    String btnNegativeString = getResources().getString(R.string.dialog_negative_button_text);

                    AlertDialog.Builder ad = new AlertDialog.Builder(MAnalisesTypesActivity.this);
                    ad.setTitle(title);  // заголовок
                    //ad.setMessage(message); // сообщение
                    ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            if (fDBMethods.checkOfUsageAnalisisType(currentAnalisisTypeID) == true){
                                // вид анализа используется где-то. Сообщим об этом:
                                Snackbar snackbar = Common.getCustomSnackbar(MAnalisesTypesActivity.this, rv, getResources().getString(R.string.analises_types_must_not_delete));
                                snackbar.show();
                            }else {
                                fDBMethods.deleteAnalisesType(currentAnalisisTypeID);
                            }
                            getLoaderManager().restartLoader(0, null, MAnalisesTypesActivity.this);
                            //getLoaderManager().getLoader(0).forceLoad();
                            //rvAnalisesTypesRVAdapter.notifyDataSetChanged();
                        }
                    });
                    ad.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            getLoaderManager().restartLoader(0, null, MAnalisesTypesActivity.this);
                            //rvAnalisesTypesRVAdapter.notifyDataSetChanged();
                            //setResult(RESULT_CANCELED);
                            //finish();
                        }
                    });
                    ad.setCancelable(false);
                    ad.show();

                } else {
                    // изменить
                    final int currentAnalisisTypeID = ((AnalisesTypesRVAdapter.AnalisesTypesListViewHolders) viewHolder).analisisTypesID;
                    mCurrentItemID = currentAnalisisTypeID;
                    Intent intent = new Intent(MAnalisesTypesActivity.this, MAnalisisTypeActivity.class);
                    intent.putExtra("given_analises_types_id", currentAnalisisTypeID);
                    //intent.putExtra("getted_adapter_position", ((AnalisesTypesRVAdapter.AnalisesTypesListViewHolders) viewHolder).adapterPosition);
                    startActivityForResult(intent, Constants.ANALISES_TYPES_DATA);

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
                        //p.setColor(Color.parseColor("#388E3C"));
                        p.setColor(Color.parseColor("#20ab1e"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.btn_edit_black);
                        //RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + 0*width ,(float) itemView.getBottom() - width,(float) itemView.getLeft()+ 1*width,(float)itemView.getTop() + width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    } else {
                        // delete
                        //p.setColor(alternativeThemeColor);
                        //p.setColor(Color.parseColor("#D32F2F"));
                        p.setColor(Color.parseColor("#FF4081"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.btn_delete_black);
                        //RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
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
        return new AnalisesTypesCursorLoader(this, fDBMethods);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // код для определения позиции адаптера, соответствующей текущему analisesTypesID (это нужно при добавлении новой позиции в список)
        if (mCurrentItemID != -1){
            if (data.getCount()>0){
                data.moveToFirst();
                do {
                    int analisesTypesID = data.getInt(data.getColumnIndex(DBMethods.TableAnalisesTypes._ID));
                    if (analisesTypesID == mCurrentItemID){
                        mCurrentItemPosition = data.getPosition();
                        break;
                    }
                }while (data.moveToNext());
            }
        }

        rvAnalisesTypesRVAdapter.swapCursor(data);
        //rv.smoothScrollToPosition(19);
        //rv.smoothScrollToPosition(rv.getAdapter().getItemCount()-1);
        //rv.smoothScrollBy(rv.getChildAt(0).getHeight()*19, 0);
        //llManager.scrollToPositionWithOffset(19, 0);
        //llManager.setStackFromEnd(true);
        //rv.getLayoutManager().scrollToPosition(21);
//        AppBarLayout appBarLayout = (AppBarLayout)findViewById(R.id.mAnalisesTypesAppbar);
//        appBarLayout.setExpanded(false);
        //rv.getLayoutManager().scrollToPosition(21);
        //rvAnalisesTypesRVAdapter.swapCursor(data);

        //mCurrentItemPosition = rvAnalisesTypesRVAdapter.getAdapterPositionByAnalisisTypesID(mCurrentItemID);
        if (mCurrentItemPosition != -1){
            appBarLayout.setExpanded(false);
            rv.smoothScrollToPosition(mCurrentItemPosition +7);
            //llManager.scrollToPosition(mCurrentItemPosition +7);
        }else rv.smoothScrollToPosition(0);
        //rv.smoothScrollToPosition(19);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        rvAnalisesTypesRVAdapter.swapCursor(null);
    }

    static class AnalisesTypesCursorLoader extends CursorLoader {
        DBMethods scldbMethods;

        public AnalisesTypesCursorLoader(Context context, DBMethods dbMethods) {
            super(context);
            this.scldbMethods = dbMethods;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = scldbMethods.getAllAnalisesTypes();
            return cursor;
        }
    }



}
