package ru.kai.mcard;
// галерея "выставка-продажа" ) фотографий визита

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.kai.mcard.utility.Common;

public class PhotoScrollingActivity extends AppCompatActivity {

    public GridLayoutManager lLayout;

    private DBMethods fDBMethods;

    String loadingPhotos;

    private Uri mCreatedFileUri = null;
    private String mCreatedFileName = ""; // пользовательская часть имени вновь создаваемого файла
    private String mStructuredFileName = ""; // структурированная пользовательская часть имени вновь создаваемого файла с учетом номера визита и т.д.
    private String mStructuredFilesDir = ""; // директория для хранения файлов
    //File currentFile = null;
    int typeOfVisit;  // тип визита, к которому прикрепляем фотографию (обычный или визит-исследование)
    int basicVisitsID; // тип и id визита, к которому прикрепляем фотографию
    int mCurrentProfileID;
    String gettedPhotosName;
    String gettedPhotosURI;
    int whyPhotoOpened; // открыто для создания нового или изменения существующего
    int photosType; // для понимания: откуда загружаем из камеры или из галереи

    List<ItemPhotoObject> photoList;
    RecyclerView rView;
    RecyclerViewAdapter rcAdapter;
    Toolbar toolbar;
    CollapsingToolbarLayout mPhotoScrolling;
    FloatingActionButton fabPhotos;

    PhotoLoadingDialogFragment myDialogFragment;
    FragmentManager manager;

    // Google analytics (GA)
    private Tracker mTracker;   // еще в onCreate и onResume + события

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.activity_photoscrolling);


        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA


        toolbar = (Toolbar) findViewById(R.id.mPhotoToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        File homeDir = new File(String.valueOf(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)));
        if (!homeDir.exists()){
            homeDir.mkdirs();
        }
        mStructuredFilesDir = homeDir.getAbsolutePath() + "/";
        //mStructuredFilesDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/";

        fDBMethods = new DBMethods(this);
        fDBMethods.open();

        loadingPhotos = getResources().getString(R.string.loading);

        Bundle bundle = getIntent().getBundleExtra("photosBundle");
        typeOfVisit = bundle.getInt("typeOfVisit");
        basicVisitsID = bundle.getInt("basicVisitsID");
        mCurrentProfileID = bundle.getInt("currentProfileID");

        mPhotoScrolling = (CollapsingToolbarLayout) findViewById(R.id.mPhotoScrolling);
        mPhotoScrolling.setContentScrim(Common.getCurrentContentScrimDawable(PhotoScrollingActivity.this));

        rView = (RecyclerView)findViewById(R.id.mPhotoRecyclerView);

        fabPhotos = (FloatingActionButton) findViewById(R.id.fabPhoto);
        fabPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Add")
                        .setAction("Add фотографию.")
                        .build());

                // сначала создадим файл, в который будем записывать полученную фотографию:
                File createdFile = setCurrentFileUri();
                // получим URI созданного файла для передачи его в камеру, чтобы туда вернулась созданная фотография:
                mCreatedFileUri = Uri.fromFile(createdFile);

                // Check permission for CAMERA
                // если permission не установлена (на версии lolipop и выше), то запросим разрешение у пользователя
                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Check Permissions Now
                    // Callback onRequestPermissionsResult interceptado na Activity z_old_MainActivity1
                    ActivityCompat.requestPermissions(PhotoScrollingActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            Constants.CAMERA_RESULT);
                } else {
                    // permission has been granted, continue as usual
/*
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mCreatedFileUri);
                    //intent.setClipData(ClipData.newRawUri(null, mCreatedFileUri));
                    startActivityForResult(intent, Constants.CAMERA_RESULT);
*/
                    runTheCamera();

                }

            }
        });

        manager = getSupportFragmentManager();
        myDialogFragment = new PhotoLoadingDialogFragment(); //  <-- внутренний класс

        // асинхронная загрузка фотографий на форму
        photoList = new ArrayList<ItemPhotoObject>();
        new LoadingPhotosTask().execute();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case Constants.CAMERA_RESULT:
                if (grantResults.length > 0){
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        // запускаем камеру, чтобы пользователю не пришлось еще раз жать на fab
                        runTheCamera();
                    }else {
                        // snackbar нужно дать разрешение на использование камеры
                    }
                }else {
                    // snackbar нужно дать разрешение на использование камеры
                }
                break;
        }
    }

    private void runTheCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCreatedFileUri);
        //intent.setClipData(ClipData.newRawUri(null, mCreatedFileUri));
        startActivityForResult(intent, Constants.CAMERA_RESULT);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Google analytics (GA)
        //Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "PhotoScrollingActivity");
        mTracker.setScreenName("PhotoScrollingActivity (" + typeOfVisit + ")");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    public File setCurrentFileUri() {

        // будем сразу сохранять файл фотографии по этому пути
        File curFile;
        mCreatedFileName = CreateFileName();

        String state = Environment.getExternalStorageState();  // состояние внешней памяти

        if (Environment.MEDIA_MOUNTED.equals(state)) {       //  она смонтирована и доступна запишем во внешний контекст

            //curFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "img_" + // публичный (общий) каталог (НЕ удалится вместе с деинсталяцией приложения)
            //curFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "img_" + // публичный (общий) каталог (НЕ удалится вместе с деинсталяцией приложения)

            //curFile = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DCIM), "img_" +    // частный каталог (удалится вместе с деинсталяцией приложения)

            mStructuredFileName = "img_" +
                                    String.valueOf(mCurrentProfileID) + "_" +
                                    String.valueOf(typeOfVisit) + "_" +
                                    String.valueOf(basicVisitsID) + "_" +
                                    mCreatedFileName + ".jpg";

            curFile = new File(mStructuredFilesDir, mStructuredFileName);

/*
            curFile = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "img_" +    // частный каталог (удалится вместе с деинсталяцией приложения)
                    String.valueOf(typeOfVisit) + "_" +
                    String.valueOf(basicVisitsID) + "_" +
                    mCreatedFileName + ".jpg");
*/

            return curFile;

        } else {  // запишем во внутр. директорию приложения

            mStructuredFileName = "img_" +
                    String.valueOf(mCurrentProfileID) + "_" +
                    String.valueOf(typeOfVisit) + "_" +
                    String.valueOf(basicVisitsID) + "_" +
                    mCreatedFileName + ".jpg";

            curFile = new File(mStructuredFilesDir, mStructuredFileName);

/*
            curFile = new File(getApplicationContext().getFilesDir().getPath() + "/Picture/", "img_" +
                    String.valueOf(typeOfVisit) + "_" +
                    String.valueOf(basicVisitsID) + "_" +
                    mCreatedFileName + ".jpg");
*/
            return curFile;

        }

    }

    private String CreateFileName(){
        String curFileName;
        //curFileName = String.valueOf(System.currentTimeMillis());
        //Calendar calendar = Calendar.getInstance();
        //CharSequence date = DateFormat.format("yyyy.MM.dd_HH.mm.ss", new Date());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
        //currentDate = new Date();
        //System.out.println("currentDate = " + sdf.format(currentDate));


        curFileName = sdf.format(new Date());

        return curFileName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //ivPhotoAdd.setImageURI(null);  // очистим кэш

        switch (requestCode) {
            case Constants.CAMERA_RESULT: {
                if (resultCode == RESULT_OK) {
                    photosType = Constants.PHOTO_CAMERA;
                    // Проверяем, содержит ли результат маленькую картинку
                    if (data != null) {
                        Toast.makeText(PhotoScrollingActivity.this, "Маленькая картинка.", Toast.LENGTH_SHORT).show();
                        //if (data.hasExtra("data")) {
                            //Bitmap thumbnailBitmap = data.getParcelableExtra("data");
                            // TODO Какие-то действия с миниатюрой
                            //ivPhotoAdd.setImageBitmap(thumbnailBitmap);

                        //}
                    } else {
                        // TODO Какие-то действия с полноценным изображением, сохраненным по адресу mOutputFileUri
                        //Toast.makeText(PhotoScrollingActivity.this, "111.", Toast.LENGTH_SHORT).show();
                        String photoPath = mCreatedFileUri.getPath(); // mCreatedFileUri - сюда передает URI файла камера

                        ///////////////////////////////////////
/*
                        File dir = new File(getApplicationContext().getFilesDir().getPath());
                        //File dir = new File(getApplicationContext().getFilesDir().getPath() + "/Picture/");
                        if (!dir.isDirectory()){
                            dir.mkdir();
                        }
                        File newFile = new File(dir, "img_" +
                                String.valueOf(typeOfVisit) + "_" +
                                String.valueOf(basicVisitsID) + "_" +
                                mCreatedFileName + ".jpg");
                        String newFilePath = newFile.getPath();
                        Common.copyFile(photoPath, newFilePath);
                        Common.deleteFile(photoPath);
*/
                        //////////////////////////////////////

                        // добавим данные о фотографии в БД
                        Bundle bundle = new Bundle();
                        bundle.putInt("basicVisitsType", typeOfVisit);
                        bundle.putInt("basicVisitsID", basicVisitsID);
                        //bundle.putInt("visitsPhotosType", 0);   // из камеры (0) или из галереи (1)
                        bundle.putInt("visitsPhotosType", Constants.PHOTOS_TYPE_APP);   // из камеры (0) или из галереи (1)
                        bundle.putString("visitsPhotosName", mCreatedFileName);

                        //bundle.putString("visitsPhotosURI", photoPath);  // полный путь
                        bundle.putString("visitsPhotosURI", mStructuredFileName);

                        int photosID = (int) fDBMethods.insertVisitsPhotosNew(bundle);

                        // теперь добавим полученную фотку в лист фотографий photoList
                        photoList.add(new ItemPhotoObject(photosID, 0, mCreatedFileName, photoPath));
                        rcAdapter.notifyDataSetChanged();
                        mCreatedFileName = ""; //

                    }
                }

                break;
            }

            case Constants.GALLERY_REQUEST: {
                if (resultCode == RESULT_OK) {
                    photosType = Constants.PHOTO_GALLERY;

                }

                break;
            }
            case Constants.PHOTO_VIEW_OPEN:{  // вернулись с просмотра; могли там удалить фотку, поэтому нужно обновить лист фотографий
                photoList.clear();

                getAllItemList();
                if (rcAdapter == null){
                    lLayout = new GridLayoutManager(PhotoScrollingActivity.this, 3);

                    rView.setHasFixedSize(true);
                    rView.setLayoutManager(lLayout);

                    rcAdapter = new RecyclerViewAdapter(PhotoScrollingActivity.this, photoList);
                    rView.setAdapter(rcAdapter);
                }
                rcAdapter.updateRecyclerViewAdapter(PhotoScrollingActivity.this, photoList);
                rcAdapter.notifyDataSetChanged();
                break;
            }
        }


    }

    protected void onDestroy() {
        super.onDestroy();
        if (fDBMethods != null) {
            fDBMethods.close();
        }
    }

    @Override
    protected Dialog onCreateDialog(int dialogId){
        ProgressDialog progress = null;
        switch (dialogId) {
            case Constants.PROGRESS_DLG_ID:
                progress = new ProgressDialog(this);
                //progress = new ProgressDialog(PhotoScrollingActivity.this);
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setMessage(loadingPhotos);

                break;
        }
        return progress;
    }

    private void getAllItemList(){
    //private List<ItemPhotoObject> getAllItemList(){

        if (photoList == null){
            photoList = new ArrayList<ItemPhotoObject>();
        }

        String currentPhotosName;
        String currentPhotosPath;

        Cursor cursorVisitsPhotos = fDBMethods.getAllVisitsPhotos(typeOfVisit, basicVisitsID);

        cursorVisitsPhotos.moveToFirst();
        while (!cursorVisitsPhotos.isAfterLast()) {

            int photosID = cursorVisitsPhotos.getInt(cursorVisitsPhotos.getColumnIndex(DBMethods.TableVisitsPhotos._ID)); // id таблицы
            //int visitsID = cursorVisitsCures.getInt(cursorVisitsCures.getColumnIndex(DBMethods.TableVisitsCures.COLUMN_BASIC_VISITS_ID));
            int photosType = cursorVisitsPhotos.getInt(cursorVisitsPhotos.getColumnIndex(DBMethods.TableVisitsPhotos.COLUMN_PHOTOS_TYPE)); // из камеры (0) фотография или из галереи (1) (type - int)
            currentPhotosName = cursorVisitsPhotos.getString(cursorVisitsPhotos.getColumnIndex(DBMethods.TableVisitsPhotos.COLUMN_PHOTOS_NAME));
            currentPhotosPath = cursorVisitsPhotos.getString(cursorVisitsPhotos.getColumnIndex(DBMethods.TableVisitsPhotos.COLUMN_PHOTOS_URI));

            //allPhotoItemsList.add(new ItemPhotoObject(photosID, photosType, currentPhotosName, currentPhotosPath));
            photoList.add(new ItemPhotoObject(photosID, photosType, currentPhotosName, mStructuredFilesDir + currentPhotosPath));

            cursorVisitsPhotos.moveToNext();
        }
        cursorVisitsPhotos.close();

    }

    class LoadingPhotosTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            myDialogFragment.show(manager, "loading");

        }

        @Override
        protected Void doInBackground(Void... params) {
            //publishProgress(new Void[]{});

            getAllItemList();

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            lLayout = new GridLayoutManager(PhotoScrollingActivity.this, 3);

            rView.setHasFixedSize(true);
            rView.setLayoutManager(lLayout);

            rcAdapter = new RecyclerViewAdapter(PhotoScrollingActivity.this, photoList);
            rView.setAdapter(rcAdapter);

            if (myDialogFragment != null) {
                myDialogFragment.dismiss();
            }
        }
    }

    static public class PhotoLoadingDialogFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

/*
            //AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyCustomTheme );
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Pfuаагрузка...");

            return builder.create();
*/

            ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
            dialog.setMessage(getResources().getString(R.string.loading));
            dialog.setIndeterminate(true);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            return dialog;



        }
    }



}
