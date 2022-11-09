package ru.kai.mcard;


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
public class MVisitRecommendationsFragment extends Fragment {

    private DBMethods fDBMethods;
    int gettedID;
    int mDiagnosisID;

    FloatingActionButton fabAddVisitsRecommendations;

    public ArrayList<VisitsRecommendationsModel> recommendationsList;
    VisitsRecommendationsRVAdapter rvVisitsRecommendationsAdapter;

    // настройки
    private SharedPreferences mSettings;
    private int showVisitsCuresHelp = 1;
    //TextView visits_fragment_recommendations_list_help;


    public MVisitRecommendationsFragment() {
        // Required empty public constructor
    }

    public static MVisitRecommendationsFragment newInstance(Bundle bundle) {
        MVisitRecommendationsFragment currentFragment = new MVisitRecommendationsFragment();
        Bundle args = new Bundle();
        args.putBundle("gettedArgs", bundle);
        currentFragment.setArguments(args);
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
        View rootView =  inflater.inflate(R.layout.fragment_visit_m_recommendations, container, false);

        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.rvVisitsFragmentRecommendations);
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


        recommendationsList = new ArrayList<>();
        if (gettedID > 0){
            Cursor cursor = fDBMethods.getAllVisitsRecommendations(gettedID);
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                do {
                    int rowID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsRecommendations._ID));
                    int visitsRecommendationsID = cursor.getInt(cursor.getColumnIndex(DBMethods.TableVisitsRecommendations.COLUMN_RECOMMENDATIONS_ID));

                    recommendationsList.add(new VisitsRecommendationsModel(gettedID, visitsRecommendationsID));

                }while (cursor.moveToNext());
            }
            cursor.close();
        }


        rvVisitsRecommendationsAdapter = new VisitsRecommendationsRVAdapter(recommendationsList);
        rv.setAdapter(rvVisitsRecommendationsAdapter);


        fabAddVisitsRecommendations = (FloatingActionButton) rootView.findViewById(R.id.fabAddVisitsRecommendations);
        fabAddVisitsRecommendations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (gettedID > 0){
                    Intent intent = new Intent(getContext(), MRecommendationsActivity.class);
                    startActivityForResult(intent, Constants.RECOMMENDATIONS_DATA);
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

                            Intent intent = new Intent(getContext(), MRecommendationsActivity.class);
                            startActivityForResult(intent, Constants.RECOMMENDATIONS_DATA);
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


    @Override  // получаем ответ от открытых ранее активностей
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            switch (requestCode) {
                case Constants.RECOMMENDATIONS_DATA:
                    // если выбрали вид исследования

                    int choosenVisitsRecomendationsID = data.getIntExtra("choosen_recommendations_id", 0);
                    String choosenVisitsRecomendationsDescr = fDBMethods.getRecommendationsNameByID(choosenVisitsRecomendationsID);

                    // проверим не содержится ли уже исследование в списке (проверка по recomendationsID)
                    int currentRecomendationsID = 0;
                    int i;
                    boolean itIsDubble = false;
                    for (i = 0; i < recommendationsList.size(); i++) {
                        VisitsRecommendationsModel itemModel = recommendationsList.get(i);
                        currentRecomendationsID = itemModel.visitsRecommendationsID;
                        if (currentRecomendationsID == choosenVisitsRecomendationsID) {
                            itIsDubble = true;
                            break;
                        }
                    }
                    if (itIsDubble == false) {
                        recommendationsList.add(new VisitsRecommendationsModel(gettedID, choosenVisitsRecomendationsID));
                        rvVisitsRecommendationsAdapter.notifyDataSetChanged();
                    }

                    break;
            }
            //ArrayList<MVisitRecommendationsFragment.VisitsRecommendationsModel> recommendationsList = fragmentVisitMrecommendations.recommendationsList;
            if ((recommendationsList != null) && (recommendationsList.size() > 0)) {
                fDBMethods.updateVisitsRecommendationsList(gettedID, recommendationsList);
            }

        }

    }



    public class VisitsRecommendationsModel {

        int basicVisitsID; // визит, на котором выдана рекомендация (т.е. этот визит)
        public int visitsRecommendationsID; // id рекомендации

        ArrayList<VisitsRecommendationsModel>recomendationsList;

        VisitsRecommendationsModel(){
            this.recomendationsList = new ArrayList<>();
        }

        VisitsRecommendationsModel(int basicVisitsID, int visitsRecommendationsID){
            this.basicVisitsID = basicVisitsID;
            this.visitsRecommendationsID = visitsRecommendationsID;
       }

        public void add(int basicVisitsID, int visitsRecommendationsID){
            recomendationsList.add(new VisitsRecommendationsModel(basicVisitsID, visitsRecommendationsID));
        }

    }

    public class VisitsRecommendationsRVAdapter extends RecyclerView.Adapter<VisitsRecommendationsRVAdapter.RecommendationsListViewHolders> {

        ArrayList<VisitsRecommendationsModel> recommendationsList;

        VisitsRecommendationsRVAdapter(ArrayList<VisitsRecommendationsModel> recommendationsList) {
            this.recommendationsList = recommendationsList;
        }

        @Override
        public RecommendationsListViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_for_visit_recommendations_list_recycled_list, null);
            RecommendationsListViewHolders recommendationsListVH = new RecommendationsListViewHolders(v);

            return recommendationsListVH;
        }

        @Override
        public void onBindViewHolder(RecommendationsListViewHolders holder, int position) {
            holder.recommendationsModel = recommendationsList.get(position);
            String currentRecommendationsTitle = fDBMethods.getRecommendationsNameByID(holder.recommendationsModel.visitsRecommendationsID);
            holder.tv_VisitsRecommendationsTitle.setText(currentRecommendationsTitle);
            // представление визита
            // пока все сделаем невидимым:
            holder.btn_browse_item_RecommendationsVisit.setVisibility(View.GONE);
            holder.btn_add_del_item_Visits.setVisibility(View.GONE);
            holder.tv_FragmentRecommendations_VisitsView.setVisibility(View.GONE);

        }

        @Override
        public int getItemCount() {
            return recommendationsList.size();
        }

        public class RecommendationsListViewHolders extends RecyclerView.ViewHolder {

            TextView tv_VisitsRecommendationsTitle;
            TextView tv_FragmentRecommendations_VisitsView;
            ImageButton btn_browse_item_RecommendationsVisit;
            ImageButton btn_add_del_item_Visits;
            ImageButton btn_Visists_RecommendationsItem;

            VisitsRecommendationsModel recommendationsModel;


            public RecommendationsListViewHolders(View itemView) {
                super(itemView);
                tv_VisitsRecommendationsTitle = (TextView) itemView.findViewById(R.id.tv_VisitsRecommendationsTitle);
                tv_FragmentRecommendations_VisitsView = (TextView) itemView.findViewById(R.id.tv_FragmentRecommendations_VisitsView);

                btn_browse_item_RecommendationsVisit = (ImageButton) itemView.findViewById(R.id.btn_browse_item_RecommendationsVisit);
                btn_add_del_item_Visits = (ImageButton) itemView.findViewById(R.id.btn_add_del_RecommendationsVisit);
                btn_Visists_RecommendationsItem = (ImageButton) itemView.findViewById(R.id.btn_Visists_RecommendationsItem);

                // большая кнопка
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
