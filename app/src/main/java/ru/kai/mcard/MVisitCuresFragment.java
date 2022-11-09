package ru.kai.mcard;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MVisitCuresFragment extends Fragment {

    private DBMethods fDBMethods;
    int gettedID;
    int mDiagnosisID;

    FloatingActionButton fabAddVisitsCures;

    public ArrayList<VisitsCuresModel> curesList;
    VisitsCuresRVAdapter rvVisitsCuresAdapter;

    // настройки
    private SharedPreferences mSettings;
    private int showVisitsCuresHelp = 1;
    TextView visits_fragment_сures_list_help;


    public MVisitCuresFragment() {
        // Required empty public constructor
    }

    public static MVisitCuresFragment newInstance(Bundle bundle) {
        MVisitCuresFragment currentFragment = new MVisitCuresFragment();
        Bundle args = new Bundle();
        args.putBundle("gettedArgs", bundle);
        currentFragment.setArguments(args);
        //currentFragment.curesList = new ArrayList<>();
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
        View rootView = inflater.inflate(R.layout.fragment_visit_m_cures, container, false);

        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.rvVisitsFragmentCures);
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
        }

        curesList = new ArrayList<>();
        if (gettedID > 0){
            Cursor cursor = fDBMethods.getAllVisitsCures(gettedID);
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                do {
                    int rowID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsCures._ID));
                    int visitsCuresID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsCures.COLUMN_CURES_ID));
                    String visitsCuresDescr = cursor.getString(cursor.getColumnIndex(DBMethods.TableVisitsCures.COLUMN_CURES_DESCR));
                    //int curesExecutionsListsID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsCures.COLUMN_PROCEDURES_VISITS_ID));
                    int curesExecutionsListsID = 0;

                    curesList.add(new VisitsCuresModel(gettedID, visitsCuresID, visitsCuresDescr, curesExecutionsListsID));

                }while (cursor.moveToNext());
            }
            cursor.close();
        }

        rvVisitsCuresAdapter = new VisitsCuresRVAdapter(curesList);
        rv.setAdapter(rvVisitsCuresAdapter);

        // не показывать обучающую информацию
        visits_fragment_сures_list_help = (TextView)rootView.findViewById(R.id.visits_fragment_сures_list_help);

        mSettings = getContext().getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(Constants.APP_PREFERENCES_SHOW_VISITS_CURES_HELP)) {
            // Получаем число из настроек
            showVisitsCuresHelp = mSettings.getInt(Constants.APP_PREFERENCES_SHOW_VISITS_CURES_HELP, 1);
            if (showVisitsCuresHelp == 0){
                visits_fragment_сures_list_help.setVisibility(View.GONE);
            }
        }

        visits_fragment_сures_list_help.setOnClickListener(new View.OnClickListener() {
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
                        editor.putInt(Constants.APP_PREFERENCES_SHOW_VISITS_CURES_HELP, 0);
                        editor.apply();
                        visits_fragment_сures_list_help.setVisibility(View.GONE);
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
        });

        fabAddVisitsCures = (FloatingActionButton) rootView.findViewById(R.id.fabAddVisitsCures);
        fabAddVisitsCures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gettedID > 0){
                    Intent intent = new Intent(getContext(), MVisitsCureActivity.class);
                    startActivityForResult(intent, Constants.VISITS_CURE_DATA);
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

                            Intent intent = new Intent(getContext(), MVisitsCureActivity.class);
                            startActivityForResult(intent, Constants.VISITS_CURE_DATA);
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
    }

    @Override  // получаем ответ от открытых ранее активностей
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            switch (requestCode) {
                case Constants.VISITS_CURE_DATA:
                    // если выбрали вид исследования

                    Bundle bundle = data.getBundleExtra("choosen_visits_cure_data");
                    int currentCuresListPosition = bundle.getInt("currentCuresListPosition", -1);
                    int basicVisitsID = bundle.getInt("basicVisitsID", -1);
                    int choosenCuresID = bundle.getInt("visitsCuresID", -1);
                    String choosenCuresDescr = bundle.getString("visitsCuresDescr");
                    // если отправляли для изменения, то известна позиция строки; просто заменим :
                    if (currentCuresListPosition > -1){
                        VisitsCuresModel visitsCuresModel = curesList.get(currentCuresListPosition);
                        visitsCuresModel.basicVisitsID = basicVisitsID;
                        visitsCuresModel.visitsCuresID = choosenCuresID;
                        visitsCuresModel.visitsCuresDescr = choosenCuresDescr;
                        curesList.set(currentCuresListPosition, visitsCuresModel);
                        rvVisitsCuresAdapter.notifyDataSetChanged();
                    }else {

                        // прежде, чем добавить, проверим не содержится ли уже исследование в списке (проверка по analisisTypesID и по directionsVisitsID)
                        // то есть одинаковое исследование может повторяться в списке только если его назначили на разных визитах-консультациях.
                        int currentCuresID = 0;
                        int i;
                        boolean itIsDubble = false;
                        for (i = 0; i < curesList.size(); i++) {
                            VisitsCuresModel itemModel = curesList.get(i);
                            currentCuresID = itemModel.visitsCuresID;

                            if (currentCuresID == choosenCuresID) {
                                itIsDubble = true;
                                break;
                            }
                        }
                        if (itIsDubble == false) {
                            curesList.add(new VisitsCuresModel(gettedID, choosenCuresID, choosenCuresDescr, 0));
                            rvVisitsCuresAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
            }
            //ArrayList<MVisitCuresFragment.VisitsCuresModel> curesList = fragmentVisitMcures.curesList;
            if ((curesList != null) && (curesList.size() > 0)) {
                fDBMethods.updateVisitsCuresList(gettedID, curesList);
            }
        }

    }

    public class VisitsCuresModel {

        public int basicVisitsID; // визит, на котором выдано назначение (т.е. этот визит)
        public int visitsCuresID; // id назначения
        public String visitsCuresDescr; // описание назначения
        public int curesExecutionsListsID; // лист визитов, на которых будет выполняться, производиться процедура-назначение

        ArrayList<VisitsCuresModel>curesList;

        VisitsCuresModel(){
            this.curesList = new ArrayList<>();
        }

        VisitsCuresModel(int basicVisitsID, int visitsCuresID, String visitsCuresDescr, int curesExecutionsListsID){
            this.basicVisitsID = basicVisitsID;
            this.visitsCuresID = visitsCuresID;
            this.visitsCuresDescr = visitsCuresDescr;
            this.curesExecutionsListsID = curesExecutionsListsID;
        }

        public void add(int basicVisitsID, int visitsCuresID, String visitsCuresDescr, int curesVisitsID){
            curesList.add(new VisitsCuresModel(basicVisitsID, visitsCuresID, visitsCuresDescr, curesVisitsID));
        }

    }

    public class VisitsCuresRVAdapter extends RecyclerView.Adapter<VisitsCuresRVAdapter.CuresListViewHolders> {

        //private final int ANALISES_TYPES = 1000;
        //private final int DIRECTIONS_VISIT = 1001;


        ArrayList<VisitsCuresModel> curesList;

        VisitsCuresRVAdapter(ArrayList<VisitsCuresModel> curesList) {
            this.curesList = curesList;
        }

        @Override
        public CuresListViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_for_visit_cures_list_recycled_list, null);
            CuresListViewHolders curesListVH = new CuresListViewHolders(v);

            return curesListVH;
        }

        @Override
        public void onBindViewHolder(CuresListViewHolders holder, int position) {
            holder.curesModel = curesList.get(position);
                String currentCuresTitle = fDBMethods.getCuresNameByID(holder.curesModel.visitsCuresID);
                holder.tv_VisitsCuresTitle.setText(currentCuresTitle);
                // представление визита
                int currentCuresExecutionsListsID = holder.curesModel.curesExecutionsListsID;
                if (currentCuresExecutionsListsID != 0) {
/*
                    String currentCuresVisitsView = fDBMethods.getCuresVisitsViewByID(currentCuresVisitsID);
                    holder.tv_FragmentCures_VisitsView.setText(currentCuresVisitsView);

                    holder.btn_browse_item_CuresVisit.setVisibility(View.VISIBLE);
                    holder.btn_edit_item_CuresVisit.setImageResource(R.mipmap.btn_cancel_black);
*/
                    // пока все сделаем невидимым:
                    holder.btn_browse_item_CuresVisit.setVisibility(View.GONE);
                    holder.btn_edit_item_CuresVisit.setVisibility(View.GONE);
                    holder.tv_FragmentCures_VisitsView.setVisibility(View.GONE);

                } else {
                    holder.btn_browse_item_CuresVisit.setVisibility(View.GONE);
                    //holder.btn_edit_item_CuresVisit.setImageResource(R.mipmap.btn_add_black);
                    //holder.tv_FragmentCures_VisitsView.setHint(R.string.visits_fragment_cures_list_hint_prosedures_visit);
                    //holder.btn_edit_item_CuresVisit.setVisibility(View.GONE);
                    //holder.tv_FragmentCures_VisitsView.setVisibility(View.GONE);
                    holder.tv_FragmentCures_VisitsView.setText(holder.curesModel.visitsCuresDescr);
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
            return curesList.size();
        }

        public class CuresListViewHolders extends RecyclerView.ViewHolder {

            TextView tv_VisitsCuresTitle;
            TextView tv_FragmentCures_VisitsView;
            ImageButton btn_browse_item_CuresVisit;
            ImageButton btn_edit_item_CuresVisit;
            ImageButton btn_Visists_CuresItem;

            VisitsCuresModel curesModel;


            public CuresListViewHolders(View itemView) {
                super(itemView);
                tv_VisitsCuresTitle = (TextView) itemView.findViewById(R.id.tv_VisitsCuresTitle);
                tv_FragmentCures_VisitsView = (TextView) itemView.findViewById(R.id.tv_FragmentCures_VisitsView);

                btn_browse_item_CuresVisit = (ImageButton) itemView.findViewById(R.id.btn_browse_item_CuresVisit);
                btn_edit_item_CuresVisit = (ImageButton) itemView.findViewById(R.id.btn_edit_CuresVisit);
                btn_Visists_CuresItem = (ImageButton) itemView.findViewById(R.id.btn_Visists_CuresItem);

                // большая кнопка
                btn_Visists_CuresItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (curesModel.curesExecutionsListsID == 0) {
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
                                    // нет листа визитов, выполяющих назначение, (графика приема лекарств) просто удаляем из списка
                                    curesList.remove(curesModel);
                                    rvVisitsCuresAdapter.notifyDataSetChanged();

                                    //ArrayList<MVisitCuresFragment.VisitsCuresModel> curesList = fragmentVisitMcures.curesList;
                                    if (curesList != null) {
                                        fDBMethods.updateVisitsCuresList(gettedID, curesList);
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

                // изменим назначение визита
                btn_edit_item_CuresVisit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // текущая строка:
                        int currentCuresListPosition = curesList.indexOf(curesModel);
                        Bundle bundle = new Bundle();
                        bundle.putInt("currentCuresListPosition", currentCuresListPosition);
                        bundle.putInt("basicVisitsID", curesModel.basicVisitsID);
                        bundle.putInt("visitsCuresID", curesModel.visitsCuresID);
                        bundle.putString("visitsCuresDescr", curesModel.visitsCuresDescr);

                        Intent intent = new Intent(getContext(), MVisitsCureActivity.class);
                        intent.putExtra("visitsCuresModel", bundle);
                        //intent.putExtra("currentCuresListPosition", currentCuresListPosition);
                        //intent.putExtra("visitsCuresID", curesModel.visitsCuresID);
                        startActivityForResult(intent, Constants.VISITS_CURE_DATA);

/*
                        // сначала проверим, есть ли визит-направление:
                        if (directionsModel.directionsVisitsID == 0){ // направления нет, нужно добавить:
                            // создадим еще диалог выбора: новый визит создавать или выбрать уже существующий:
                            FragmentManager manager = getActivity().getSupportFragmentManager();
                            VisitMdirectionsFragmentDialogFragment myDialogFragment = new VisitMdirectionsFragmentDialogFragment(directionsModel, directionsList.indexOf(directionsModel)); //  <-- внутренний класс
                            myDialogFragment.show(manager, "dialog");
                        }else { // направление есть, значит нажали, чтобы удалить

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
                                    // нажали "Да, хотим удалить"
                                    int curDirectionsVisitsID = directionsModel.directionsVisitsID;
                                    // сначала удалим визит-направление из таблицы направлений этого визита:
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
                                    if (deletingRowID != -1){
                                        fDBMethods.deleteVisitsDirection(deletingRowID);
                                    }

                                    // теперь удалим визуально в пользовательском интерфейсе этого визита
                                    tv_FragmentDirections_VisitsView.setText("");
                                    btn_edit_item_CuresVisit.setImageResource(R.mipmap.btn_add_black);
                                    btn_browse_item_DirectionsVisit.setVisibility(View.GONE);
                                    directionsModel.directionsVisitsID = 0;
                                    directionsList.set(directionsList.indexOf(directionsModel),directionsModel);

                                    // теперь сделаем нужное отвязывание от этого визита-основания в визите направлении:
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
*/
                    }

                });


                // просмотрим визит-направление
                btn_browse_item_CuresVisit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
/*
                        if (directionsModel.directionsVisitsID != 0){
                            switch (directionsModel.directionsVisitsType){
                                case Constants.DIRECTIONS_ANALISIS_TYPE_1:
                                    Intent intentAV = new Intent(getContext(), MAnalisisVisitActivity.class);
                                    intentAV.putExtra("choosen_visits_id", directionsModel.directionsVisitsID);
                                    startActivity(intentAV);
                                    break;
                                case Constants.DIRECTIONS_VISITS_TYPE_0:
                                    Intent intent = new Intent(getContext(), MVisitActivity.class);
                                    intent.putExtra("choosen_visits_id", directionsModel.directionsVisitsID);
                                    startActivity(intent);
                                    break;
                            }
                        }
*/
                    }
                });


            }


        }
    }


    public interface OnGetSaveVisitCommand{
        int onGetSaveVisitCommand();
    }

    public interface OnGetVisitsID{
        int onGetVisitsID();
    }


}
