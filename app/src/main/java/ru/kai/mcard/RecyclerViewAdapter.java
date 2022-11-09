package ru.kai.mcard;
// кастомный адаптер для PhotoScrollingActivity

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by akabanov on 20.05.2016.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.PhotoRecyclerViewHolders> {

    private List<ItemPhotoObject> itemList;
    private Context context;

    public RecyclerViewAdapter(Context context, List<ItemPhotoObject> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public PhotoRecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_for_photo_scrolling_recycled_list, null);
        PhotoRecyclerViewHolders rcv = new PhotoRecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(PhotoRecyclerViewHolders holder, int position) {
        holder.photoName.setText(itemList.get(position).getPhotoName());
        //holder.photoImg.setImageURI(Uri.parse(itemList.get(position).getPhotoPath()));
        holder.photoImg.setImageBitmap(decodeSampledBitmapFromFile(itemList.get(position).getPhotoPath(), 120, 140));
        holder.itemPhotoObject = itemList.get(position);
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    public void updateRecyclerViewAdapter(Context context, List<ItemPhotoObject> itemList) {
        this.itemList = itemList;
        this.context = context;
    }


    //*************************************************
    // уменьшение размера картинки для загрузки в "галерею"
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String photoPath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(photoPath, options);
    }

    public class PhotoRecyclerViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView photoName;
        public ImageView photoImg;
        ItemPhotoObject itemPhotoObject;

        PhotoScrollingActivity viewContext;
        //private Boolean longClicked = false;

        public PhotoRecyclerViewHolders(View itemView) {
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
}
