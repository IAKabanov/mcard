package ru.kai.mcard;
// полноэкранный просмотр фотографий

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;

public class PhotoViewActivity extends AppCompatActivity {

    TouchImageView ivPhotoView;
    Uri mOutputFileUri;
    FloatingActionsMenu floatingActionsMenu;

    final int PROGRESS_DLG_ID = 48;

    private DBMethods fDBMethods;
    AlertDialog.Builder ad;
    Context context;
    String filePath;

    // Google analytics (GA)
    private Tracker mTracker;    // еще в onCreate и onResume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_view);

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

        ivPhotoView = (TouchImageView)findViewById(R.id.ivPhotoView);
        floatingActionsMenu = (FloatingActionsMenu)findViewById(R.id.multiple_actions);

/*
        // при прикосновении двумя пальцами (т.е. юзер начал зумить для просмотра) убираем видимость меню:
        ivPhotoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (event.getPointerCount() > 1){
                        floatingActionsMenu.setVisibility(View.INVISIBLE);
                    }
                }
                return false;
            }
        });
*/
        final int fileID = getIntent().getIntExtra("fileID", -1);
        filePath = getIntent().getStringExtra("filePath");

        //mOutputFileUri = Uri.parse(filePath);
        mOutputFileUri = Uri.fromFile(new File(filePath));

        ivPhotoView.setImageURI(mOutputFileUri);
        //ivPhotoView.setImageBitmap(BitmapFactory.decodeFile(filePath));


        // fab
        FloatingActionButton actionPhotoEdit = (FloatingActionButton)findViewById(R.id.action_photo_edit);
        FloatingActionButton actionPhotoDelete = (FloatingActionButton)findViewById(R.id.action_photo_delete);
        FloatingActionButton actionRotateLeft = (FloatingActionButton)findViewById(R.id.action_rotate_left);
        FloatingActionButton actionRotateRight = (FloatingActionButton)findViewById(R.id.action_rotate_right);
        actionPhotoEdit.setIcon(R.mipmap.btn_photos_edit);
        actionPhotoDelete.setIcon(R.mipmap.btn_photos_del);
        actionRotateLeft.setIcon(R.mipmap.btn_left_red);
        actionRotateRight.setIcon(R.mipmap.btn_right_red);

        actionPhotoEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Фото. Пересняли. (actionPhotoEdit)")
                        .build());

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //mOutputFileUri = Uri.fromFile(new File(mOutputFileUri.getPath()));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputFileUri); // результат будет в поле mOutputFileUri
                startActivityForResult(intent, Constants.CAMERA_RESULT);
            }
        });
        actionPhotoDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.show();
            }
        });
        actionRotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Фото. Повернули налево. (actionRotateLeft)")
                        .build());

                //actionB.setVisibility(actionB.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                new DownloadImageTask().execute("left");
                //butRotatePhotoLeft(ivPhotoView);
                floatingActionsMenu.collapse();
            }
        });
        actionRotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Фото. Повернули направо. (actionRotateRight)")
                        .build());

                //actionB.setVisibility(actionB.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                new DownloadImageTask().execute("right");
                //butRotatePhotoRight(ivPhotoView);
                floatingActionsMenu.collapse();
            }
        });

        // чтобы fam-меню скрывалось при нажатии на кнопку
        ivPhotoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                floatingActionsMenu.collapse();
                return false;
            }
        });

        context = PhotoViewActivity.this;
        String title = getResources().getString(R.string.delete_file);
        String message = getResources().getString(R.string.delete_photo);
        String button1String = getResources().getString(R.string.edit_profiles_delete_profile_positive_button_text);
        String button2String = getResources().getString(R.string.edit_profiles_delete_profile_negative_button_text);

        ad = new AlertDialog.Builder(context);
        ad.setTitle(title);  // заголовок
        ad.setMessage(message); // сообщение
        ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Фото. Удалили. (actionPhotoDelete)")
                        .build());

                // сначала саму фотографию
                File currentFile = new File(filePath);
                currentFile.delete();
                fDBMethods.deleteVisitsPhotoByID(fileID);
                setResult(RESULT_OK);
                finish();
            }
        });
        ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Фото. Отмена удаления. (actionPhotoDelete)")
                        .build());

                setResult(RESULT_CANCELED);
                //finish();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        // Google analytics (GA)
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "PhotoViewActivity");
        mTracker.setScreenName("Фото. Просмотр PhotoViewActivity onResume");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CAMERA_RESULT: {
                if (resultCode == RESULT_OK) {
                    //Intent intent = new Intent();
                    //intent.putExtra("chosen_photos_type", photosType);
                    //intent.putExtra("chosen_photos_name", editPhotosName.getText().toString());
                    //intent.putExtra("chosen_photos_uri", mOutputFileUri.getPath());
                    //setResult(RESULT_OK, intent);
                    setResult(RESULT_OK);
                    finish();
                }
                break;
            }
        }
    }

    /*
    @Override
    protected void onStop() {
        super.onStop();
        ivPhotoView.setImageURI(null);
        ivPhotoView.setImageBitmap(null);
    }
*/


    @Override
    protected Dialog onCreateDialog(int dialogId){
        ProgressDialog progress = null;
        switch (dialogId) {
            case PROGRESS_DLG_ID:
                progress = new ProgressDialog(this);
                progress.setMessage("Loading...");

                break;
        }
        return progress;
    }

    protected void onDestroy() {
        super.onDestroy();
        if (fDBMethods != null) {
            fDBMethods.close();
        }
        ivPhotoView.setImageURI(null);
    }

    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            publishProgress(new Void[]{});

            String direction = "";
            if( params.length > 0 ){
                direction = params[0];
            }

            Bitmap b;
            if (direction == "right"){

                BitmapDrawable mydrawable = (BitmapDrawable) ivPhotoView.getDrawable();
                b = mydrawable.getBitmap();

                Matrix matrix = new Matrix();
                matrix.postRotate(90);  // поворачиваем на 90 градусов
                b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);


            }else if (direction == "left"){

                BitmapDrawable mydrawable = (BitmapDrawable) ivPhotoView.getDrawable();
                b = mydrawable.getBitmap();

                Matrix matrix = new Matrix();
                matrix.postRotate(270);  // поворачиваем на 270 градусов
                b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);

            }else {
                b = null;
            }

            return b;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            showDialog(PROGRESS_DLG_ID);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            dismissDialog(PROGRESS_DLG_ID);

            ivPhotoView.setImageBitmap(result);
        }
    }



}
