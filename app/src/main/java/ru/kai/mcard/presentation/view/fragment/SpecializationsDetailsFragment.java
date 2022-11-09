package ru.kai.mcard.presentation.view.fragment;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.squareup.leakcanary.RefWatcher;

import javax.inject.Inject;

import ru.kai.mcard.Constants;
import ru.kai.mcard.MCardApplication;
import ru.kai.mcard.R;
import ru.kai.mcard.presentation.presentations_model.BasePModel;
import ru.kai.mcard.presentation.view.animation.AnimationUtils;
import ru.kai.mcard.presentation.view.animation.IDismissible;
import ru.kai.mcard.presentation.view.animation.RevealAnimationSetting;
import ru.kai.mcard.utility.Common;

/**
 * Created by akabanov on 24.10.2017.
 * DetailsFragment - ведомый фрагмент. В нем данные будут только отображаться.
 * Вся ответственность за CRUD будет на Activity, так как именно там кнопка "Сохранить" и т.д.
 */

public class SpecializationsDetailsFragment extends BaseFragment implements ISpecializationsDetailsFragment, IDismissible{

    private RelativeLayout rl_progress;
    private EditText editSpecializationName;

    @NonNull
    private BasePModel curModel;
    @Nullable
    private RevealAnimationSetting revealAnimationSetting = null;

    public SpecializationsDetailsFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null){
            BasePModel pModel = (BasePModel) bundle.getSerializable("specializationPModel");
            if (pModel == null) {
                curModel = new BasePModel(Constants.CREATE_NEW_MODEL);
            }
            else {
                curModel = pModel;
            }
            if(bundle.containsKey("ARG_REVEAL_SETTINGS")){
                revealAnimationSetting = bundle.getParcelable("ARG_REVEAL_SETTINGS");
            }
        }else {
            curModel = new BasePModel(Constants.CREATE_NEW_MODEL);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.specialization_details_fragment_new, container, false);
        rl_progress = (RelativeLayout) rootView.findViewById(R.id.rl_progress);
        editSpecializationName = (EditText) rootView.findViewById(R.id.editSpecializationName);

        if (revealAnimationSetting != null) {
            //int colorStart = Common.getRevealAnimationStartColor(getActivity());
            //int colorEnd = Common.getRevealAnimationEndColor(getActivity());
            int colorStart = getResources().getColor(Common.getRevealAnimationStartColor(getActivity()));
            int colorEnd = getResources().getColor(Common.getRevealAnimationEndColor(getActivity()));
            AnimationUtils.registerCircularRevealAnimation(getContext(), rootView, revealAnimationSetting, colorStart, colorEnd);
        }

        return rootView;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.renderSpecializationsDetails(curModel);
        if (savedInstanceState != null){
            if (savedInstanceState.containsKey("pModelName")) {
                editSpecializationName.setText(savedInstanceState.getString("pModelName"));
            }
        }
    }

    @Override
    public void dismiss(OnDismissedListener listener) {
        int colorStart = getResources().getColor(Common.getRevealAnimationStartColor(getActivity()));
        int colorEnd = Common.getSelectedItemsColor(getActivity());
        //int colorEnd = getResources().getColor(Common.getSelectedItemsColor(getActivity()));
        AnimationUtils.startCircularRevealExitAnimation(getContext(), getView(), revealAnimationSetting, colorStart, colorEnd, listener);
    }

    @Override
    public void onDestroyView() {
        rl_progress = null;
        editSpecializationName = null;
        curModel = null;
        super.onDestroyView();
    }

    @Override public void onDestroy() {
        super.onDestroy();

        // for LeakCanary
        RefWatcher refWatcher = MCardApplication.getRefWatcher(getActivity());
        if (refWatcher != null){
            refWatcher.watch(this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (editSpecializationName == null)return; // ошибка при утечке фрагмента (пока заткнем)
        String curModelName = getSafedModelName(editSpecializationName.getText().toString());
        outState.putString("pModelName", curModelName);
    }

    @Override
    public void showLoading() {
        this.rl_progress.setVisibility(View.VISIBLE);
        //this.getActivity().setProgressBarIndeterminateVisibility(true);
    }

    @Override
    public void hideLoading() {
        this.rl_progress.setVisibility(View.GONE);
        //this.getActivity().setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void showError(String message) {
        this.showToastMessage(message);
    }

    @Override
    public void renderSpecializationsDetails(@NonNull BasePModel pModel) {
        this.curModel = pModel;
        if (pModel == null){
            return;
        }
        String curModelName = getSafedModelName(pModel.getName());
        editSpecializationName.setText(curModelName);

        if (pModel.getModelID() == Constants.MODEL_IS_NULL) {
/*
            tvSpecializationCaption.setVisibility(View.GONE);
            llSpecializationName.setVisibility(View.GONE);
            tvSpecializationsDetailsSelectItem.setVisibility(View.VISIBLE);
*/
        } else if (pModel.getModelID() == Constants.CREATE_NEW_MODEL) {
/*
            tvSpecializationCaption.setVisibility(View.VISIBLE);
            llSpecializationName.setVisibility(View.VISIBLE);
            tvSpecializationsDetailsSelectItem.setVisibility(View.GONE);
*/
        } else {
/*
            tvSpecializationCaption.setVisibility(View.VISIBLE);
            llSpecializationName.setVisibility(View.VISIBLE);
            tvSpecializationsDetailsSelectItem.setVisibility(View.GONE);
*/

            //editSpecializationName.setText(pModel.getName());
        }
    }

    private String getSafedModelName(String s){
        if (s == null){
            return  "";
        }
        return s;
    }

    private void updateCurModel(){
        curModel.setName(editSpecializationName.getText().toString());
    }

    @NonNull
    @Override
    public BasePModel getCurrentPModelName() {
        updateCurModel();
        return curModel;
    }

}

