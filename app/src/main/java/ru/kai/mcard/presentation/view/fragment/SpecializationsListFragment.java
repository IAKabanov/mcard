package ru.kai.mcard.presentation.view.fragment;


import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.squareup.leakcanary.RefWatcher;

import java.util.List;

/*
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
*/
import javax.inject.Inject;

import ru.kai.mcard.Constants;
import ru.kai.mcard.MCardApplication;
import ru.kai.mcard.R;
import ru.kai.mcard.di.pack.Application.IAppComponent;
import ru.kai.mcard.di.pack.PerFragments.DaggerISpecializationsListComponent;
import ru.kai.mcard.di.pack.PerFragments.ISpecializationsListComponent;
import ru.kai.mcard.di.pack.PerFragments.SpecializationsListModule;
import ru.kai.mcard.presentation.presentations_model.BasePModel;
import ru.kai.mcard.presentation.presenter.SpecializationsListPresenter;
import ru.kai.mcard.presentation.view.activity.IListDetailActivityView;
import ru.kai.mcard.presentation.view.adapter.SingleRowRVAdapter;
import ru.kai.mcard.presentation.view.animation.RevealAnimationSetting;
import ru.kai.mcard.utility.Utility;


/**
 * A simple {@link Fragment} subclass.
 */
public class SpecializationsListFragment extends BaseFragment implements ISpecializationsListFragment {

    private LinearLayoutManager llManager;
    private RecyclerView rv;
    private RelativeLayout rl_progress;
    private FloatingActionButton fabSpecializationsList;
    private ViewGroup listContainer;

    private ISpecializationsListComponent component;
    @Inject
    SpecializationsListPresenter presenter;
    @Inject
    SingleRowRVAdapter specializationsAdapter;

    private IListDetailActivityView iListDetailActivityView;
    private int curItemPosition = 0;
    private List<BasePModel> curCollection = null; // текущий список. Нужен для того, чтобы при удалении item-а правильно определить предыдущий (чтобы установить на него курсор)

    private final String TAG = SpecializationsListFragment.class.getSimpleName();

    public SpecializationsListFragment() {
        // Required empty public constructor
        //setRetainInstance(true);
        Log.w(TAG, " SpecializationsListFragment = " + SpecializationsListFragment.this.toString());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        MCardApplication.getInstance().getAppComponent().createSpecializationsListComponent().inject(this);
        IAppComponent appComponent = MCardApplication.getInstance().getAppComponent();
        component = DaggerISpecializationsListComponent.builder()
                .specializationsListModule(new SpecializationsListModule(getActivity()))
                .iAppComponent(appComponent)
                .build();
        component.inject(this);

        Bundle bundle = getArguments();
        if (bundle != null){
            curItemPosition = bundle.getInt("curItemPosition");
        }

        Log.w(TAG, " onCreate = " + SpecializationsListFragment.this.toString());

        this.presenter.initialize();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof IListDetailActivityView){
            this.iListDetailActivityView = (IListDetailActivityView) activity;
        }else{
/*
            try {
                throw new ClassNotFoundException("Activity must implement IOnFabClickListener.");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
*/
            throw new RuntimeException("Activity must implement IListDetailActivityView.");
        }
        Log.w(TAG, " onAttach = " + SpecializationsListFragment.this.toString());
    }

    @Override
    public void onDetach() {
        Log.w(TAG, " onDetach = " + SpecializationsListFragment.this.toString());
        this.iListDetailActivityView = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.specialization_list_fragment, container, false);
        rl_progress = (RelativeLayout) rootView.findViewById(R.id.rl_progress);
        fabSpecializationsList = (FloatingActionButton) rootView.findViewById(R.id.fabSpecializationsList);
        fabSpecializationsList.setOnClickListener(fabOnClickListener);

        //container = (RelativeLayout) rootView.findViewById(R.id.SpecializationActivityContainerList);
        listContainer = container;

        rv = (RecyclerView)rootView.findViewById(R.id.mSpecializationsListRecyclerView);
        if (rv != null) {
            rv.setHasFixedSize(true);
        }
        //llManager = new ListsLinearLayoutManager(getActivity());
        llManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llManager);
        rv.setAdapter(specializationsAdapter);

        Log.w(TAG, " onCreateView = " + SpecializationsListFragment.this.toString());

        initSwipe();

        return rootView;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.presenter.setView(this);
        Log.w(TAG, " onViewCreated = " + SpecializationsListFragment.this.toString());
        if (savedInstanceState == null){
            this.loadSpecializationsList();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        setPositionCurPModel(curItemPosition);

        Log.w(TAG, " onResume = " + SpecializationsListFragment.this.toString() + " - " + curItemPosition);
    }

//    @Override
    public void setPositionCurPModel(int mCurrentItemPosition){
        specializationsAdapter.mCurItemPosition = mCurrentItemPosition;
        specializationsAdapter.notifyItemChanged(mCurrentItemPosition);

        if (mCurrentItemPosition != -1){
            //rv.smoothScrollToPosition(mCurrentItemPosition +3);
            rv.scrollToPosition(mCurrentItemPosition - 2);
            //llManager.scrollToPositionWithOffset(mCurrentItemPosition - 2, 0);
        }else {
            rv.smoothScrollToPosition(0);
        }


    }

    @Override
    public void onDestroyView() {
        rv.setAdapter(null);
//        specializationsAdapter = null;
        Log.w(TAG, " onDestroyView = " + SpecializationsListFragment.this.toString());
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        this.llManager = null;
        this.rv = null;
        this.rl_progress = null;
        this.fabSpecializationsList = null;
        this.specializationsAdapter = null;
        this.fabOnClickListener = null;

        this.presenter.destroy();
        this.presenter = null;
        this.component = null;
        this.curCollection = null;
        Log.w(TAG, " onDestroy = " + SpecializationsListFragment.this.toString());
        super.onDestroy();

        // for LeakCanary
        RefWatcher refWatcher = MCardApplication.getRefWatcher(getActivity());
        if (refWatcher != null) {
            refWatcher.watch(this);
        }

    }

    @Override
    public void showLoading() {
        this.rl_progress.setVisibility(View.VISIBLE);
        this.getActivity().setProgressBarIndeterminateVisibility(true);
    }

    @Override
    public void hideLoading() {
        this.rl_progress.setVisibility(View.GONE);
        this.getActivity().setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void showError(String message) {
        this.showToastMessage(message);
    }

    /**
     * Loads all specializations.
     */
    @Override
    public void loadSpecializationsList() {
        this.presenter.loadSpecializationsList();
    }

    @Override // обновить (перерисовать) список (реализация интерфейса ISpecializationsListFragment)
    public void renderSpecializationsList(@Nullable List<BasePModel> pModelsList) {
        if (pModelsList != null) {
            this.specializationsAdapter.setCurrentPModel(iListDetailActivityView.getCurrentPModel());
            this.specializationsAdapter.setCollection(pModelsList);
            this.curCollection = pModelsList;
        }else {
            showError(getActivity().getResources().getString(R.string.list_is_empty));
        }
    }

    private View.OnClickListener fabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            presenter.createSpecialisation();
        }
    };

    public void createNewSpecialisation(){
        // передаем в активность
        iListDetailActivityView.onFabClickListener();

    }

    private void initSwipe(){

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                // флаг: если ландшафт, то свайпаем только налево.
                if (!Utility.isThisOrientationPort(getActivity())) {
                    int swipeFlags = ItemTouchHelper.LEFT;
                    int dragFlags = 0;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }
                return super.getMovementFlags(recyclerView, viewHolder);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                curItemPosition = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT){
                    // удалить
                    final BasePModel curPModel = ((SingleRowRVAdapter.SingleViewHolder) viewHolder).getPModel();
                    String title = getResources().getString(R.string.deleting_visit_question_mini);
                    //String message = getResources().getString(R.string.do_not_show_help_info);
                    String btnPositiveString = getResources().getString(R.string.dialog_positive_button_text);
                    String btnNegativeString = getResources().getString(R.string.dialog_negative_button_text);

                    AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
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
                                fDBMethods.deleteSpecialization(currentSpecializationsID);
                            }
                            getLoaderManager().restartLoader(0, null, MSpecializationsActivity.this);
                            //getLoaderManager().getLoader(0).forceLoad();
                            //rvAnalisesTypesRVAdapter.notifyDataSetChanged();
*/
                            presenter.deleteItem(curPModel);
                            //if (count > 0) {
                            BasePModel previousPModel = findPreviousPModelInCurCollection(curPModel);
                            iListDetailActivityView.setCurPModel(previousPModel);
                            iListDetailActivityView.renderSingleDetailsFragment(previousPModel);
                                loadSpecializationsList();
                                //setPositionCurPModel(curItemPosition);
                            //}

                        }
                    });
                    ad.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            // отправляем команду презентеру для перезагрузки списка, чтобы перерисовать открывшуюся иконку удаления
                            iListDetailActivityView.setCurPModel(curPModel);
                            loadSpecializationsList();
                            setPositionCurPModel(curItemPosition);
                        }
                    });
                    ad.setCancelable(false);
                    ad.show();

                } else {   // if (direction == ItemTouchHelper.RIGHT)
                    // изменить (в ландшафтной ориентации изменять будем не по свайпу, а по "тапу")
                    if (Utility.isThisOrientationPort(getActivity())) {
                        // отправляем в activity команду на открытие DetailFragment
                        BasePModel curPModel = ((SingleRowRVAdapter.SingleViewHolder) viewHolder).getPModel();
                        iListDetailActivityView.onItemBrowsingListener(curPModel);
                    }
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Paint paint = new Paint();
                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 1;
                    if (dX > 0) {
                        // edit // перерисовку делаем только для портретной ориентации
                        if (Utility.isThisOrientationPort(getActivity())) {
                            paint.setColor(Color.parseColor("#20ab1e"));
                            RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                            c.drawRect(background, paint);
                            icon = BitmapFactory.decodeResource(getResources(), R.mipmap.btn_edit_black);
                            RectF icon_dest = new RectF((float) itemView.getLeft() + 0 * width, (float) itemView.getBottom() - width, (float) itemView.getLeft() + 1 * width, (float) itemView.getTop() + width);
                            c.drawBitmap(icon, null, icon_dest, paint);
                        }
                    } else {
                        // delete
                        paint.setColor(Color.parseColor("#FF4081"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background, paint);
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.btn_delete_black);
                        RectF icon_dest = new RectF((float) ((float) itemView.getRight() - 1 * width), (float) itemView.getBottom() - width, (float) ((float) itemView.getRight() - 0 * width), (float) itemView.getTop() + width);
                        c.drawBitmap(icon, null, icon_dest, paint);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rv);
    }

    private BasePModel findPreviousPModelInCurCollection(BasePModel curPModel){
        // При удалении текущая модель удаляется. Нужно установить новую curPModel.
        // В самом простом случае находим ее и возвращаем.
        // Если удаляемая оказалась первой в списке, тогда, если список еще не пустой, то берем следующую за удаляемой.
        // А если удаляемая модель была последней, то просто возвращаем новую.
        if (curCollection.contains(curPModel)){
            int curIndex = curCollection.indexOf(curPModel);
            if ((curIndex) > 0){
                BasePModel previousPModel = curCollection.get(curIndex - 1);
                return previousPModel;
            }else {
                if (curCollection.size() > 1){
                    BasePModel nextModel = curCollection.get(1);
                    return nextModel;
                }
            }
        }
        return new BasePModel(Constants.CREATE_NEW_MODEL);
    }

    @Override
    public int getCurItemPosition() {
        return curItemPosition;
    }

    @Override
    public void setCurItemPosition(int curItemPosition) {
        this.curItemPosition = curItemPosition;
    }

    @Override
    public void notifyUpdatedItem(BasePModel pModel){
        specializationsAdapter.setCurrentPModel(pModel);
        loadSpecializationsList();
        setPositionCurPModel(curItemPosition);
        //specializationsAdapter.notifyDataSetChanged();
    }

    private RevealAnimationSetting constructRevealSettings() {
        return RevealAnimationSetting.with(
                (int) (fabSpecializationsList.getX() + fabSpecializationsList.getWidth() / 2),
                (int) (fabSpecializationsList.getY() + fabSpecializationsList.getHeight() / 2),
                listContainer.getWidth(),
                listContainer.getHeight());
    }

    @Override
    public RevealAnimationSetting getRevealSettings() {
        return constructRevealSettings();
    }

}
