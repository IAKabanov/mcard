package ru.kai.mcard;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MVisitDirectionsFragment extends Fragment {

    private DBMethods fDBMethods;
    int gettedID;
    int mDiagnosisID;
    int mCurrentProfileID; // нужно знать для какого профиля сохраняем данный визит

    FloatingActionsMenu famAddVisitsDirections;

    public ArrayList<VisitsDirectionsModel> directionsList;
    visitsDirectionsRVAdapter rvVisitsDirectionsAdapter;
    RecyclerView rv;

    // настройки
    private SharedPreferences mSettings;
    private int showVisitsDirectionsHelp = 1;
    TextView visits_fragment_directions_list_help;

    public MVisitDirectionsFragment() {
        // Required empty public constructor
    }

    public static MVisitDirectionsFragment newInstance(Bundle bundle) {
        MVisitDirectionsFragment currentFragment = new MVisitDirectionsFragment();
        Bundle args = new Bundle();
        args.putBundle("gettedArgs", bundle);
        currentFragment.setArguments(args);
        //currentFragment.directionsList = new ArrayList<>();
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
        View rootView = inflater.inflate(R.layout.fragment_visit_m_directions, container, false);

        famAddVisitsDirections = (FloatingActionsMenu)rootView.findViewById(R.id.famAddVisitsDirections);

        rv = (RecyclerView) rootView.findViewById(R.id.rvVisitsFragmentDirections);
        rv.setHasFixedSize(true);

        LinearLayoutManager llManager = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llManager);

        fDBMethods = new DBMethods(getContext());
        fDBMethods.open();

        // при первом открытии (не поворот экрана)
        if (savedInstanceState == null) {
            // получаем из активности
            Bundle bundle = getArguments().getBundle("gettedArgs");
            gettedID = bundle.getInt("gettedID", 0);
            mDiagnosisID = bundle.getInt("diagnosisID", 0);
            mCurrentProfileID = bundle.getInt("currentProfileID", -1);
        }

        directionsList = new ArrayList<>();
        if (gettedID > 0){
            Cursor cursor = fDBMethods.getAllVisitsDirectionsList(gettedID);
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                do {
                    int rowID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsDirections._ID));
                    int basicVisitsID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsDirections.COLUMN_BASIC_VISITS_ID));
                    int directionsVisitsType = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE));
                    int anTypeOrSpecID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID));
                    int directionsVisitsID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID));

                    directionsList.add(new VisitsDirectionsModel(basicVisitsID, directionsVisitsType, anTypeOrSpecID, directionsVisitsID));

                }while (cursor.moveToNext());
            }
            cursor.close();
        }

        rvVisitsDirectionsAdapter = new visitsDirectionsRVAdapter(directionsList);
        rv.setAdapter(rvVisitsDirectionsAdapter);

        // не показывать обучающую информацию
        visits_fragment_directions_list_help = (TextView)rootView.findViewById(R.id.visits_fragment_directions_list_help);

        mSettings = getContext().getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(Constants.APP_PREFERENCES_SHOW_VISITS_DIRECTIONS_HELP)) {
            // Получаем число из настроек
            showVisitsDirectionsHelp = mSettings.getInt(Constants.APP_PREFERENCES_SHOW_VISITS_DIRECTIONS_HELP, 1);
            if (showVisitsDirectionsHelp == 0)visits_fragment_directions_list_help.setVisibility(View.GONE);
        }

        visits_fragment_directions_list_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = getResources().getString(R.string.do_not_show_help_info);
                //String message = getResources().getString(R.string.do_not_show_help_info);
                String btnPositiveString = getResources().getString(R.string.but_continue_help);
                String btnNegativeString = getResources().getString(R.string.but_cancel_help);

                AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
                ad.setTitle(title);  // заголовок
                //ad.setMessage(message); // сообщение
                ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putInt(Constants.APP_PREFERENCES_SHOW_VISITS_DIRECTIONS_HELP, 0);
                        editor.apply();
                        visits_fragment_directions_list_help.setVisibility(View.GONE);
                    }
                });
                ad.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        //setResult(RESULT_CANCELED);
                        //finish();
                    }
                });
                famAddVisitsDirections.collapse();
                ad.show();
            }
        });
        //-------------------------

        // пусть fam скрывается, если его открыли, а затем просто тапнули в другое место:
        CoordinatorLayout visitsFragmentDirectionsListCoordinatorLayout = (CoordinatorLayout)rootView.findViewById(R.id.visitsFragmentDirectionsListCoordinatorLayout);
        visitsFragmentDirectionsListCoordinatorLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                famAddVisitsDirections.collapse();

                return false;
            }
        });
        NestedScrollView visitsFragmentDirectionsListNestedScrollView = (NestedScrollView)rootView.findViewById(R.id.visitsFragmentDirectionsListNestedScrollView);
        visitsFragmentDirectionsListNestedScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                famAddVisitsDirections.collapse();

                return false;
            }
        });

        // добавим строку с типом исследования:
        FloatingActionButton fabAddVisitsDirectionsAnalisisType = (FloatingActionButton)rootView.findViewById(R.id.fabAddVisitsDirectionsAnalisisType);
        fabAddVisitsDirectionsAnalisisType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                famAddVisitsDirections.collapse();
                if (gettedID > 0){
                    Intent intent = new Intent(getContext(), MAnalisesTypesActivity.class);
                    startActivityForResult(intent, Constants.ANALISES_TYPES_DATA);
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

                            Intent intent = new Intent(getContext(), MAnalisesTypesActivity.class);
                            startActivityForResult(intent, Constants.ANALISES_TYPES_DATA);
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
            }
        });
        // добавим строку со специализацией доктора:
        FloatingActionButton fabAddVisitsDirectionsDoctorSpesialisation = (FloatingActionButton)rootView.findViewById(R.id.fabAddVisitsDirectionsDoctorSpesialisation);
        fabAddVisitsDirectionsDoctorSpesialisation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                famAddVisitsDirections.collapse();
                if (gettedID > 0){
                    Intent intent = new Intent(getContext(), MSpecializationsActivity.class);
                    startActivityForResult(intent, Constants.SPECIALIZATION_DATA);
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

                            Intent intent = new Intent(getContext(), MSpecializationsActivity.class);
                            startActivityForResult(intent, Constants.SPECIALIZATION_DATA);
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
            }
        });

        return rootView;
    }


    @Override  // получаем ответ от открытых ранее активностей
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            switch (requestCode) {
                // сразу сохраним в БД      (пока только добавляем в список. В таблицу БД сохраним только при сохранении всего визита.)
                case Constants.ANALISES_TYPES_DATA:
                    // если выбрали вид исследования

                    int analisisTypesID = data.getIntExtra("given_analises_types_id", 0);

                    // проверим не содержится ли уже исследование в списке (проверка по analisisTypesID и по directionsVisitsID)
                    // то есть одинаковое исследование может повторяться в списке только если его назначили на разных визитах-консультациях.
                    int currentBasicVisitsID = 0;
                    int currentDirectionsVisitsType = 0;
                    int currentAnTypeOrSpecID = 0;
                    int currentDirectionsVisitsID = 0;
                    int i;
                    boolean itIsDubble = false;

                    for (i = 0; i < directionsList.size(); i++) {
                        VisitsDirectionsModel itemModel = directionsList.get(i);
                        currentBasicVisitsID = itemModel.basicVisitsID;
                        currentDirectionsVisitsType = itemModel.directionsVisitsType;
                        currentAnTypeOrSpecID = itemModel.anTypeOrSpecID;
                        currentDirectionsVisitsID = itemModel.directionsVisitsID;
                        if (currentDirectionsVisitsType == Constants.DIRECTIONS_ANALISIS_TYPE_1){
                            if (currentAnTypeOrSpecID == analisisTypesID){
                                if (currentDirectionsVisitsID == 0){
                                    itIsDubble = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (itIsDubble == false){
                        directionsList.add(new VisitsDirectionsModel(gettedID, Constants.DIRECTIONS_ANALISIS_TYPE_1, analisisTypesID, 0));
                        rvVisitsDirectionsAdapter.notifyDataSetChanged();
                    }

                    break;

                case Constants.SPECIALIZATION_DATA:
                    // если выбрали вид исследования

                    int specialisationsID = data.getIntExtra("choosen_specializations_id", 0);

                    // проверим не содержится ли уже исследование в списке (проверка по analisisTypesID и по directionsVisitsID)
                    // то есть одинаковое исследование может повторяться в списке только если его назначили на разных визитах-консультациях.
                    int currentBasicVisitsID_ = 0;
                    int currentDirectionsVisitsType_ = 0;
                    int currentAnTypeOrSpecID_ = 0;
                    int currentDirectionsVisitsID_ = 0;
                    int j;
                    boolean itIsDubble_ = false;

                    for (j = 0; j < directionsList.size(); j++) {
                        VisitsDirectionsModel itemModel = directionsList.get(j);
                        currentBasicVisitsID_ = itemModel.basicVisitsID;
                        currentDirectionsVisitsType_ = itemModel.directionsVisitsType;
                        currentAnTypeOrSpecID = itemModel.anTypeOrSpecID;
                        currentDirectionsVisitsID_ = itemModel.directionsVisitsID;
                        if (currentDirectionsVisitsType_ == Constants.DIRECTIONS_VISITS_TYPE_0){
                            if (currentAnTypeOrSpecID_ == specialisationsID){
                                if (currentDirectionsVisitsID_ == 0){
                                    itIsDubble_ = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (itIsDubble_ == false){
                        directionsList.add(new VisitsDirectionsModel(gettedID, Constants.DIRECTIONS_VISITS_TYPE_0, specialisationsID, 0));
                        rvVisitsDirectionsAdapter.notifyDataSetChanged();
                    }



                    break;

                case Constants.NEW_DIRECTIONS_VISIT:
                    // это тот случай, когда создали новый визит-направление, и при его сохранении нужно в этом визите-основании (reference) прописать на него ссылку.

                    boolean thisIsANewDirectionsVisit = data.getBooleanExtra("thisIsANewDirectionsVisit", false);
                    if (thisIsANewDirectionsVisit == true){
                        int directionsListsPosition = data.getIntExtra("directionsListsPosition", 0); // позиция в листе направлений, в которой будем прописывать ссылку на направление.
                        VisitsDirectionsModel vdModel = directionsList.get(directionsListsPosition);
                        vdModel.directionsVisitsID = data.getIntExtra("directionsVisitID", 0);
                        fDBMethods.updateVisitsDirectionsList(gettedID, directionsList);
                        rvVisitsDirectionsAdapter.notifyDataSetChanged();
                    }

                    break;

                case Constants.NEW_DIRECTIONS_ANALISIS_VISIT:
                    // это тот случай, когда создали новый визит-направление, и при его сохранении нужно в этом визите-основании (reference) прописать на него ссылку.

                    boolean thisIsANewDirectionsVisit_ = data.getBooleanExtra("thisIsANewDirectionsVisit", false);
                    if (thisIsANewDirectionsVisit_){
                        int directionsListsPosition = data.getIntExtra("directionsListsPosition", 0); // позиция в листе направлений, в которой будем прописывать ссылку на направление.
                        VisitsDirectionsModel vdModel = directionsList.get(directionsListsPosition);
                        vdModel.directionsVisitsID = data.getIntExtra("directionsVisitID", 0);
                        fDBMethods.updateVisitsDirectionsList(gettedID, directionsList);
                        rvVisitsDirectionsAdapter.notifyDataSetChanged();
                    }

                    break;

                case Constants.MAIN_ACTIVITY_FOR_DIRECTIONS:

                    int directionsVisitsID = data.getIntExtra("choosen_directions_id", 0);   // id выбранного в z_old_MainActivity1 визита-направления
                    int directionVisitsTypesID = data.getIntExtra("directionVisitsTypesID", -1); // тип визита-направления (определили в z_old_MainActivity1)

                    // нельзя выбирать себя в качестве направления (тот же id и тот же тип визита): - НЕТ, МОЖНО!!! - пример: повторный визит
/*
                    if ((directionsVisitsID == gettedID)&(directionVisitsTypesID == Constants.DIRECTIONS_VISITS_TYPE_0)){
                        OnShowSnackbar listener = (OnShowSnackbar)getActivity();
                        listener.onShowSnackbar(getString(R.string.visits_fragment_directions_chosen_bad_visit));
                        return;
                    }
*/

                    // Это - визит-основание. И это тот случай, когда в таблице направлений мы сначала создали строчку
                    // с типом исследования или со специализацией доктора, нр пока без визита-направления
                    // и сейчас прикрепляем к ней существующий визит.
                    // Нужно не забыть сделать проверку на тип визита-направления (чтобы не прикрепили по ошибке визит-консультацию к типу анализа и наоборот)

                    // 1) Сначала обновим таблицу направлений этого визита

                    int directionsListsPosition = data.getIntExtra("directionsListsPosition", 0); // позиция в листе направлений, в которой будем прописывать ссылку на направление.
                    VisitsDirectionsModel vdModel = directionsList.get(directionsListsPosition); // получили модель - полную строку таблицы направлений
                    // проверим, совпадают ли типы и, если совпадают, то пропишем:
                    boolean typeIsEqual = true;
                    switch (directionVisitsTypesID){
                        case Constants.DIRECTIONS_VISITS_TYPE_0:
                            if (vdModel.directionsVisitsType == Constants.DIRECTIONS_ANALISIS_TYPE_1){
                                typeIsEqual = false;
                            }
                            break;
                        case Constants.DIRECTIONS_ANALISIS_TYPE_1 :
                            if (vdModel.directionsVisitsType == Constants.DIRECTIONS_VISITS_TYPE_0){
                                typeIsEqual = false;
                            }
                            break;
                    }

                    // сообщим об ошибке пользователю
                    if (!typeIsEqual){
                        OnShowSnackbar listener = (OnShowSnackbar)getActivity();
                        listener.onShowSnackbar(getString(R.string.visits_fragment_directions_chosen_different_types));
                    }

                    if (typeIsEqual){
                        vdModel.directionsVisitsID = directionsVisitsID;
                        fDBMethods.updateVisitsDirectionsList(gettedID, directionsList);
                        rvVisitsDirectionsAdapter.notifyDataSetChanged();
                    }

                    // 2) А теперь нужно прописать ссылку (reference) на этот визит как на основание в визите-направлении:
                    switch (directionVisitsTypesID){
                        case Constants.DIRECTIONS_VISITS_TYPE_0: // в z_old_MainActivity1 выбрали визит-консультацию
                            //Bundle bundle = fDBMethods.getVisitsFildsByVisitsID(directionsVisitsID);
                            //bundle.putInt("referencesID", gettedID);
                            if (typeIsEqual){
                                fDBMethods.updateVisitReferenceAndDiagnosis(directionsVisitsID, gettedID, mDiagnosisID);
                            }
                            break;
                        case Constants.DIRECTIONS_ANALISIS_TYPE_1:  // в z_old_MainActivity1 выбрали визит-исследование
                            // просто добавим строчку в табличную часть визита-исследования
                            // но сначала исключим дубли:
                            if (typeIsEqual){
                                boolean itIsDubble__ = false;   // признак полностью дублирующей записи: тип + визит-основание
                                boolean itIsDubbleTypeWhithoutBaseVisit = false; // признак дублирующего типа без визита-основания
                                int dubbleTypeWhithoutBaseVisitsRowID = -1;
                                Cursor cursor = fDBMethods.getAllAnalisisVisitsAnalisesList(directionsVisitsID);
                                if (cursor.getCount() > 0){
                                    cursor.moveToFirst();
                                    do {
                                        int rowID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList._ID));
                                        int currentAnalisisTypesID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID));
                                        int currentReferencesVisitsID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList.COLUMN_REFERENCES_VISITS_ID));

                                        if (currentAnalisisTypesID == vdModel.anTypeOrSpecID){
                                            if (currentReferencesVisitsID == gettedID){
                                                itIsDubble__ = true;
                                            }else {
                                                itIsDubbleTypeWhithoutBaseVisit = true; // есть дублирующий тип без основания. Можно ссылку записать на него. Пока запомним номер строки.
                                                dubbleTypeWhithoutBaseVisitsRowID = rowID;
                                            }
                                        }
                                    }while (cursor.moveToNext());
                                }

                                // будем делать запись только при условии, что нет полного дубля
                                if (!itIsDubble__) {
                                    if (itIsDubbleTypeWhithoutBaseVisit){
                                        // если есть дубль по типу, то update-тим его через удаление старой строки и вставки новой:
                                        fDBMethods.deleteAnalisisVisitsAnalisesList(dubbleTypeWhithoutBaseVisitsRowID);
                                        fDBMethods.insertRecordAnalisisVisitsAnalisesList(directionsVisitsID, vdModel.anTypeOrSpecID, gettedID);
                                    }else {
                                        fDBMethods.insertRecordAnalisisVisitsAnalisesList(directionsVisitsID, vdModel.anTypeOrSpecID, gettedID);
                                    }
                                }
                            }
                            break;
                    }
                    break;
            }

            rv.smoothScrollToPosition(rv.getAdapter().getItemCount()-1);

            //ArrayList<MVisitDirectionsFragment.VisitsDirectionsModel> directionsList = fragmentVisitMdirections.directionsList;
            if ((directionsList != null) && (directionsList.size() > 0)) {
                fDBMethods.updateVisitsDirectionsList(gettedID, directionsList);
            }

        }
        if (resultCode == getActivity().RESULT_CANCELED) {
            if (data != null){
                // тот случай, когда открыли новое направление на исследование, но передумали и хотят закрыть без сохранения. Сделаем это:
                Boolean thisIsANewDirectionsVisit = data.getBooleanExtra("thisIsANewDirectionsVisit", false);
                if(thisIsANewDirectionsVisit){
                    int directionsID = data.getIntExtra("currentItemID", -1);
                    if (directionsID != -1){
                        // удалим только что созданный визит-исследование и (вначале) его таблицу:
                        fDBMethods.deleteAnalisisVisitsAnalisesList(directionsID);
                        fDBMethods.deleteAnalisisVisit(directionsID);
                    }
                }
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        //Bundle bundle = getArguments().getBundle("gettedArgs");
        //gettedID = bundle.getInt("gettedID", 0);
        OnGetVisitsID listener = (OnGetVisitsID)getActivity();
        gettedID = listener.onGetVisitsID();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fDBMethods != null) {
            fDBMethods.close();
        }
    }

    public class VisitsDirectionsModel {

        int basicVisitsID; // визит, на котором выдано направление (т.е. этот визит)
        public int directionsVisitsType; // тип визита направления - на исследование или на консультацию
        public int anTypeOrSpecID;       // id  исследования или специализации доктора
        public int directionsVisitsID;   // id визита-направления

        ArrayList<VisitsDirectionsModel>directionsList;

        VisitsDirectionsModel(){
            this.directionsList = new ArrayList<>();
        }

        VisitsDirectionsModel(int basicVisitsID, int directionsVisitsType, int anTypeOrSpecID, int directionsVisitsID){
            this.basicVisitsID = basicVisitsID;
            this.directionsVisitsType = directionsVisitsType;
            this.anTypeOrSpecID = anTypeOrSpecID;
            //this.directionsTitle = directionsTitle;
            this.directionsVisitsID = directionsVisitsID;
        }

        public void add(int basicVisitsID, int directionsVisitsTypes, int anTypeOrSpecID, int directionsVisitsID){
            directionsList.add(new VisitsDirectionsModel(basicVisitsID, directionsVisitsTypes, anTypeOrSpecID, directionsVisitsID));
        }

    }

    public class visitsDirectionsRVAdapter extends RecyclerView.Adapter<visitsDirectionsRVAdapter.DirectionsListViewHolders> {

        //private final int ANALISES_TYPES = 1000;
        //private final int DIRECTIONS_VISIT = 1001;


        ArrayList<VisitsDirectionsModel> directionsList;

        visitsDirectionsRVAdapter(ArrayList<VisitsDirectionsModel> directionsList) {
            this.directionsList = directionsList;
        }

        @Override
        public DirectionsListViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_for_visit_directions_list_recycled_list, null);
            DirectionsListViewHolders directionsListVH = new DirectionsListViewHolders(v);

            return directionsListVH;
        }

        @Override
        public void onBindViewHolder(DirectionsListViewHolders holder, int position) {
            holder.directionsModel = directionsList.get(position);
            if (holder.directionsModel.directionsVisitsType == Constants.DIRECTIONS_ANALISIS_TYPE_1) {
                String curDirectionsTitle = fDBMethods.getAnalisesTypesNameByID(holder.directionsModel.anTypeOrSpecID);
                holder.tv_VisitsDirectionsTitle.setText(curDirectionsTitle);
                // представление визита
                int curDirVisitID = holder.directionsModel.directionsVisitsID;
                if (curDirVisitID != 0) {
                    String curDirectionsVisitsView = fDBMethods.getAnalisisVisitsViewByID(curDirVisitID);
                    holder.tv_FragmentDirections_VisitsView.setText(curDirectionsVisitsView);

                    holder.btn_browse_item_DirectionsVisit.setVisibility(View.VISIBLE);
                    holder.btn_add_del_item_Visits.setImageResource(R.mipmap.btn_cancel_black);
                } else {
                    holder.btn_browse_item_DirectionsVisit.setVisibility(View.GONE);
                    holder.btn_add_del_item_Visits.setImageResource(R.mipmap.btn_add_black);
                    holder.tv_FragmentDirections_VisitsView.setHint(R.string.visits_fragment_directions_list_hint_analisis_visit);
                }


            } else {
                String curDirectionsTitle = fDBMethods.getSpecializationsNameByID(holder.directionsModel.anTypeOrSpecID);
                holder.tv_VisitsDirectionsTitle.setText(curDirectionsTitle);
                // представление визита
                int curDirVisitID = holder.directionsModel.directionsVisitsID;
                if (curDirVisitID != 0) {
                    String curDirectionsVisitsView = fDBMethods.getVisitsViewByID(curDirVisitID);
                    holder.tv_FragmentDirections_VisitsView.setText(curDirectionsVisitsView);

                    holder.btn_browse_item_DirectionsVisit.setVisibility(View.VISIBLE);
                    holder.btn_add_del_item_Visits.setImageResource(R.mipmap.btn_cancel_black);
                } else {
                    holder.tv_FragmentDirections_VisitsView.setText("");
                    holder.btn_browse_item_DirectionsVisit.setVisibility(View.GONE);
                    holder.btn_add_del_item_Visits.setImageResource(R.mipmap.btn_add_black);
                    holder.tv_FragmentDirections_VisitsView.setHint(R.string.visits_fragment_directions_list_hint_visit);
                }

            }
        }


/*
        @Override
        public int getItemViewType(int position) {
            // условие для определения айтем какого типа выводить в конкретной позиции
            int directionsVisitsType = directionsList.get(position).directionsVisitsType;

            if (directionsVisitsType == Constants.DIRECTIONS_ANALISIS_TYPE_1){
                return ANALISES_TYPES;
            }else return DIRECTIONS_VISIT;
        }

*/

        @Override
        public int getItemCount() {
            return directionsList.size();
        }

        public class DirectionsListViewHolders extends RecyclerView.ViewHolder {

            TextView tv_VisitsDirectionsTitle;
            TextView tv_FragmentDirections_VisitsView;
            ImageButton btn_browse_item_DirectionsVisit;
            ImageButton btn_add_del_item_Visits;
            ImageButton btn_Visists_DirectionsItem;

            VisitsDirectionsModel directionsModel;


            public DirectionsListViewHolders(View itemView) {
                super(itemView);
                tv_VisitsDirectionsTitle = (TextView) itemView.findViewById(R.id.tv_VisitsDirectionsTitle);
                tv_FragmentDirections_VisitsView = (TextView) itemView.findViewById(R.id.tv_FragmentDirections_VisitsView);

                btn_browse_item_DirectionsVisit = (ImageButton) itemView.findViewById(R.id.btn_browse_item_DirectionsVisit);
                btn_add_del_item_Visits = (ImageButton) itemView.findViewById(R.id.btn_add_del_DirectionsVisit);
                btn_Visists_DirectionsItem = (ImageButton) itemView.findViewById(R.id.btn_Visists_DirectionsItem);

                btn_Visists_DirectionsItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (directionsModel.directionsVisitsID == 0) {
                            // нет визита-направления, просто удаляем из списка
                            directionsList.remove(directionsModel);
                            rvVisitsDirectionsAdapter.notifyDataSetChanged();

                            //ArrayList<MVisitDirectionsFragment.VisitsDirectionsModel> directionsList = fragmentVisitMdirections.directionsList;
                            //if ((directionsList != null) && (directionsList.size() > 0)) {
                            if (directionsList != null) {
                                fDBMethods.updateVisitsDirectionsList(gettedID, directionsList);
                            }

                        } else {
                            // еще предупредить и удалить все: запись в таблице направлений (если есть), ссылку в визите направлении и почистить интерфейс
                            // спросим, точно ли хотят удалить
                            String title = getResources().getString(R.string.deleting_visit_question_mini);
                            //String message = getResources().getString(R.string.deleting_visit_question_mini);
                            String btnPositiveString = getResources().getString(R.string.edit_profiles_delete_profile_positive_button_text);
                            String btnNegativeString = getResources().getString(R.string.edit_profiles_delete_profile_negative_button_text);

                            AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
                            ad.setTitle(title);  // заголовок
                            //ad.setMessage(message); // сообщение
                            ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int arg1) {
                                    // удалим в БД - в таблице направлений и в реквизите самого визита-направления (делать так будем всегда, когда есть вопрос к пользователю)
                                    // в зависимости от разных типов визитов
                                    switch (directionsModel.directionsVisitsType){
                                        case Constants.DIRECTIONS_VISITS_TYPE_0:
                                            fDBMethods.updateVisitReferenceAndDiagnosis(directionsModel.directionsVisitsID, 0, mDiagnosisID);
                                            // для удаления найдем нужную строку в таблице направлений, получим ее id и дальше удалим ее из таблицы:
                                            int deletingRowID = -1;
                                            Cursor cursor = fDBMethods.getAllVisitsDirectionsList(gettedID);
                                            if (cursor.getCount()>0){
                                                cursor.moveToFirst();
                                                do{
                                                    int rowID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsDirections._ID));
                                                    int directionsVisitsID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID));
                                                    if (directionsVisitsID == directionsModel.directionsVisitsID){
                                                        deletingRowID = rowID;
                                                        break;
                                                    }
                                                }while (cursor.moveToNext());
                                            }
                                            cursor.close();
                                            if (deletingRowID != -1)fDBMethods.deleteVisitsDirection(deletingRowID);

                                            // теперь удалим визуально в пользовательском интерфейсе
                                            directionsList.remove(directionsModel);
                                            rvVisitsDirectionsAdapter.notifyDataSetChanged();

                                            break;

                                        case Constants.DIRECTIONS_ANALISIS_TYPE_1:
                                            // сначала найдем нужную строку в таблице исследоваий визита-исследования, получим ее id и дальше удалим ее из таблицы:
                                            int deleting_avList_RowID = -1;
                                            Cursor cursorAV = fDBMethods.getAllAnalisisVisitsAnalisesList(directionsModel.directionsVisitsID);
                                            if (cursorAV.getCount()>0){
                                                cursorAV.moveToFirst();
                                                do{
                                                    int rowID = cursorAV.getInt(cursorAV.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList._ID));
                                                    int anTypesID = cursorAV.getInt(cursorAV.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID));
                                                    int refAnVisitsID = cursorAV.getInt(cursorAV.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList.COLUMN_REFERENCES_VISITS_ID));
                                                    if (refAnVisitsID == gettedID){
                                                        if (anTypesID == directionsModel.anTypeOrSpecID){
                                                            deleting_avList_RowID = rowID;
                                                            break;
                                                        }
                                                    }
                                                }while (cursorAV.moveToNext());
                                            }
                                            cursorAV.close();
                                            if (deleting_avList_RowID != -1){
                                                fDBMethods.deleteAnalisisVisitsAnalisesList(deleting_avList_RowID);
                                            }

                                            // теперь почистим все таблице направлений визита-основания
                                            // для удаления найдем нужную строку в таблице направлений, получим ее id и дальше удалим ее из таблицы:
                                            int deleting_av_RowID = -1;
                                            Cursor cursorD = fDBMethods.getAllVisitsDirectionsList(gettedID);
                                            if (cursorD.getCount()>0){
                                                cursorD.moveToFirst();
                                                do{
                                                    int rowID = cursorD.getInt(cursorD.getColumnIndex(DBMethods.TableVisitsDirections._ID));
                                                    int directionsVisitsID = cursorD.getInt(cursorD.getColumnIndex(DBMethods.TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID));
                                                    if (directionsVisitsID == directionsModel.directionsVisitsID){
                                                        deleting_av_RowID = rowID;
                                                        break;
                                                    }
                                                }while (cursorD.moveToNext());
                                            }
                                            cursorD.close();
                                            if (deleting_av_RowID != -1)fDBMethods.deleteVisitsDirection(deleting_av_RowID);

                                            // теперь удалим визуально в пользовательском интерфейсе
                                            directionsList.remove(directionsModel);
                                            rvVisitsDirectionsAdapter.notifyDataSetChanged();

                                            break;
                                    }

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

                    }
                });

                // добавим (или удалим) визит
                btn_add_del_item_Visits.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // сначала проверим, есть ли визит-направление:
                        if (directionsModel.directionsVisitsID == 0){ // направления нет, нужно добавить:
                            // создадим еще диалог выбора: новый визит создавать или выбрать уже существующий:
                            FragmentManager manager = getActivity().getSupportFragmentManager();
                            //Bundle bundle = new Bundle();
                            //bundle.getInt("directionsVisitsType", directionsModel.directionsVisitsType);
                            //bundle.getInt("anTypeOrSpecID", directionsModel.anTypeOrSpecID);
                            //bundle.getInt("directionsListsPosition", directionsList.indexOf(directionsModel));
                            DialogFragment myDialogFragment = VisitMdirectionsFragmentDialogFragment.newInstance(directionsModel.directionsVisitsType, directionsModel.anTypeOrSpecID, directionsList.indexOf(directionsModel)); //  <-- внутренний класс
                            //VisitMdirectionsFragmentDialogFragment myDialogFragment = new VisitMdirectionsFragmentDialogFragment(directionsModel, directionsList.indexOf(directionsModel)); //  <-- внутренний класс
                            myDialogFragment.show(manager, "dialog");
                        }else { // направление есть, значит нажали, чтобы удалить (отвязать)

                            // спросим, точно ли хотят удалить (отвязать)
                            String title = getResources().getString(R.string.visits_fragment_directions_unlink_question);
                            //String message = getResources().getString(R.string.deleting_visit_question_mini);
                            String btnPositiveString = getResources().getString(R.string.edit_profiles_delete_profile_positive_button_text);
                            String btnNegativeString = getResources().getString(R.string.edit_profiles_delete_profile_negative_button_text);

                            AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
                            ad.setTitle(title);  // заголовок
                            //ad.setMessage(message); // сообщение
                            ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int arg1) {
                                    // нажали "Да, хотим удалить"
                                    int curDirectionsVisitsID = directionsModel.directionsVisitsID;
                                    // сначала удалим визит-направление из таблицы направлений этого визита:
                                    // для удаления (отвязывания) найдем нужную строку в таблице направлений, получим ее id и дальше удалим ее из таблицы:
                                    int deletingRowID = -1;
                                    Cursor cursor = fDBMethods.getAllVisitsDirectionsList(gettedID);
                                    if (cursor.getCount()>0){
                                        cursor.moveToFirst();
                                        do{
                                            int rowID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsDirections._ID));
                                            int directionsVisitsType = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsDirections.COLUMN_DIRECTION_VISITS_TYPE));
                                            int anTypeOrSpecID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsDirections.COLUMN_AN_TYPES_SPECIALISATIONS_ID));
                                            int directionsVisitsID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsDirections.COLUMN_DIRECTION_VISITS_ID));
                                            if ((directionsVisitsType == directionsModel.directionsVisitsType)&
                                                    (anTypeOrSpecID == directionsModel.anTypeOrSpecID)&
                                                    (directionsVisitsID == directionsModel.directionsVisitsID)){
                                                deletingRowID = rowID;
                                                break;
                                            }
                                        }while (cursor.moveToNext());
                                    }
                                    cursor.close();
                                    if (deletingRowID != -1){
                                        fDBMethods.deleteVisitsDirection(deletingRowID);
                                        fDBMethods.insertRecordVisitsDirectionsList(gettedID, directionsModel.directionsVisitsType, directionsModel.anTypeOrSpecID, 0);
                                    }

                                    // теперь удалим (отвяжем) визуально в пользовательском интерфейсе этого визита
                                    tv_FragmentDirections_VisitsView.setText("");
                                    btn_add_del_item_Visits.setImageResource(R.mipmap.btn_add_black);
                                    btn_browse_item_DirectionsVisit.setVisibility(View.GONE);
                                    directionsModel.directionsVisitsID = 0;
                                    directionsList.set(directionsList.indexOf(directionsModel),directionsModel);

                                    // теперь сделаем нужное отвязывание от этого визита-основания в визите направлении, т.е. удалим строку:
                                    switch (directionsModel.directionsVisitsType){
                                        case Constants.DIRECTIONS_ANALISIS_TYPE_1:

                                            int deleting_avList_RowID = -1;
                                            Cursor cursorAV = fDBMethods.getAllAnalisisVisitsAnalisesList(curDirectionsVisitsID);
                                            if (cursorAV.getCount()>0){
                                                cursorAV.moveToFirst();
                                                do{
                                                    int rowID = cursorAV.getInt(cursorAV.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList._ID));
                                                    int anTypesID = cursorAV.getInt(cursorAV.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID));
                                                    int refAnVisitsID = cursorAV.getInt(cursorAV.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList.COLUMN_REFERENCES_VISITS_ID));
                                                    if (refAnVisitsID == gettedID){
                                                        if (anTypesID == directionsModel.anTypeOrSpecID){
                                                            deleting_avList_RowID = rowID;
                                                            break;
                                                        }
                                                    }
                                                }while (cursorAV.moveToNext());
                                            }
                                            cursorAV.close();
                                            if (deleting_avList_RowID != -1){
                                                fDBMethods.deleteAnalisisVisitsAnalisesList(deleting_avList_RowID);
                                            }

                                            tv_FragmentDirections_VisitsView.setHint(R.string.visits_fragment_directions_list_hint_analisis_visit);
                                            break;
                                        case Constants.DIRECTIONS_VISITS_TYPE_0:
                                            // удалим в БД - в таблице направлений и в реквизите самого визита-направления (делать так будем всегда, когда есть вопрос к пользователю)
                                            fDBMethods.updateVisitReferenceAndDiagnosis(curDirectionsVisitsID, 0, mDiagnosisID);

                                            tv_FragmentDirections_VisitsView.setHint(R.string.visits_fragment_directions_list_hint_visit);

                                            break;
                                    }

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
                    }
                });

                // просмотрим визит-направление
                btn_browse_item_DirectionsVisit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (directionsModel.directionsVisitsID != 0){
                            switch (directionsModel.directionsVisitsType){
                                case Constants.DIRECTIONS_ANALISIS_TYPE_1:
                                    Intent intentAV = new Intent(getContext(), MAnalisisVisitActivity.class);
                                    intentAV.putExtra("choosen_visits_id", directionsModel.directionsVisitsID);
                                    intentAV.putExtra("currentProfileID", mCurrentProfileID);
                                    startActivity(intentAV);
                                    break;
                                case Constants.DIRECTIONS_VISITS_TYPE_0:
                                    Intent intent = new Intent(getContext(), MVisitActivity.class);
                                    intent.putExtra("choosen_visits_id", directionsModel.directionsVisitsID);
                                    intent.putExtra("currentProfileID", mCurrentProfileID);
                                    startActivity(intent);
                                    break;
                            }
                        }
                    }
                });

            }


        }
    }

    public static class VisitMdirectionsFragmentDialogFragment extends DialogFragment {

        //VisitsDirectionsModel directionsModel;
        //int directionsListsPosition;

        public VisitMdirectionsFragmentDialogFragment() {
            // Required empty public constructor
        }

/*
        VisitMdirectionsFragmentDialogFragment(VisitsDirectionsModel directionsModel, int directionsListsPosition){
            this.directionsModel = directionsModel;
            this.directionsListsPosition = directionsListsPosition;
        }
*/

        public static VisitMdirectionsFragmentDialogFragment newInstance(int directionsVisitsType, int anTypeOrSpecID, int directionsListsPosition) {
            VisitMdirectionsFragmentDialogFragment frag = new VisitMdirectionsFragmentDialogFragment();
            Bundle args = new Bundle();
            args.putInt("directionsVisitsType", directionsVisitsType);
            args.putInt("anTypeOrSpecID", anTypeOrSpecID);
            args.putInt("directionsListsPosition", directionsListsPosition);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final String messageTitle = getActivity().getString(R.string.visits_fragment_directions_add_visit_dialog_title);
            final String newVisit = getActivity().getString(R.string.item_directions_new);      // Новый визит
            final String existentVisit = getActivity().getString(R.string.item_directions_choose);  // Выбрать существующий визит

            final String[] visitsArray = {newVisit, existentVisit};

            final int directionsVisitsType = getArguments().getInt("directionsVisitsType");
            final int anTypeOrSpecID = getArguments().getInt("anTypeOrSpecID");
            final int directionsListsPosition = getArguments().getInt("directionsListsPosition");

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
            builder.setTitle(messageTitle)
                    .setItems(visitsArray, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            switch (which){
                                case 0:  // новый визит
                                    Bundle bundle = new Bundle();
                                    switch (directionsVisitsType){
                                        case Constants.DIRECTIONS_ANALISIS_TYPE_1: // новый визит-исследование
                                            // отсюда никак не получается вызвать onActivityResult, поэтому перейдем в процедуру вне класса
                                            bundle.putInt("directAnalisisTypeID", anTypeOrSpecID);
                                            bundle.putInt("directionsListsPosition", directionsListsPosition);
                                            ((MVisitActivity)getActivity()).fragmentVisitMdirections.openActivityForResult(true, Constants.DIRECTIONS_ANALISIS_TYPE_1, bundle);
                                            break;
                                        case Constants.DIRECTIONS_VISITS_TYPE_0:  // новый визит-консультация
                                            // отсюда никак не получается вызвать onActivityResult, поэтому перейдем в процедуру вне класса
                                            bundle.putInt("directSpecialisationsID", anTypeOrSpecID);
                                            bundle.putInt("directionsListsPosition", directionsListsPosition);
                                            ((MVisitActivity)getActivity()).fragmentVisitMdirections.openActivityForResult(true, Constants.DIRECTIONS_VISITS_TYPE_0, bundle);
                                            break;
                                    }
                                    break;
                                case 1:  // существующий визит
                                    Bundle bundle1 = new Bundle();
                                    //bundle1.putBoolean("getExistsDirection", true);
                                    bundle1.putInt("directionsListsPosition", directionsListsPosition);
                                    ((MVisitActivity)getActivity()).fragmentVisitMdirections.openActivityForResult(false, 0, bundle1);
                                    break;
                            }
                        }
                    });

            return builder.create();
        }
    }

    public void openActivityForResult(boolean itIsAnewVisitsType, int newVisitsType, Bundle bundle){

        MVisitActivity MVisitActivity = (MVisitActivity)getActivity();
        if (itIsAnewVisitsType == true){
            switch (newVisitsType){
                case Constants.DIRECTIONS_ANALISIS_TYPE_1:
                    Intent intentAV = new Intent(getContext(), MAnalisisVisitActivity.class);
                    intentAV.putExtra("choosen_visits_id", 0); // т.е. при открытии поймем, что это новый визит
                    intentAV.putExtra("referenceID", gettedID); // отправим ему ссылку на текущий, как на основание
                    intentAV.putExtra("currentProfileID", MVisitActivity.mCurrentProfileID);
                    intentAV.putExtra("directAnalisisTypeID", bundle.getInt("directAnalisisTypeID", 0));
                    intentAV.putExtra("directionsListsPosition", bundle.getInt("directionsListsPosition", 0));
                    startActivityForResult(intentAV, Constants.NEW_DIRECTIONS_ANALISIS_VISIT);
                    break;
                case Constants.DIRECTIONS_VISITS_TYPE_0:
                    Intent intent = new Intent(getContext(), MVisitActivity.class);
                    intent.putExtra("choosen_visits_id", 0); // т.е. при открытии поймем, что это новый визит
                    intent.putExtra("referenceID", gettedID); // отправим ему ссылку на текущий, как на основание
                    intent.putExtra("diagnosisID", MVisitActivity.mDiagnosisID); // отправим ему также ссылку на диагноз
                    intent.putExtra("currentProfileID", MVisitActivity.mCurrentProfileID);
                    intent.putExtra("directSpecialisationsID", bundle.getInt("directSpecialisationsID", 0));
                    intent.putExtra("directionsListsPosition", bundle.getInt("directionsListsPosition", 0));
                    startActivityForResult(intent, Constants.NEW_DIRECTIONS_VISIT);
                    break;

            }
        }else {
            Intent intentM = new Intent(getContext(), MCardMainActivity.class);
            intentM.putExtra("getExistsDirection", true);
            intentM.putExtra("directionsListsPosition", bundle.getInt("directionsListsPosition", 0));
            startActivityForResult(intentM, Constants.MAIN_ACTIVITY_FOR_DIRECTIONS);
        }

    }

    public interface OnGetSaveVisitCommand{
        int onGetSaveVisitCommand();
    }

    public interface OnGetVisitsID{
        int onGetVisitsID();
    }


    public interface OnShowSnackbar{
        int onShowSnackbar(String message);
    }


/*
    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int title) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");

            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.alert_dialog_icon)
                    .setTitle(title)
                    .setPositiveButton(R.string.alert_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((MVisitActivity)getActivity()).fragmentVisitMdirections.doPositiveClick();
                                }
                            }
                    )
                    .setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //((MVisitActivity)getActivity()).doNegativeClick();
                                    ((MVisitActivity)getActivity()).fragmentVisitMdirections.doNegativeClick();
                                }
                            }
                    )
                    .create();
        }
    }

    void showDialog() {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(
                R.string.alert_dialog_two_buttons_title);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void doPositiveClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Positive click!");
    }

    public void doNegativeClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Negative click!");
    }
*/


}
