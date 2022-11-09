package ru.kai.mcard;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.os.AsyncTask;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ru.kai.mcard.util.IabBroadcastReceiver;
import ru.kai.mcard.util.IabBroadcastReceiver.IabBroadcastListener;
import ru.kai.mcard.util.IabHelper;

import ru.kai.mcard.util.IabResult;
import ru.kai.mcard.util.Inventory;
import ru.kai.mcard.util.Purchase;
import ru.kai.mcard.utility.Common;

public class MProfilesListActivity extends AppCompatActivity implements IabBroadcastListener, LoaderManager.LoaderCallbacks<Cursor> {

    private DBMethods fDBMethods;

    TextView tvProfilesInAppCaptions;
    Button butProfilesSale;
    Button butProfilesSave;
    ImageView screen_wait;

    android.support.design.widget.FloatingActionButton fabProfilesList;
    LinearLayoutManager llManager;
    RecyclerView rv;
    ProfilesListsRVAdapter rvProfilesListsRVAdapter;
    AppBarLayout mProfilesListsAppbarLayout;
    CollapsingToolbarLayout mProfilesListsCollapsingToolbarLayout;
    NestedScrollView mProfilesListsNestedScrollView;

    int mCurrentItemID = -1;
    int mCurrentItemPosition = -1;

    private Paint p = new Paint();

    Drawable singleDrawable;

    // ---- InApp
    Boolean mustCheck; // "нужно проверять" (количество визитов у профилей) - ограничение если бесплатно. После покупки должен быть false
    IabHelper mHelper;
    IabBroadcastReceiver mBroadcastReceiver;
    private static final String TAG = "ru.kai.mcard";
    //static final String ITEM_SKU = "android.test.purchased";
    static final String ITEM_SKU = "mcard_all_profiles";
    private final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApidQrkZX+5cAeUc/azZeywCVBhSHsQL6PeJzZfk9jVQQSzqDsWyqoluAkXUnvwIsHT47jVMWzCvbg/RVwhGfj/HOGHnBfbxh0tAnMGJa1PC6VAQNZA8Nf+LyiJo3K521EpJ1H3jT972uT/klVqJsGFELJlbESwC7a1f+XGCktvoR1d/NN/IrsqoD9utWu4FLnR+yjrlNFpb75yRyv4WsEqF6I/lRlJsSp8VnjgYIRxoCR81bGcMRYdhP7U/U2CDJ3XdzsMyw34xiPPYywFUkOB2vVr4Qb0/ZmpGgskWtUB0mf+lay9xJGFrI7gj6QzB9orLSrgS0Vzv71y7wJtlQIwIDAQAB>";
    // ---- InApp

    SharedPreferences mSettings;
    TextView profilesListsHelp;
    Boolean showProfilesListsHelp = true;
    Boolean anyProfilesDeleted = false;

    // Google analytics (GA)
    private Tracker mTracker; // еще в onCreate и onResume


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // настройки
        mSettings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.m_activity_profiles_list);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        tvProfilesInAppCaptions = (TextView) findViewById(R.id.tvProfilesInAppCaptions);
        butProfilesSale = (Button)findViewById(R.id.butProfilesSale);
        butProfilesSave = (Button)findViewById(R.id.butProfilesSave);
        screen_wait = (ImageView) findViewById(R.id.screen_wait);
        mProfilesListsNestedScrollView = (NestedScrollView) findViewById(R.id.mProfilesListsNestedScrollView);

        mProfilesListsAppbarLayout = (AppBarLayout)findViewById(R.id.mProfilesListsAppbarLayout);

        mProfilesListsCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.mProfilesListsCollapsingToolbarLayout);
        mProfilesListsCollapsingToolbarLayout.setTitle(getResources().getString(R.string.list_profiles_title));
        mProfilesListsCollapsingToolbarLayout.setContentScrim(Common.getCurrentContentScrimDawable(MProfilesListActivity.this));

        anyProfilesDeleted = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.mProfilesListsToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // могли удалить какой-либо профиль. Установим первый по умолчанию
/*
                Cursor cursorP = fDBMethods.getAllProfiles();
                if (cursorP.getCount() == 1){
                    Intent intent = new Intent();
                    intent.putExtra("mCurrentProfileID", mCurrentItemID);
                    setResult(RESULT_OK, intent);
                }
*/
                if (anyProfilesDeleted){
                    Intent intent = new Intent();
                    intent.putExtra("mCurrentProfileID", mCurrentItemID);
                    setResult(RESULT_OK, intent);
                }
                onBackPressed();
            }
        });

        rv = (RecyclerView)findViewById(R.id.mProfilesListsRecyclerView);
        if (rv != null) {
            rv.setHasFixedSize(true);
        }

        llManager = new LinearLayoutManager(this);
        rv.setLayoutManager(llManager);

        int gettedProfileID = getIntent().getIntExtra("mCurrentProfileID", -1);
        if (gettedProfileID != -1){
            mCurrentItemID = gettedProfileID;
        }

        rvProfilesListsRVAdapter = new ProfilesListsRVAdapter(null);
        rv.setAdapter(rvProfilesListsRVAdapter);

        initSwipe();

        fabProfilesList = (android.support.design.widget.FloatingActionButton) findViewById(R.id.fabProfilesList);
        fabProfilesList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Add")
                        .setAction("Add new profile.")
                        .build());

                Intent intent = new Intent(MProfilesListActivity.this, MProfileEditActivity.class);
                intent.putExtra("given_profiles_id", -1);
                startActivityForResult(intent, Constants.ADD_OR_EDIT_PROFILE);
            }
        });

        // обычные item-ы (для выделения)
        singleDrawable = Common.getOrdinaryItemsDawable(MProfilesListActivity.this);

        // не показывать обучающую информацию -->
        profilesListsHelp = (TextView)findViewById(R.id.profilesListsHelp);

        if (mSettings.contains(Constants.APP_PREFERENCES_SHOW_PROFILES_HELP)) {
            // Получаем число из настроек
            showProfilesListsHelp = mSettings.getBoolean(Constants.APP_PREFERENCES_SHOW_PROFILES_HELP, true);
            if (showProfilesListsHelp == false)profilesListsHelp.setVisibility(View.GONE);
        }

        profilesListsHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = getResources().getString(R.string.do_not_show_help_info);
                //String message = getResources().getString(R.string.do_not_show_help_info);
                String btnPositiveString = getResources().getString(R.string.but_continue_help);
                String btnNegativeString = getResources().getString(R.string.but_cancel_help);

                AlertDialog.Builder ad = new AlertDialog.Builder(MProfilesListActivity.this);
                ad.setTitle(title);  // заголовок
                //ad.setMessage(message); // сообщение
                ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {

                        // Google analytics (GA)
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Action help")
                                .setAction("help profiles отключить.")
                                .build());

                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putBoolean(Constants.APP_PREFERENCES_SHOW_PROFILES_HELP, false);
                        editor.apply();
                        profilesListsHelp.setVisibility(View.GONE);
                    }
                });
                ad.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        // Google analytics (GA)
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Action help")
                                .setAction("help profiles пусть будет пока.")
                                .build());
                    }
                });
                ad.show();
            }
        });
        // <--


        // InApp

        // сначала установим из настроек, а затем после связи с сервером Google перезапишем
        if (mSettings.contains(Constants.APP_PREFERENCES_MUST_CHECK_PROFILE)) {
            // Получаем число из настроек
            mustCheck = mSettings.getBoolean(Constants.APP_PREFERENCES_MUST_CHECK_PROFILE, true);
            setPurchaseVisiblity();
        }

        //base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApidQrkZX+5cAeUc/azZeywCVBhSHsQL6PeJzZfk9jVQQSzqDsWyqoluAkXUnvwIsHT47jVMWzCvbg/RVwhGfj/HOGHnBfbxh0tAnMGJa1PC6VAQNZA8Nf+LyiJo3K521EpJ1H3jT972uT/klVqJsGFELJlbESwC7a1f+XGCktvoR1d/NN/IrsqoD9utWu4FLnR+yjrlNFpb75yRyv4WsEqF6I/lRlJsSp8VnjgYIRxoCR81bGcMRYdhP7U/U2CDJ3XdzsMyw34xiPPYywFUkOB2vVr4Qb0/ZmpGgskWtUB0mf+lay9xJGFrI7gj6QzB9orLSrgS0Vzv71y7wJtlQIwIDAQAB>";

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.enableDebugLogging(false);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // не получилось стартовать, получим из сохраненных настроек:
                    Log.d(TAG, "In-app Billing setup failed: " + result);
                }

                if (mHelper == null) return;


                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(MProfilesListActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);


                Log.d(TAG, "In-app Billing is set up OK");

                setWaitScreen(true);
                try {
                    mustCheck = true; // Для начала обнулим переменную (как будто не покупали).
                    setPurchaseVisiblity(); // обновим интерфейс
                    // пошла связь с сервером.
                    mHelper.queryInventoryAsync(mReceivedInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                    setWaitScreen(false);
                }

            }
        });

        // создаем лоадер для чтения данных
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Google analytics (GA)
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "MProfilesListActivity");
        mTracker.setScreenName("Список профилей (MProfilesListActivity)");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        // обновим (перезагрузим) loader
        if (getLoaderManager().getLoader(0) != null){
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ADD_OR_EDIT_PROFILE){
            if (resultCode == RESULT_OK){
                super.onActivityResult(requestCode, resultCode, data);
                mCurrentItemID = data.getIntExtra("currentItemID", -1);

                Cursor cursor = fDBMethods.getAllProfiles();
                //profilesAdapter.swapCursor(cursor);
                //setMyAdapter();
/*
                if (mCurrentItemID != -1){
                    if (cursor.getCount()>0){
                        cursor.moveToFirst();
                        do {
                            int profilesID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableProfiles._ID));
                            if (profilesID == mCurrentItemID){
                                mCurrentItemPosition = cursor.getPosition();
                                break;
                            }
                        }while (cursor.moveToNext());
                    }
                    rvProfilesListsRVAdapter.notifyItemInserted(mCurrentItemPosition);
                }
*/
                rvProfilesListsRVAdapter.notifyItemInserted(cursor.getCount());

            }
            //getLoaderManager().getLoader(0).forceLoad();

            //getLoaderManager().restartLoader(0, null, this);
            //rv.setAdapter(rvProfilesListsRVAdapter);
        }
        if (mHelper == null) return;
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fDBMethods != null) {
            fDBMethods.close();
        }

        if (mHelper != null) try {
            mHelper.dispose();
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
        mHelper = null;

    }

    public class ProfilesListsRVAdapter extends RecyclerView.Adapter<ProfilesListsRVAdapter.ProfilesListsListViewHolders> {

        Cursor dataCursor;

        ProfilesListsRVAdapter(Cursor cursor) {
            this.dataCursor = cursor;
        }

        @Override
        public ProfilesListsRVAdapter.ProfilesListsListViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_profiles_single, null);
            ProfilesListsListViewHolders diagnosesListVH = new ProfilesListsListViewHolders(v);

            return diagnosesListVH;
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
        public void onBindViewHolder(ProfilesListsRVAdapter.ProfilesListsListViewHolders holder, int position) {

            dataCursor.moveToPosition(position);

            int profilesID = dataCursor.getInt(dataCursor.getColumnIndex(DBMethods.TableProfiles._ID));
            String profilesName = dataCursor.getString(dataCursor.getColumnIndex(DBMethods.TableProfiles.COLUMN_PROFILES_NAME));

            holder.tvSingleRadioCardView.setText(profilesName);

            holder.profilesID = profilesID;
            holder.profilesListsItemCount = getItemCount();

            if (profilesID == mCurrentItemID){
                holder.llSingleRadioCardView.setBackground(Common.getChoosenItemsDrawable(MProfilesListActivity.this));
                holder.imgSingleRadioCardView.setImageResource(R.drawable.ic_radio_button_checked_black_24dp_vector);
                holder.imgSingleRadioCardView.setColorFilter(R.color.colorRedIcon);
            }else {
                holder.llSingleRadioCardView.setBackground(singleDrawable);
                holder.imgSingleRadioCardView.setImageResource(R.drawable.ic_radio_button_unchecked_black_24dp_vector);
                holder.imgSingleRadioCardView.setColorFilter(R.color.colorBlack);
            }
        }

        @Override
        public int getItemCount() {
            return (dataCursor == null) ? 0 : dataCursor.getCount();
        }

        public class ProfilesListsListViewHolders extends RecyclerView.ViewHolder {

            LinearLayout llSingleRadioCardView; // для выделения цветом
            TextView tvSingleRadioCardView;
            ImageView imgSingleRadioCardView;
            int profilesListsItemCount;
            int profilesID;

            public ProfilesListsListViewHolders(final View itemView) {
                super(itemView);
                llSingleRadioCardView = (LinearLayout) itemView.findViewById(R.id.llSingleRadioCardView);
                tvSingleRadioCardView = (TextView) itemView.findViewById(R.id.tvSingleRadioCardView);
                imgSingleRadioCardView = (ImageView) itemView.findViewById(R.id.imgSingleRadioCardView);

                llSingleRadioCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mCurrentItemID = profilesID;
                        //itemView.setSelected(true);

                        MProfilesListActivity.this.getLoaderManager().restartLoader(0, null, MProfilesListActivity.this);
                        AsyncInAppTask asyncInAppTask = new AsyncInAppTask();
                        asyncInAppTask.execute(mCurrentItemID);

/*
                        // отправляем ID выбранной позиции вида исследования
                        Intent intent = new Intent();
                        intent.putExtra("choosen_cures_id", profilesID);
                        setResult(RESULT_OK, intent);
                        finish();
*/

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
                    final int currentProfilesID = ((ProfilesListsRVAdapter.ProfilesListsListViewHolders) viewHolder).profilesID;
                    mCurrentItemID = currentProfilesID;
                    String title = getResources().getString(R.string.deleting_visit_question_mini);
                    //String message = getResources().getString(R.string.do_not_show_help_info);
                    String btnPositiveString = getResources().getString(R.string.dialog_positive_button_text);
                    String btnNegativeString = getResources().getString(R.string.dialog_negative_button_text);

                    final int profilesListsItemCount = ((ProfilesListsRVAdapter.ProfilesListsListViewHolders) viewHolder).profilesListsItemCount;
                    AlertDialog.Builder ad = new AlertDialog.Builder(MProfilesListActivity.this);
                    ad.setTitle(title);  // заголовок
                    //ad.setMessage(message); // сообщение
                    ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
/*
                            if (fDBMethods.checkOfUsageSpecialization(currentSpecializationsID) == true){
                                // вид анализа используется где-то. Сообщим об этом:
                                Snackbar snackbar = Common.getCustomSnackbar(MSpecializationsActivity.this, rv, getResources().getString(R.string.specializations_must_not_delete));
                                snackbar.show();
                            }else {
                                fDBMethods.deleteSpecializationOldMethod(currentSpecializationsID);
                            }
*/
                            if (profilesListsItemCount > 1) {
                                String title = getString(R.string.edit_profiles_delete_profile_title);
                                String message = getString(R.string.edit_profiles_delete_profile_message);
                                String button1String = getString(R.string.edit_profiles_delete_profile_positive_button_text);
                                String button2String = getString(R.string.edit_profiles_delete_profile_negative_button_text);

                                AlertDialog.Builder adbDel = new AlertDialog.Builder(MProfilesListActivity.this);
                                adbDel.setTitle(title);  // заголовок
                                adbDel.setMessage(message); // сообщение
                                adbDel.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int arg1) {
                                        // сначала удалим все визиты вместе со ссылками
                                        Cursor vCursor = fDBMethods.getAllVisitsByProfilesID(currentProfilesID);
                                        if (vCursor.getCount()>0){
                                            vCursor.moveToFirst();
                                            do {
                                                int visitsID = vCursor.getInt(vCursor.getColumnIndex(DBMethods.TableVisits._ID));
                                                fDBMethods.deleteVisitWithDependensesTablesFinely(visitsID);
                                            } while (vCursor.moveToNext());
                                        }
                                        vCursor.close();

                                        // теперь удалим все визиты-исследования
                                        Cursor avCursor = fDBMethods.getAllAnalisesVisitsByProfilesID(currentProfilesID);
                                        if (avCursor.getCount()>0){
                                            avCursor.moveToFirst();
                                            do {
                                                int analisesVisitsID = avCursor.getInt(avCursor.getColumnIndex(DBMethods.TableAnalisesVisits._ID));
                                                fDBMethods.deleteAnalisisVisitWithDependensesTablesFinely(analisesVisitsID);
                                            } while (avCursor.moveToNext());
                                        }
                                        avCursor.close();
                                        // теперь сам профиль
                                        fDBMethods.deleteProfiles(currentProfilesID);

                                        // установим самый первый по списку профиль
                                        Cursor cursorP = fDBMethods.getAllProfiles();
                                        if (cursorP.getCount() > 0){
                                            cursorP.moveToFirst();
                                            mCurrentItemID = cursorP.getInt(cursorP.getColumnIndex(DBMethods.TableProfiles._ID));
                                            SharedPreferences.Editor editor = mSettings.edit();
                                            editor.putInt(Constants.APP_PREFERENCES_CURRENT_PROFILE_ID, mCurrentItemID);  // id примененного фильтра
                                            editor.apply();
                                        }
                                        anyProfilesDeleted = true;
                                        //mCurrentItemID = 1; // установим самый первый по списку профиль
                                        Snackbar snackbar = Common.getCustomSnackbar(MProfilesListActivity.this, rv, getResources().getString(R.string.list_profiles_profile_deleted));
                                        snackbar.show();
                                        MProfilesListActivity.this.getLoaderManager().restartLoader(0, null, MProfilesListActivity.this);
                                    }
                                });
                                adbDel.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int arg1) {

                                    }
                                });
                                adbDel.setCancelable(true);
                                adbDel.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    public void onCancel(DialogInterface dialog) {

                                    }
                                });

                                adbDel.show();

                            } else {
                                Snackbar snackbar = Common.getCustomSnackbar(MProfilesListActivity.this, rv, getResources().getString(R.string.edit_profiles_delete_last_profile));
                                snackbar.show();
                            }




                            //getLoaderManager().getLoader(0).forceLoad();
                            //rvAnalisesTypesRVAdapter.notifyDataSetChanged();
                        }
                    });
                    ad.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            MProfilesListActivity.this.getLoaderManager().restartLoader(0, null, MProfilesListActivity.this);
                            //rvAnalisesTypesRVAdapter.notifyDataSetChanged();
                            //setResult(RESULT_CANCELED);
                            //finish();
                        }
                    });
                    ad.setCancelable(false);
                    ad.show();

                } else {
                    // изменить
                    final int currentProfilesID = ((ProfilesListsRVAdapter.ProfilesListsListViewHolders) viewHolder).profilesID;
                    mCurrentItemID = currentProfilesID;
                    Intent intent = new Intent(MProfilesListActivity.this, MProfileEditActivity.class);
                    intent.putExtra("given_profiles_id", currentProfilesID);
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

    // кнопка "Хочу купить"
    public void profilesSale(View view) {
        setWaitScreen(true);
        try {
            mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001, mPurchaseFinishedListener, base64EncodedPublicKey);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
            setWaitScreen(false);
        }

        // Google analytics (GA)
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Sale")
                .setAction("Хочу купить")
                .build());

    }

    // in-app
    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(mReceivedInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            //complain("Error querying inventory. Another async operation in progress.");
        }

    }

    // слушатель ответа о совершенной покупке
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase){
            if (result.isFailure()) {
                // Handle error
                //Toast.makeText(MProfilesListActivity.this, R.string.list_profiles_purchase_error, Toast.LENGTH_SHORT).show();
                Snackbar snackbar = Common.getCustomSnackbar(MProfilesListActivity.this, rv, getResources().getString(R.string.list_profiles_purchase_error));
                snackbar.show();

                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                //Toast.makeText(MProfilesListActivity.this, R.string.list_profiles_verification_error, Toast.LENGTH_SHORT).show();
                Snackbar snackbar = Common.getCustomSnackbar(MProfilesListActivity.this, rv, getResources().getString(R.string.list_profiles_verification_error));
                snackbar.show();

                setWaitScreen(false);
                return;
            }
            if (purchase.getSku().equals(ITEM_SKU)) {
                //Toast.makeText(MProfilesListActivity.this, R.string.list_profiles_purchase_sucsess, Toast.LENGTH_SHORT).show();
                Snackbar snackbar = Common.getCustomSnackbar(MProfilesListActivity.this, rv, getResources().getString(R.string.list_profiles_purchase_sucsess));
                snackbar.show();

                SharedPreferences.Editor editor = mSettings.edit();
                editor.putBoolean(Constants.APP_PREFERENCES_MUST_CHECK_PROFILE, false);  // куплено, не нужно проверять
                editor.apply();

                mustCheck = false;
                butProfilesSave.setEnabled(true);
                butProfilesSale.setEnabled(false);   //????
                setPurchaseVisiblity();
            }

            setWaitScreen(false);
        }
    };

    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        /*
        * TODO: здесь необходимо свою верификацию реализовать
        * TODO: Хорошо бы ещё с использованием собственного стороннего сервера.
        */
        if (payload.equals(base64EncodedPublicKey)){
            return true;
        }
        return false;
    }

/*
    public void consumeItem() {
        Toast.makeText(z_old_ProfilesListActivity1.this, "consumeItem before", Toast.LENGTH_SHORT).show();
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
        Toast.makeText(z_old_ProfilesListActivity1.this, "consumeItem after", Toast.LENGTH_SHORT).show();
    }
*/

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (mHelper == null){
                setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Query inventory finished.");
            if (result.isFailure()) {
                Log.d(TAG, "Не получили Inventory список купленных товаров (result.isFailure()): " + result);
                setWaitScreen(false);
                // Handle failure
                return;
            }

            Log.d(TAG, "Query inventory was successful.");
            Purchase purchase = inventory.getPurchase(ITEM_SKU);
            if (purchase != null && verifyDeveloperPayload(purchase)){
                //mHelper.consumeAsync(purchase, mConsumeFinishedListener);

                SharedPreferences.Editor editor = mSettings.edit();
                editor.putBoolean(Constants.APP_PREFERENCES_MUST_CHECK_PROFILE, false);  // не нужно проверять
                editor.apply();

                mustCheck = false;
                setPurchaseVisiblity();
                butProfilesSave.setEnabled(true);
            }

            setWaitScreen(false);
        }
    };

    private void setPurchaseVisiblity(){
        if (mustCheck == false){
            tvProfilesInAppCaptions.setVisibility(View.GONE);
            butProfilesSale.setVisibility(View.INVISIBLE);
        }else {
            tvProfilesInAppCaptions.setVisibility(View.VISIBLE);
            butProfilesSale.setVisibility(View.VISIBLE);
        }
    }

    // Enables or disables the "please wait" screen.
    void setWaitScreen(boolean set) {
        //findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
        //findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
        screen_wait.setVisibility(set ? View.VISIBLE : View.GONE);
    }

    public void profilesSave(View view) {
        Intent intent = new Intent();
        intent.putExtra("mCurrentProfileID", mCurrentItemID);
        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean checkEnableBtnSave(int сurrentProfileID){
        // сначала получим тот профиль, который не будем блокировать (первый)
        int enProfile = fDBMethods.getFirstProfilesID();

        if (сurrentProfileID == enProfile){
            return true;
        }else {
            // теперь получим количество визитов выбранного профиля
            Cursor cursorVisits = fDBMethods.getMainFiltratedVisitList(-1, сurrentProfileID, true);
            if (cursorVisits.getCount()>5){
                return false;
            }return true;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new ProfilesListsCursorLoader(this, fDBMethods);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // код для определения позиции адаптера, соответствующей текущему profilesID (это нужно при добавлении новой позиции в список)
/*
        if (mCurrentItemID != -1){
            if (data.getCount()>0){
                data.moveToFirst();
                do {
                    int profilesID = data.getInt(data.getColumnIndex(DBMethods.TableProfiles._ID));
                    if (profilesID == mCurrentItemID){
                        mCurrentItemPosition = data.getPosition();
                        break;
                    }
                }while (data.moveToNext());
            }
        }
*/

        rvProfilesListsRVAdapter.swapCursor(data);

/*
        Boolean canEnabled = checkEnableBtnSave(mCurrentItemID);
        if (mustCheck == true){
            if (canEnabled == true){
                butProfilesSave.setEnabled(true);
            }else {
                butProfilesSave.setEnabled(false);
                //Toast.makeText(MProfilesListActivity.this, getString(R.string.edit_profiles_profile_enabled_false), Toast.LENGTH_LONG).show();
                Snackbar snackbar = Common.getCustomSnackbar(MProfilesListActivity.this, mProfilesListsNestedScrollView, getResources().getString(R.string.edit_profiles_profile_enabled_false));
                //snackbar.getView().setMinimumHeight(300);
                snackbar.show();
            }
        }
*/

        //rv.setAdapter(rvProfilesListsRVAdapter);

/*
        if (mCurrentItemPosition != -1){
            //mProfilesListsAppbarLayout.setExpanded(false);
            rv.smoothScrollToPosition(mCurrentItemPosition +7);
        }else {
            rv.smoothScrollToPosition(0);
        }
*/


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        rvProfilesListsRVAdapter.swapCursor(null);
    }

    static class ProfilesListsCursorLoader extends CursorLoader {
        DBMethods scldbMethods;

        public ProfilesListsCursorLoader(Context context, DBMethods dbMethods) {
            super(context);
            this.scldbMethods = dbMethods;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = scldbMethods.getAllProfiles();
            return cursor;
        }
    }

    private class AsyncInAppTask extends AsyncTask<Integer, Void, Void> {

        private Boolean canEnabled;

        @Override
        protected Void doInBackground(Integer... params) {
            //int myProgress = 0;
            // [... Выполните задачу в фоновом режиме, обновите переменную myProgress...]
            //publishProgress(myProgress);
            // [... Продолжение выполнения фоновой задачи ...]

            int currentProfilesID = 0;
            for (Integer item : params){
                currentProfilesID = item;
            }
            canEnabled = checkEnableBtnSave(currentProfilesID);


            // Верните значение, ранее переданное в метод onPostExecute
            //int result = 0;

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (mustCheck == true){
                if (canEnabled == true){
                    butProfilesSave.setEnabled(true);
                }else {
                    butProfilesSave.setEnabled(false);
                    //Toast.makeText(MProfilesListActivity.this, getString(R.string.edit_profiles_profile_enabled_false), Toast.LENGTH_LONG).show();
                    Snackbar snackbar = Common.getCustomSnackbar(MProfilesListActivity.this, mProfilesListsNestedScrollView, getResources().getString(R.string.edit_profiles_profile_enabled_false));
                    //snackbar.getView().setMinimumHeight(300);
                    snackbar.show();

                }
            }
        }

    }


}
