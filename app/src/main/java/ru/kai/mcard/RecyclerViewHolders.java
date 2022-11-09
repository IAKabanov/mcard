package ru.kai.mcard;
// обработчик фотографий из RecyclerViewAdapter для PhotoScrollingActivity

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by akabanov on 20.05.2016.
 */
public class RecyclerViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView photoName;
    public ImageView photoImg;
    ItemPhotoObject itemPhotoObject;

    PhotoScrollingActivity viewContext;
    //private Boolean longClicked = false;

    public RecyclerViewHolders(View itemView) {
        super(itemView);

        itemView.setOnClickListener(this);
        //itemView.setOnLongClickListener(this);

        photoName = (TextView)itemView.findViewById(R.id.photoName);
        photoImg = (ImageView)itemView.findViewById(R.id.photoImg);

        viewContext = (PhotoScrollingActivity) itemView.getContext();

    }

    @Override
    public void onClick(View view) {

        markCardView(view);

        int currentPhotoID = itemPhotoObject.getPhotoID();
        String currentPhotoPath = itemPhotoObject.getPhotoPath();
        if (!currentPhotoPath.isEmpty()){
            showChosenPhoto(currentPhotoID, currentPhotoPath);
        }

    }

    // процедура помечает (рамкой) "тапнутую" фотографию и снимает отметку с остальных
    private void markCardView(View view) {

        for (int i = 0; i < viewContext.photoList.size(); i++) {
            CardView currentCV = (CardView)viewContext.lLayout.getChildAt(i).findViewById(R.id.card_view);
            currentCV.setCardBackgroundColor(android.R.color.transparent);
        }

        CardView cardView = (CardView) view.findViewById(R.id.card_view);
        cardView.setCardBackgroundColor(R.color.black_semi_transparent);
    }

    public void showChosenPhoto(int currentPhotoID, String currentPhotoPath) {
        // показать фото в полном размере

        Intent intent = new Intent(viewContext, PhotoViewActivity.class);
        intent.putExtra("fileID", currentPhotoID);
        intent.putExtra("filePath", currentPhotoPath);
        viewContext.startActivityForResult(intent, Constants.PHOTO_VIEW_OPEN);
    }


}