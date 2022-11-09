package ru.kai.mcard;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import android.support.design.widget.FloatingActionButton;

public class MAnalisisVisitAnaliseslistFragment extends Fragment {

    private DBMethods fDBMethods;
    int gettingID;
    int mCurrentProfileID; // нужно знать для какого профиля сохраняем данный визит

    ArrayList<analisesTypesListModel> analisesTypesList;
    RVAdapter rvAdapter;

    public MAnalisisVisitAnaliseslistFragment() {
        // Required empty public constructor
    }

    public static MAnalisisVisitAnaliseslistFragment newInstance(Bundle bundle) {
        MAnalisisVisitAnaliseslistFragment currentFragment = new MAnalisisVisitAnaliseslistFragment();
        Bundle args = new Bundle();
        args.putBundle("gettedArgs", bundle);
        currentFragment.setArguments(args);
        return currentFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_analisis_visit_m_analiseslist, container, false);

        RecyclerView rv = (RecyclerView)rootView.findViewById(R.id.rvAnalisisVisitMfragmentAnalisisList);
        rv.setHasFixedSize(true);

        LinearLayoutManager llManager = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llManager);

        fDBMethods = new DBMethods(getContext());
        fDBMethods.open();

        // получаем из активности
        Bundle bundle = getArguments().getBundle("gettedArgs");
        gettingID = bundle.getInt("gettingID", 0);
        mCurrentProfileID = bundle.getInt("currentProfileID", -1);

        analisesTypesList = new ArrayList<>();
        if (gettingID > 0){
            Cursor cursor = fDBMethods.getAllAnalisisVisitsAnalisesList(gettingID);
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                do {
                    int analisisTypesID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID));
                    if (analisisTypesID != 0){
                        String analisisVisitsTypesName = fDBMethods.getAnalisesTypesNameByID(analisisTypesID);
                        int referencesVisitsID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList.COLUMN_REFERENCES_VISITS_ID));
                        analisesTypesList.add(new analisesTypesListModel(gettingID, analisisTypesID, analisisVisitsTypesName, referencesVisitsID));
                    }
                }while (cursor.moveToNext());
            }
            cursor.close();
        }

        // визит может также быть открыт и для создания нового направления. Добавим строку из направляющего визита
        if(gettingID == 0){
            int mReferencesID = bundle.getInt("mReferencesID", 0);
            if (mReferencesID != 0){
                int directAnalisisTypeID = bundle.getInt("directAnalisisTypeID", 0);
                String analisisVisitsTypesName = fDBMethods.getAnalisesTypesNameByID(directAnalisisTypeID);
                analisesTypesList.add(new analisesTypesListModel(gettingID, directAnalisisTypeID, analisisVisitsTypesName, mReferencesID));
            }
        }

        rvAdapter = new RVAdapter(analisesTypesList);
        rv.setAdapter(rvAdapter);

        FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.fabAnalisisVisitMfragmentAnalisisList);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MAnalisesTypesActivity.class);
                startActivityForResult(intent, Constants.ANALISIS_VISIT_ANALISES_TYPES_DATA);
            }
        });

        return rootView;
    }

    @Override  // получаем ответ от открытых ранее активностей
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.ANALISIS_VISIT_ANALISES_TYPES_DATA:
                if (resultCode == getActivity().RESULT_OK) {
                    // если выбрали вид исследования

                    int analisisTypesID = data.getIntExtra("given_analises_types_id", 0);
                    String analisisVisitsTypesName = fDBMethods.getAnalisesTypesNameByID(analisisTypesID);
                    int directionsVisitsID = 0;

                    // проверим не содержится ли уже исследование в списке (проверка по analisisTypesID и по directionsVisitsID)
                    // то есть одинаковое исследование может повторяться в списке только если его назначили на разных визитах-консультациях.
                    int currentAnalisisTypesID = 0;
                    int currentDirectionsVisitsID = 0;
                    int i;
                    boolean itIsDubble = false;
                    for (i = 0; i < analisesTypesList.size(); i++) {
                        analisesTypesListModel itemModel = analisesTypesList.get(i);
                        currentAnalisisTypesID = itemModel.analisisVisitsTypesID;
                        currentDirectionsVisitsID = itemModel.referencesVisitsID;
                        if (currentAnalisisTypesID == analisisTypesID){
                            if (currentDirectionsVisitsID == directionsVisitsID){
                                itIsDubble = true;
                                break;
                            }
                        }
                    }
                    if (itIsDubble == false){
                        analisesTypesList.add(new analisesTypesListModel(gettingID, analisisTypesID, analisisVisitsTypesName, directionsVisitsID));
                        rvAdapter.notifyDataSetChanged();
                    }
                }
                break;
            case Constants.REFERENCE_BASIC_VISIT:
                // в открытом визите-основании могли изменить табличную часть и исключить связь, поэтому обновим табличную часть
                analisesTypesList.clear();
                Cursor cursor = fDBMethods.getAllAnalisisVisitsAnalisesList(gettingID);
                if (cursor.getCount()>0){
                    cursor.moveToFirst();
                    do {
                        int analisisTypesID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList.COLUMN_ANALISIS_TYPES_ID));
                        if (analisisTypesID != 0){
                            String analisisVisitsTypesName = fDBMethods.getAnalisesTypesNameByID(analisisTypesID);
                            int referencesVisitsID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableAnalisisVisitsAnalisesList.COLUMN_REFERENCES_VISITS_ID));
                            analisesTypesList.add(new analisesTypesListModel(gettingID, analisisTypesID, analisisVisitsTypesName, referencesVisitsID));
                        }
                    }while (cursor.moveToNext());
                }
                cursor.close();
                break;
        }
        //if ((analisesTypesList != null) && (analisesTypesList.size() > 0)) {
        if (analisesTypesList != null) {
            fDBMethods.updateAnalisisVisitsAnalisesList(gettingID, analisesTypesList);
            rvAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fDBMethods != null) {
            fDBMethods.close();
        }
    }

    public class analisesTypesListModel {
        int basicAnalisisVisitID;
        public int analisisVisitsTypesID;
        String analisisVisitsTypesName;
        public int referencesVisitsID;  // id визита, с которого направили на исследование

        ArrayList<analisesTypesListModel>analisesList;

        analisesTypesListModel(){
            this.analisesList = new ArrayList<>();
        }

        analisesTypesListModel(int basicAnalisisVisitID, int analisisVisitsTypesID, String analisisVisitsTypesName, int referencesVisitsID){
            this.basicAnalisisVisitID = basicAnalisisVisitID;
            this.analisisVisitsTypesID = analisisVisitsTypesID;
            this.analisisVisitsTypesName = analisisVisitsTypesName;
            this.referencesVisitsID = referencesVisitsID;
        }

        public void add(int basicAnalisisVisitsID, String analisisVisitsTypesName, int directionsVisitsID){
            analisesList.add(new analisesTypesListModel(basicAnalisisVisitsID, analisisVisitsTypesID, analisisVisitsTypesName, directionsVisitsID));
        }

    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.AnalisesTypesListViewHolders>{
        ArrayList<analisesTypesListModel> analisesList;

        RVAdapter(ArrayList<analisesTypesListModel> analisesList){
            this.analisesList = analisesList;
        }

        @Override
        public AnalisesTypesListViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_for_av_analises_list_recycled_list, null);
            AnalisesTypesListViewHolders analisisTypesListVH = new AnalisesTypesListViewHolders(v);

            return analisisTypesListVH;
        }

        @Override
        public void onBindViewHolder(AnalisesTypesListViewHolders holder, int position) {
            holder.tv_AV_AnalisesTypesMTitle.setText(analisesList.get(position).analisisVisitsTypesName);
            holder.analisesTypesListModel = analisesList.get(position);
            int curReferencesVisitsID = holder.analisesTypesListModel.referencesVisitsID;
            if (curReferencesVisitsID == 0){
                //holder.btn_edit_item_AV_AnalisesTypesM.setVisibility(View.VISIBLE);
                holder.btn_item_AV_AnalisesTypesM.setImageResource(R.mipmap.btn_delete_black);
            }else {
                //holder.btn_edit_item_AV_AnalisesTypesM.setVisibility(View.GONE);
                holder.btn_item_AV_AnalisesTypesM.setImageResource(R.mipmap.btn_browse_black);
                String curReferencesVisitsView = fDBMethods.getVisitsViewByID(curReferencesVisitsID);
                holder.tv_AV_Ref_VisitsView.setText(curReferencesVisitsView);
            }
        }

        @Override
        public int getItemCount() {
            return analisesList.size();
        }


        public class AnalisesTypesListViewHolders extends RecyclerView.ViewHolder{

            TextView tv_AV_AnalisesTypesMTitle;
            TextView tv_AV_Ref_VisitsView;
            //ImageButton btn_edit_item_AV_AnalisesTypesM;
            ImageButton btn_item_AV_AnalisesTypesM;
            analisesTypesListModel analisesTypesListModel;

            public AnalisesTypesListViewHolders(View itemView) {
                super(itemView);
                tv_AV_AnalisesTypesMTitle = (TextView)itemView.findViewById(R.id.tv_AV_AnalisesTypesMTitle);
                tv_AV_Ref_VisitsView = (TextView)itemView.findViewById(R.id.tv_AV_Ref_VisitsView);
                //btn_edit_item_AV_AnalisesTypesM = (ImageButton)itemView.findViewById(R.id.btn_edit_item_AV_AnalisesTypesM);
                btn_item_AV_AnalisesTypesM = (ImageButton)itemView.findViewById(R.id.btn_item_AV_AnalisesTypesM);

                // большая кнопка:
                btn_item_AV_AnalisesTypesM.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (analisesTypesListModel.referencesVisitsID == 0){
                            // нет направляющего визита, просто удаляем из списка
                            //analisesTypesListModel.analisesList.remove(new analisesTypesListModel());
                            analisesTypesList.remove(analisesTypesListModel);
                            rvAdapter.notifyDataSetChanged();

                            // сразу удалим из БД
                            if (analisesTypesList != null) {
                                fDBMethods.updateAnalisisVisitsAnalisesList(gettingID, analisesTypesList);
                            }

                        }else {
                            // открыть и просмотреть направляющий визит
                            Intent intent = new Intent(getContext(), MVisitActivity.class);
                            intent.putExtra("choosen_visits_id", analisesTypesListModel.referencesVisitsID);
                            intent.putExtra("currentProfileID", mCurrentProfileID);
                            startActivityForResult(intent, Constants.REFERENCE_BASIC_VISIT);
                        }

                    }
                });

            }

        }

    }

    public interface OnGetSaveVisitCommand{
        int onGetSaveVisitCommand();
    }



}
