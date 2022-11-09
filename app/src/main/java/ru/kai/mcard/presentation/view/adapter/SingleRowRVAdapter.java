package ru.kai.mcard.presentation.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ru.kai.mcard.Constants;
import ru.kai.mcard.di.pack.PerFragments.PerListFragment;
import ru.kai.mcard.presentation.presentations_model.BasePModel;
import ru.kai.mcard.presentation.view.activity.IListDetailActivityView;
import ru.kai.mcard.presentation.view.adapter.view_holders.BaseViewHolder;
import ru.kai.mcard.utility.Common;
import ru.kai.mcard.R;

/**
 * Created by akabanov on 20.09.2017.
 */

@PerListFragment
public class SingleRowRVAdapter extends RecyclerView.Adapter<BaseViewHolder> implements IRVAdapterContract{

    private List<? extends BasePModel> collection;
    private final LayoutInflater layoutInflater;
    private Context context;

    private int selectedItemColor;
    private int commonColor;
    private BasePModel mCurPModel = null;
    private int mPreviousItemPosition = 0;
    public int mCurItemPosition = -1;

    private static final int TYPE_SINGLE_CARD = 11;
    private static final int TYPE_DOUBLE_CARD = 12;

    private IListDetailActivityView itemClickListener;

    @Inject
    public SingleRowRVAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.collection = Collections.emptyList();

        // присваиваем Activity, как имплементацию IOnRVAdapterItemClickListener
        if (this.context instanceof IListDetailActivityView){
            this.itemClickListener = (IListDetailActivityView) this.context;
        }else{
            this.itemClickListener = null;
            throw new RuntimeException("Activity must implement IOnRVAdapterItemClickListener.");
        }

        this.selectedItemColor = Common.getSelectedItemsColor(context); // background выбранных item-ов
        this.commonColor = Common.getCommonItemsColor(context); // background обычных item-ов
    }

    @Override
    public int getItemViewType(int position) {
        if (collection.get(position) instanceof BasePModel){
            return TYPE_SINGLE_CARD;
        }else {
            return TYPE_DOUBLE_CARD;
        }
    }

    @Override
    public SingleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // CardView - неординарный виджет.
        // Так как в разметке он не растягивается в ширину на весь экран,
        // то в качестве корневого элемента используем RelativeLayout.
        View v = null;
        SingleViewHolder singleListVH = null;
        switch (viewType){
            case TYPE_SINGLE_CARD:
                v = layoutInflater.inflate(R.layout.card_view_single_new, null);
                break;
            case TYPE_DOUBLE_CARD:
                break;
        }

        if (v != null) {
/*
            View cardView = ((RelativeLayout) v).getChildAt(0);
            ((CardView) cardView).setCardBackgroundColor(Common.getSelectedItemsColor(context));
            View textView = ((CardView) cardView).getChildAt(0);
            ((TextView) textView).setTextColor(Common.getChoosenItemsTextColor(context));
*/
            singleListVH = new SingleViewHolder(v);
        }
        return singleListVH;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {

        BasePModel curPModel = null;
        if (holder instanceof SingleViewHolder){
            curPModel = collection.get(position);
            holder.setPModel(curPModel);
        }else{

        }

        if (mCurPModel.getModelID() == curPModel.getModelID()){
        //if (position == mCurItemPosition){
            holder.setCVBackgroundColor(selectedItemColor);
            mCurItemPosition = position;
        }else {
            holder.setCVBackgroundColor(commonColor);
        }


    }

    @Override
    public int getItemCount() {
        return (this.collection != null) ? this.collection.size() : 0;
    }

    //public void setCollection(@NonNull Collection<BasePModel> baseCollection, SinglePModel curPModel){
    public void setCollection(@NonNull List<? extends BasePModel> baseCollection){
        this.validateCollection(baseCollection);
        this.collection = baseCollection;

        // инициализация модели:
        if (mCurPModel == null || mCurPModel.getModelID() == -1) {
            mCurPModel = baseCollection.get(0);
            itemClickListener.setCurPModelInitially(mCurPModel);
        }
        this.notifyDataSetChanged();
    }

    @Override
    public void setCurrentPModel(BasePModel pModel) {
        mCurPModel = pModel;
    }

    //private void validateCollection(Collection<BasePModel> baseCollection) {
    private void validateCollection(List<? extends BasePModel> baseCollection) {
        if (baseCollection == null) {
            throw new IllegalArgumentException("The list cannot be null");
        }
    }

    public class SingleViewHolder extends BaseViewHolder implements View.OnClickListener{

        private TextView tvSimpleCardView;
        private CardView cvSimpleCardView;

        private BasePModel pModel;
        //    private TextView tvSimpleCommonCardView;
        private int itemID;
//    private LinearLayout llSimpleCommonCardView; // для выделения цветом текущего item-а

        SingleViewHolder(View itemView) {
            super(itemView);
            tvSimpleCardView = (TextView) itemView.findViewById(R.id.tvSimpleCardView);
            cvSimpleCardView = (CardView) itemView.findViewById(R.id.cvSimpleCardView);
            cvSimpleCardView.setOnClickListener(this);

//        tvSimpleCommonCardView = (TextView) itemView.findViewById(R.id.tvSimpleCommonCardView);
//        llSimpleCommonCardView = (LinearLayout) itemView.findViewById(R.id.llSimpleCommonCardView);
        }

        public TextView getTvSimpleCommonCardView() {
            return tvSimpleCardView;
        }

        private void setTextTvSimpleCardView(String tvSimpleCardView) {
            this.tvSimpleCardView.setText(tvSimpleCardView);
        }

        public BasePModel getPModel() {
            return pModel;
        }

        @Override
        public void setPModel(BasePModel pModel) {
            this.pModel = pModel;
            this.itemID = pModel.getModelID();
            setTextTvSimpleCardView(pModel.getName());

        }

        @Override
        public void setCVBackgroundColor(int backgroundColor){
//        llSimpleCommonCardView.setBackground(backgroundDrawable);
            cvSimpleCardView.setCardBackgroundColor(backgroundColor);
        }

        private void setPreviousItemColor(final int previousItemPosition){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    SingleRowRVAdapter.this.notifyItemChanged(previousItemPosition);
                }
            });
            thread.start();
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null){
                // we set the selected color to the current item
                this.setCVBackgroundColor(selectedItemColor);

                if (mCurItemPosition > -1){
                    // we set the general color to the previously selected item
                    setPreviousItemColor(mCurItemPosition);
                }

                SingleRowRVAdapter.this.mCurPModel = this.pModel;
                mCurItemPosition = getAdapterPosition();

                itemClickListener.onRVAdapterItemClick(this.pModel, mCurItemPosition);

            }
        }
    }










}
