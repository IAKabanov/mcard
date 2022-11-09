package ru.kai.mcard;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.support.design.widget.FloatingActionButton;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.kai.mcard.utility.Common;


public class MChosenDirActivity extends AppCompatActivity{

    RecyclerView rv;
    LinearLayoutManager llManager;
    BackupsDirsFilesRVAdapter rvBackupsDirsFilesRVAdapter;
    RelativeLayout rlChosenDirContainer;

    TextView textPath;
    Context _context;
    int select_id_list = -1;
    String path = "/";

    String operation;

    Drawable singleDrawable;

    //ArrayList<String> arrayDirFilesList = new ArrayList<String>();
    //ArrayAdapter<String> adapter;

    List<BackupsDirsFilesListsModel> arrayDirFilesList = new ArrayList<>();

    AlertDialog.Builder ad;

    // Google analytics (GA)
    private Tracker mTracker; // еще в onCreate и onResume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _context = this;
        super.onCreate(savedInstanceState);

        // установка темы
        setTheme(Common.getCurrentTheme(this));

        setContentView(R.layout.m_activity_chosen_dir);

        // Google analytics (GA)
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MCardApplication application = (MCardApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]
        // Enable Display Features.
        mTracker.enableAdvertisingIdCollection(true);
        //-------------------- GA

        operation = getIntent().getStringExtra("operation");
        path = getIntent().getStringExtra("path");

        Toolbar mToolBar = (Toolbar) findViewById(R.id.mChosenDirToolbar);
        setSupportActionBar(mToolBar);
        if (operation.equals("restore")){
            assert mToolBar != null;
            mToolBar.setTitle(getString(R.string.choose_file_title));
        }else {
            assert mToolBar != null;
            mToolBar.setTitle(getString(R.string.choose_dir_title));
        }

        textPath = (TextView) findViewById(R.id.textPath);

        rv = (RecyclerView) findViewById(R.id.mDirListsRecyclerView);
        if (rv != null) {
            rv.setHasFixedSize(true);
        }

        llManager = new LinearLayoutManager(this);
        rv.setLayoutManager(llManager);

        update_list_dir();

        rvBackupsDirsFilesRVAdapter = new BackupsDirsFilesRVAdapter(arrayDirFilesList);
        rv.setAdapter(rvBackupsDirsFilesRVAdapter);

        //adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayDirFilesList);
        //list_dir.setAdapter(adapter);

/*
        list_dir.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                select_id_list = (int) id;
                update_list_dir();
            }
        });
*/

/*
        if (mSettings.contains(Constants.BACKUPS_PATH_ON_SD_CARD)) {
            path = mSettings.getString(Constants.BACKUPS_PATH_ON_SD_CARD, "/");
            update_list_dir();
        }
*/

        // обычные item-ы (для выделения)
        singleDrawable = Common.getOrdinaryItemsDawable(MChosenDirActivity.this);

        rlChosenDirContainer = (RelativeLayout) findViewById(R.id.rlChosenDirContainer);

        FloatingActionButton fabChosenDir = (FloatingActionButton)findViewById(R.id.fabChosenDir);

        String title = getResources().getString(R.string.choose_dir_create_dir_dialog_title);
        //String message = getResources().getString(R.string.choose_dir_create_dir_dialog_enter_dir_name);
        String btnPositiveString = getResources().getString(R.string.dialog_positive_button_text_create);
        String btnNegativeString = getResources().getString(R.string.dialog_negative_button_text_cancel);

        ad = new AlertDialog.Builder(MChosenDirActivity.this);
        ad.setTitle(title);  // заголовок
        //ad.setMessage(message); // сообщение
        //ad.setView(inflater.inflate(R.layout.layout_set_name, null));
        ad.setPositiveButton(btnPositiveString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                // Google analytics (GA)
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Backup")
                        .setAction("Add Dir for backup.")
                        .build());

                RelativeLayout rlChosenDirContainer = (RelativeLayout) findViewById(R.id.rlChosenDirContainer);
                TextView dirName = (TextView)((AlertDialog) dialog).findViewById(R.id.dirname);
                String DirNameString = null;
                if (dirName != null) {
                    DirNameString = dirName.getText().toString();
                    if (DirNameString.isEmpty()){
                        Snackbar snackbar = Common.getCustomSnackbar(MChosenDirActivity.this, rlChosenDirContainer, getString(R.string.choose_dir_create_dir_must_enter_dir_name));
                        snackbar.show();
                        return;
                    }else {
                        File newDir = new File(path, DirNameString);
                        newDir.mkdir();
                        update_list_dir();
                        rvBackupsDirsFilesRVAdapter.notifyDataSetChanged();
                    }
                }
                //setResult(RESULT_OK);
                //finish();
            }
        });
        ad.setNegativeButton(btnNegativeString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                //setResult(RESULT_CANCELED);
                //finish();
            }
        });

        fabChosenDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                ad.setView(inflater.inflate(R.layout.layout_set_name, null));
                ad.show();
            }
        });

        if (operation.equals("restore")){
            fabChosenDir.setVisibility(View.GONE);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Google analytics (GA)
        Log.i(Constants.DEBUG_TAG, "Setting screen name: " + "Выбор каталога (MChosenDirActivity)");
        mTracker.setScreenName("Выбор каталога (MChosenDirActivity)");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chosen_dir, menu);

        if (operation.equals("restore")){
            menu.findItem(R.id.chosen_dir_img_OK).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

/*
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
*/

        switch (id) {
            case R.id.chosen_dir_img_Back:
                onClickBack();
                break;
            case R.id.chosen_dir_img_OK:
                onClickGo();
                break;
            case R.id.chosen_dir_img_Close:
                chosenDirActivityClose();
                break;
        }

        return true;
    }

    private void chosenDirActivityClose() {

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void onClickBack() {

        if (path.equals("/")){
            return;
        }

        path = (new File(path)).getParent();
        if (!(path.equals("/"))){
            path = path + "/";
        }
        update_list_dir();
        rvBackupsDirsFilesRVAdapter.notifyDataSetChanged();
    }

    public void onClickGo() {
        Intent intent = new Intent();
        intent.putExtra("path", path);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void update_list_dir() {
        if (select_id_list != -1) {
            path = path + arrayDirFilesList.get(select_id_list).dirsFilesName + "/";
            //path = path + "/" + arrayDirFilesList.get(select_id_list).dirsFilesName + "/";
        }
        select_id_list = -1;
        arrayDirFilesList.clear();
        File files = new File(path);
        if ((files == null)|(!files.exists())) {

            path = "/storage/sdcard1/";  // инициализация
            files = new File(path);
            if (files == null) {  // проверка, вдруг у кого-нибудь SD карта по другому пути
                path = "/";
                files = new File(path);
            }

        }

        if (!files.isDirectory()){
            path = files.getParentFile().getAbsolutePath() + "/";
            files = new File(path);
        }

        if (!files.canRead()){
            //path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
            files = new File(path);
        }

        if (files != null) {
            if (files.listFiles() != null) { // т.к. папка может оказаться пустая, без файлов
                for (File aFile : files.listFiles()) {
                    if (aFile.isDirectory()) {
                        if (dir_opened(aFile.getPath())) {
                            arrayDirFilesList.add(new BackupsDirsFilesListsModel(aFile.getName(), aFile.getAbsolutePath(), R.drawable.ic_folder_vector_24dp, true));
                        }
                    } else {
                        arrayDirFilesList.add(new BackupsDirsFilesListsModel(aFile.getName(), aFile.getAbsolutePath(), R.drawable.ic_file_vector_24dp, false));
                    }
                }
                Collections.sort(arrayDirFilesList, new CompByIsDirAndName());
            }
        }
        //adapter.notifyDataSetChanged();
        textPath.setText(path);

    }

    private boolean dir_opened(String url) {
        try {
            File[] files = new File(url).listFiles();
            for (@SuppressWarnings("unused") File aFile : files) {
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private class BackupsDirsFilesListsModel{
        String dirsFilesName;
        String dirsFilesPath;
        int dirsFilesIcon;
        Boolean isDir;

        BackupsDirsFilesListsModel(String dirsFilesName, String dirsFilesPath, int dirsFilesIcon, Boolean isDir){
            this.dirsFilesName = dirsFilesName;
            this.dirsFilesPath = dirsFilesPath;
            this.dirsFilesIcon = dirsFilesIcon;
            this.isDir = isDir;
        }

        public int getIsDir(){
            if (this.isDir){
                return 0;
            }
            return 1;
        }

        public String getDirsFilesName(){
            return this.dirsFilesName;
        }

    }

    public class CompByIsDirAndName implements Comparator<BackupsDirsFilesListsModel> {

        @Override
        public int compare(BackupsDirsFilesListsModel o1, BackupsDirsFilesListsModel o2) {
            // отнимает id и получаем результат в переменную flag
            int flag = o1.getIsDir() - o2.getIsDir();

            // если получили 0, то сортируем по имени
            if(flag == 0) flag = o1.getDirsFilesName().compareTo(o2.getDirsFilesName());
            return flag;
        }
    }

    private class BackupsDirsFilesRVAdapter extends RecyclerView.Adapter<BackupsDirsFilesRVAdapter.BackupsDirsFilesViewHolders> {

        List<BackupsDirsFilesListsModel> dirsFilesList;

        BackupsDirsFilesRVAdapter(List<BackupsDirsFilesListsModel> dirsFilesList) {
            this.dirsFilesList = dirsFilesList;
        }

        @Override
        public BackupsDirsFilesViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_for_file_system, null);
            BackupsDirsFilesViewHolders diagnosesListVH = new BackupsDirsFilesViewHolders(v);

            return diagnosesListVH;
        }

        @Override
        public void onBindViewHolder(BackupsDirsFilesViewHolders holder, int position) {

            holder.ivDirFileIcon.setImageResource(dirsFilesList.get(position).dirsFilesIcon);
            holder.tvDirFileName.setText(dirsFilesList.get(position).dirsFilesName);
            holder.isDir = dirsFilesList.get(position).isDir;
            holder.filePath = dirsFilesList.get(position).dirsFilesPath;
        }

        public List<BackupsDirsFilesListsModel> swapCursor(List<BackupsDirsFilesListsModel> newDirsFilesList) {
            if (dirsFilesList == newDirsFilesList) {
                return null;
            }
            List<BackupsDirsFilesListsModel> oldDirsFilesList = dirsFilesList;
            this.dirsFilesList = newDirsFilesList;
            if (newDirsFilesList != null) {
                this.notifyDataSetChanged();
            }
            return oldDirsFilesList;
        }

        @Override
        public int getItemCount() {
            return (dirsFilesList == null) ? 0 : dirsFilesList.size();
        }

        public class BackupsDirsFilesViewHolders extends RecyclerView.ViewHolder {

            LinearLayout llDirFileCardView; // для выделения цветом
            ImageView ivDirFileIcon;
            TextView tvDirFileName;
            Boolean isDir;
            String filePath;

            public BackupsDirsFilesViewHolders(final View itemView) {
                super(itemView);
                llDirFileCardView = (LinearLayout) itemView.findViewById(R.id.llDirFileCardView);
                ivDirFileIcon = (ImageView) itemView.findViewById(R.id.ivDirFileIcon);
                tvDirFileName = (TextView) itemView.findViewById(R.id.tvDirFileName);

                llDirFileCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (isDir) {
                            //select_id_list = getPosition();
                            select_id_list = getAdapterPosition();
                            update_list_dir();
                            rvBackupsDirsFilesRVAdapter.notifyDataSetChanged();
                            //rvBackupsDirsFilesRVAdapter.swapCursor(arrayDirFilesList);
                            //rvBackupsDirsFilesRVAdapter = null;
                            //rvBackupsDirsFilesRVAdapter = new BackupsDirsFilesRVAdapter(arrayDirFilesList);
                            //rv.setAdapter(rvBackupsDirsFilesRVAdapter);

                        }else{
                            if (operation.equals("restore")){
                                String curFileName = tvDirFileName.getText().toString();
                                if ((curFileName.contains("m_sd_card_copy_"))&&(curFileName.contains(".mcc"))){
                                    Intent intent = new Intent();
                                    intent.putExtra("chosen_file_name", filePath);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }else {
                                    Snackbar snackbar = Common.getCustomSnackbar(MChosenDirActivity.this, rlChosenDirContainer, getString(R.string.backup_sdcard_backup_is_epsend));
                                    snackbar.show();

                                }
                            }
                        }
                    }
                });

            }

        }
    }



}