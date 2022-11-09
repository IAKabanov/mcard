package ru.kai.mcard.utility;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import ru.kai.mcard.Constants;
import ru.kai.mcard.ModelVisitsCures;
import ru.kai.mcard.R;

public class Common{

    static final int DIRECTIONS_VISITS_TYPE_0 = 0;
    static final int DIRECTIONS_ANALISIS_TYPE_1 = 1;

    static public ArrayList<ModelVisitsCures> clonPhotoArrayList(ArrayList<ModelVisitsCures> gettedArrayList) {

        ArrayList<ModelVisitsCures> clArrayList = new ArrayList<ModelVisitsCures>();
        ModelVisitsCures map;

        String currentPhotosType;
        String currentPhotosName;
        String currentPhotosURI;

        for (int i = 0; i < gettedArrayList.size(); i++) {
            HashMap itemMap_start = gettedArrayList.get(i);

            currentPhotosType = String.valueOf(itemMap_start.get("visitsPhotosType"));
            currentPhotosName = String.valueOf(itemMap_start.get("visitsPhotosName"));
            currentPhotosURI = String.valueOf(itemMap_start.get("visitsPhotosURI"));

            map = new ModelVisitsCures();
            map.put("visitsPhotosType", currentPhotosType);
            map.put("visitsPhotosName", currentPhotosName);
            map.put("visitsPhotosURI", currentPhotosURI);
            clArrayList.add(map);
        }

        return clArrayList;
    }

    public static int getAlternativeThemeColor(Context context) {

        int alternativeThemeColor = R.color.color_standart_green_btn_impel_bottom_gradient;
        int currentTheme = Common.getCurrentTheme(context);

        switch (currentTheme){
            case R.style.Theme_StandartGreen:
                alternativeThemeColor = R.color.color_standart_green_btn_impel_bottom_gradient;
                break;
            case R.style.Theme_StandartLemon:
                alternativeThemeColor = R.color.color_standart_lemon_btn_impel_bottom_gradient;
                break;
            case R.style.Theme_StandartPink:
                alternativeThemeColor = R.color.color_standart_pink_btn_impel_bottom_gradient;
                break;
            case R.style.Theme_StandartCornflower:
                alternativeThemeColor = R.color.color_standart_cornflower_btn_impel_bottom_gradient;
                break;
            default:
                alternativeThemeColor = R.color.color_standart_green_btn_impel_bottom_gradient;
                break;
        }

        return alternativeThemeColor;
    }

    public static int getCommonItemsColor(Context context) {

        int choosenItemsColor = R.color.color_standard_green_main_list_top;
        int currentTheme = R.style.Theme_StandartGreen;
        currentTheme = getCurrentTheme(context);

        switch (currentTheme){
            case R.style.Theme_StandartGreen:
                choosenItemsColor = R.color.color_standard_green_main_list_top;
                break;
            case R.style.Theme_StandartLemon:
                //choosenItemsColor = R.color.color_standart_lemon_item_list_selected_top;
                choosenItemsColor = R.color.color_standard_lemon_main_list_top;
                break;
            case R.style.Theme_StandartPink:
                //choosenItemsColor = R.color.color_standart_pink_item_list_selected_top;
                choosenItemsColor = R.color.color_standard_pink_main_list_top;
                break;
            case R.style.Theme_StandartCornflower:
                choosenItemsColor = R.color.color_standart_cornflower_item_list_selected_top;
                choosenItemsColor = R.color.color_standard_cornflower_main_list_top;
                break;
            default:
                choosenItemsColor = R.color.color_standard_green_main_list_top;
                break;
        }

        return context.getResources().getColor(choosenItemsColor);
        //return choosenItemsColor;
    }

    public static int getSelectedItemsColor(@NonNull Context context) {

        int choosenItemsColor = R.color.color_standard_green_main_list_selected;
        int currentTheme = R.style.Theme_StandartGreen;
        currentTheme = getCurrentTheme(context);

        switch (currentTheme){
            case R.style.Theme_StandartGreen:
                choosenItemsColor = R.color.color_standard_green_main_list_selected;
                break;
            case R.style.Theme_StandartLemon:
                choosenItemsColor = R.color.color_standart_lemon_item_list_selected_top;
                choosenItemsColor = R.color.color_standard_lemon_main_list_selected;
                break;
            case R.style.Theme_StandartPink:
                choosenItemsColor = R.color.color_standart_pink_item_list_selected_top;
                choosenItemsColor = R.color.color_standard_pink_main_list_selected;
                break;
            case R.style.Theme_StandartCornflower:
                choosenItemsColor = R.color.color_standart_cornflower_item_list_selected_top;
                choosenItemsColor = R.color.color_standard_cornflower_main_list_selected;
                break;
            default:
                choosenItemsColor = R.color.color_standard_green_main_list_selected;
                break;
        }

        return context.getResources().getColor(choosenItemsColor);
        //return choosenItemsColor;
    }

    public static int getChoosenItemsTextColor(Context context) {

        int choosenItemsTextColor = R.color.color_standart_green_shadow;
        int currentTheme = R.style.Theme_StandartGreen;
        currentTheme = getCurrentTheme(context);

        switch (currentTheme){
            case R.style.Theme_StandartGreen:
                choosenItemsTextColor = R.color.color_standart_green_btn_text;
                choosenItemsTextColor = R.color.color_standart_green_shadow;
                break;
            case R.style.Theme_StandartLemon:
                choosenItemsTextColor = R.color.color_standart_lemon_btn_text;
                choosenItemsTextColor = R.color.color_standart_lemon_shadow;
                break;
            case R.style.Theme_StandartPink:
                choosenItemsTextColor = R.color.color_standart_pink_btn_text;
                choosenItemsTextColor = R.color.color_standart_pink_shadow;
                break;
            case R.style.Theme_StandartCornflower:
                choosenItemsTextColor = R.color.color_standart_cornflower_btn_text;
                choosenItemsTextColor = R.color.color_standart_cornflower_shadow;
                break;
            default:
                choosenItemsTextColor = R.color.color_standart_green_shadow;
                break;
        }

        return context.getResources().getColor(choosenItemsTextColor);
        //return choosenItemsColor;
    }

    public static int getPrimeryDarkColor(int currentTheme) {

        int choosenColor = R.color.colorPrimaryDarkGreen;

        switch (currentTheme){
            case R.style.Theme_StandartGreen:
                choosenColor = R.color.colorPrimaryDarkGreen;
                break;
            case R.style.Theme_StandartLemon:
                choosenColor = R.color.colorPrimaryDarkLemon;
                break;
            case R.style.Theme_StandartPink:
                choosenColor = R.color.colorPrimaryDarkPink;
                break;
            case R.style.Theme_StandartCornflower:
                choosenColor = R.color.colorPrimaryDarkCornflower;
                break;
            default:
                choosenColor = R.color.colorPrimaryDarkGreen;
                break;
        }

        return choosenColor;
    }

    // почему-то цвет StatusBar-а не устанавливается при смене темы. Установим его при помощи этой процедуры
    public static void setStatusBarColor(Activity context, int newTheme) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = context.getWindow();

            int statusBarColor = context.getResources().getColor(getPrimeryDarkColor(newTheme));

            if (statusBarColor == Color.BLACK && window.getNavigationBarColor() == Color.BLACK) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
            window.setStatusBarColor(statusBarColor);
        }
    }

    public static int getRevealAnimationStartColor(@NonNull Context context) {

        int currentTheme = getCurrentTheme(context);
        int choosenRevealAnimationStartColor;
        switch (currentTheme){
            case R.style.Theme_StandartGreen:
                choosenRevealAnimationStartColor = R.color.color_standart_green_toolbar_background;
                break;
            case R.style.Theme_StandartLemon:
                choosenRevealAnimationStartColor = R.color.color_standart_lemon_toolbar_background;
                break;
            case R.style.Theme_StandartPink:
                choosenRevealAnimationStartColor = R.color.color_standart_pink_toolbar_background;
                break;
            case R.style.Theme_StandartCornflower:
                choosenRevealAnimationStartColor = R.color.color_standart_cornflower_toolbar_background;
                break;
            default:
                choosenRevealAnimationStartColor = R.color.color_standart_green_toolbar_background;
                break;
        }

        return choosenRevealAnimationStartColor;
    }

    public static int getRevealAnimationEndColor(@NonNull Context context) {

        int currentTheme = getCurrentTheme(context);
        int choosenRevealAnimationEndColor;
        switch (currentTheme){
            case R.style.Theme_StandartGreen:
                choosenRevealAnimationEndColor = R.color.color_standart_green_main_background;
                break;
            case R.style.Theme_StandartLemon:
                choosenRevealAnimationEndColor = R.color.color_standart_lemon_main_background;
                break;
            case R.style.Theme_StandartPink:
                choosenRevealAnimationEndColor = R.color.color_standart_pink_main_background;
                break;
            case R.style.Theme_StandartCornflower:
                choosenRevealAnimationEndColor = R.color.color_standart_cornflower_main_background;
                break;
            default:
                choosenRevealAnimationEndColor = R.color.color_standart_green_main_background;
                break;
        }

        return choosenRevealAnimationEndColor;
    }

    public static Drawable getCurrentContentScrimDawable(Context context) {

        Resources res = context.getResources();

        Drawable choosenContentScrimDrawable;
        int currentTheme = Common.getCurrentTheme(context);

        switch (currentTheme){
            case R.style.Theme_StandartGreen:
                choosenContentScrimDrawable = res.getDrawable(R.drawable.standart_green_content_scrim);
                break;
            case R.style.Theme_StandartLemon:
                choosenContentScrimDrawable = res.getDrawable(R.drawable.standart_lemon_content_scrim);
                break;
            case R.style.Theme_StandartPink:
                choosenContentScrimDrawable = res.getDrawable(R.drawable.standart_pink_content_scrim);
                break;
            case R.style.Theme_StandartCornflower:
                choosenContentScrimDrawable = res.getDrawable(R.drawable.standart_cornflower_content_scrim);
                break;
            default:
                choosenContentScrimDrawable = res.getDrawable(R.drawable.standart_green_content_scrim);
                break;
        }

        return choosenContentScrimDrawable;
    }

    public static Drawable getOrdinaryItemsDawable(Context context) {

        Resources res = context.getResources();

        Drawable choosenItemsDrawable;
        int currentTheme = Common.getCurrentTheme(context);

        switch (currentTheme){
            case R.style.Theme_StandartGreen:
                choosenItemsDrawable = res.getDrawable(R.drawable.standart_green_list_item_background);
                break;
            case R.style.Theme_StandartLemon:
                choosenItemsDrawable = res.getDrawable(R.drawable.standart_lemon_list_item_background);
                break;
            case R.style.Theme_StandartPink:
                choosenItemsDrawable = res.getDrawable(R.drawable.standart_pink_list_item_background);
                break;
            case R.style.Theme_StandartCornflower:
                choosenItemsDrawable = res.getDrawable(R.drawable.standart_cornflower_list_item_background);
                break;
            default:
                choosenItemsDrawable = res.getDrawable(R.drawable.standart_green_list_item_background);
                break;
        }

        return choosenItemsDrawable;
    }

    public static Drawable getOrdinaryVisitsItemsDawable(Context context) {

        Resources res = context.getResources();

        Drawable ordinaryVisitsItemsDrawable;
        int currentTheme = Common.getCurrentTheme(context);

        switch (currentTheme){
            case R.style.Theme_StandartGreen:
                ordinaryVisitsItemsDrawable = res.getDrawable(R.drawable.standart_green_cv_mcard_main_list_visits_item_background);
                break;
            case R.style.Theme_StandartLemon:
                ordinaryVisitsItemsDrawable = res.getDrawable(R.drawable.standart_lemon_cv_mcard_main_list_visits_item_background);
                break;
            case R.style.Theme_StandartPink:
                ordinaryVisitsItemsDrawable = res.getDrawable(R.drawable.standart_pink_cv_mcard_main_list_visits_item_background);
                break;
            case R.style.Theme_StandartCornflower:
                ordinaryVisitsItemsDrawable = res.getDrawable(R.drawable.standart_cornflower_cv_mcard_main_list_visits_item_background);
                break;
            default:
                ordinaryVisitsItemsDrawable = res.getDrawable(R.drawable.standart_green_list_item_background);
                break;
        }

        return ordinaryVisitsItemsDrawable;
    }

    public static Drawable getChoosenItemsDrawable(Context context) {

        Resources res = context.getResources();

        Drawable choosenItemsDrawable;
        int currentTheme = Common.getCurrentTheme(context);

        switch (currentTheme){
            case R.style.Theme_StandartGreen:
                choosenItemsDrawable = res.getDrawable(R.drawable.standart_green_list_item_selected_background);
                break;
            case R.style.Theme_StandartLemon:
                choosenItemsDrawable = res.getDrawable(R.drawable.standart_lemon_list_item_selected_background);
                break;
            case R.style.Theme_StandartPink:
                choosenItemsDrawable = res.getDrawable(R.drawable.standart_pink_list_item_selected_background);
                break;
            case R.style.Theme_StandartCornflower:
                choosenItemsDrawable = res.getDrawable(R.drawable.standart_cornflower_list_item_selected_background);
                break;
            default:
                choosenItemsDrawable = res.getDrawable(R.drawable.standart_green_list_item_selected_background);
                break;
        }

        return choosenItemsDrawable;
    }

    public static Snackbar getCustomSnackbar(Context context, View view, String message) {

        Snackbar mSnackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        View mSnackbarView = mSnackbar.getView();

        int alternativeThemeColor = Common.getAlternativeThemeColor(context.getApplicationContext());
        mSnackbarView.setBackgroundColor(alternativeThemeColor);

        return mSnackbar;
    }

    public static Boolean copyFile(String sourcePath, String destPath) {
        FileInputStream is = null;
        FileOutputStream os = null;
        byte[] buf = null;
        try {
            is = new FileInputStream(sourcePath);
            os = new FileOutputStream(destPath);
            int nLength;
            buf = new byte[16000000];
            while (true) {
                nLength = is.read(buf);
                if (nLength < 0) {
                    break;
                }
                os.write(buf, 0, nLength);
            }
            return true;
        } catch (IOException ex) {

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ex) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception ex) {
                }
            }
            if (buf != null) {
                try {
                    buf = null;
                } catch (Exception ex) {
                }
            }
        }
        return false;
    }

    public static Boolean deleteFile (String filePath){
        File currentFile = new File(filePath);
        currentFile.delete();
        return false;
    }

    /**
     * Compresses a set of files contained in a directory into a zip file.
     *
     * @param toFile the zip file.
     * @param fromDir the directory which contains the files to be compressed.
     */
    public static void compress(File toFile, File fromDir) {
        try {
            if (fromDir == null || !fromDir.exists() || !fromDir.isDirectory()) {

            }

            FileOutputStream fos = new FileOutputStream(toFile);
            ZipOutputStream outs = new ZipOutputStream(fos);

//            FileSystem fs = new FileSystem(fromDir);
//            Iterator allFiles = fs.getFiles(true).iterator();

            File[] filesList = fromDir.listFiles();
            for (File curFile : filesList) {

//            while (allFiles.hasNext()) {
//                File srcFile = (File) allFiles.next();
                String filepath = curFile.getAbsolutePath();
                String dirpath = fromDir.getAbsolutePath();
                String entryName = filepath.substring(dirpath.length() + 1)
                        .replace('\\', '/');
                ZipEntry zipEntry = new ZipEntry(entryName);
                //zipEntry.setTime(curFile.lastModified());
                //zipEntry.setSize(curFile.getTotalSpace());

                FileInputStream insPre = new FileInputStream(curFile);
                zipEntry.setSize(insPre.available());
                insPre.close();

                FileInputStream ins = new FileInputStream(curFile);

                outs.putNextEntry(zipEntry);

/*
                FileChannel src = ins.getChannel();
                FileChannel dst = fos.getChannel();
                dst.transferFrom(src, 0, src.size());
*/

                // считываем содержимое файла в массив byte
                byte[] buffer = new byte[ins.available()];
                ins.read(buffer);
                // добавляем содержимое к архиву
                outs.write(buffer);
                // закрываем текущую запись для новой записи
                outs.closeEntry();
                ins.close();
            }

            outs.close();

        }
        catch (Exception e) {
            String s = e.getMessage();
        }
    }


    public static void decompress(File fileFrom, File dirTo) {

        try
        {
            ZipFile zip = new ZipFile(fileFrom);
            Enumeration entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                System.out.println(entry.getName());

                InputStream in = zip.getInputStream(entry);
                OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(dirTo, entry.getName())));

                //byte[] buffer = new byte[1024];
                byte[] buffer = new byte[(int) entry.getSize()];
                int len;
                while ((len = in.read(buffer)) >= 0)
                    out.write(buffer, 0, len);
                out.close();
                in.close();

            }

            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

/*
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileFrom))) {
            ZipEntry entry;
            String currFileName;
            long currFileSize;
            while((entry=zis.getNextEntry())!=null){

                currFileName = entry.getName(); // получим название файла
                currFileSize=entry.getSize();  // получим его размер в байтах
                System.out.printf("Название: %s \t размер: %d \n", currFileName, currFileSize);

                OutputStream fos = new FileOutputStream(dirTo + "/" + currFileName);

                byte[] buffer = new byte[9241024];
                int count;
                while ((count = zis.read(buffer)) > -1) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                zis.closeEntry();

*/
/*
                try {
                    FileChannel src = new FileInputStream(String.valueOf(entry)).getChannel();
                    FileChannel dst = new FileOutputStream(dirTo + entry.getName()).getChannel();
                    dst.transferFrom(src, 0, src.currFileSize());
                    src.close();
                    dst.close();
                } catch (Exception e) {
                }
*//*


            }
        }
        catch(Exception ex){

            System.out.println(ex.getMessage());
        }
*/
    }

    public static Boolean checkSDcard(){
/*
        String sdState = Environment.getExternalStorageState();
        if ((sdState.equals(Environment.MEDIA_MOUNTED))&&(Environment.isExternalStorageRemovable())) {
            return true;
        }
*/
        String path = System.getenv("SECONDARY_STORAGE");
        if (path == null) {
            return false;
        }
        return true;
    }

    public static String getSDcardPath() throws Exception{

        String path = System.getenv("SECONDARY_STORAGE");
        if (path == null){
            return "/";
        }

        File files = new File(path);
        if ((files == null)|(!files.isDirectory())) {
            return "/";
        }

        if (getTotalMemorySize(path).equals("0")){
            return "/";
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT){
            path = System.getenv("SECONDARY_STORAGE") + "/Android/data/ru.kai.mcard/files/Pictures/";
            File dir = new File(path);
            if (!dir.exists()){
                dir.mkdirs();
            }
            return path;
        }

        return System.getenv("SECONDARY_STORAGE");
    }

    public static String getTotalMemorySize(String path){
        double totalSize;

        StatFs statFs = new StatFs(path);

        if (Build.VERSION.SDK_INT >= 18) {
            totalSize = statFs.getBlockCountLong() * statFs.getBlockSizeLong();
        } else {
            totalSize = statFs.getBlockCount() * statFs.getBlockSize();
        }

        NumberFormat numberFormat = NumberFormat.getInstance();
        // disable grouping
        numberFormat.setGroupingUsed(false);
        // display numbers with two decimal places
        numberFormat.setMaximumFractionDigits(2);

/*
        outputInfo += "Размер в гигабайтах: "
                + numberFormat.format((totalSize / (double) 1073741824))
                + " GB \n" + "Размер в мегабайтах: "
                + numberFormat.format((totalSize / (double) 1048576))
                + " MB \n" + "Размер в килобайтах: "
                + numberFormat.format((totalSize / (double) 1024))
                + " KB \n" + "Размер в байтах: "
                + numberFormat.format(totalSize) + " B \n";
*/

        return numberFormat.format((totalSize / (double) 1048576));

    }

    public static String getFreeMemorySize(String path){
        double freeSpace;

        StatFs statFs = new StatFs(path);

        if (Build.VERSION.SDK_INT >= 18) {
            freeSpace = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
        } else {
            freeSpace = statFs.getAvailableBlocks() * statFs.getBlockSize();
        }

        NumberFormat numberFormat = NumberFormat.getInstance();
        // disable grouping
        numberFormat.setGroupingUsed(false);
        // display numbers with two decimal places
        numberFormat.setMaximumFractionDigits(2);

/*
        outputInfo += "Размер в гигабайтах: "
                + numberFormat.format((freeSpace / (double) 1073741824))
                + " GB \n" + "Размер в мегабайтах: "
                + numberFormat.format((freeSpace / (double) 1048576))
                + " MB \n" + "Размер в килобайтах: "
                + numberFormat.format((freeSpace / (double) 1024))
                + " KB \n" + "Размер в байтах: "
                + numberFormat.format(freeSpace) + " B \n";
*/

        return numberFormat.format((freeSpace / (double) 1048576));

    }

    // интерфейс для возрата ID визита во фрагмент, использующийся в этом визите
    public interface OnGetVisitsID{
        int onGetVisitsID();
    }

    public static int getCurrentTheme(Context context){
        int newTheme = R.style.Theme_StandartGreen;
        String newThemeLitera = Constants.THEME_LITERA_G;
        // настройки
        SharedPreferences mSettings = context.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        //int currentTheme = R.style.Theme_StandartGreen;
        if (mSettings.contains(Constants.APP_PREFERENCES_CURRENT_THEME_S)) {
            // Получаем число из настроек
            try {
                newThemeLitera = mSettings.getString(Constants.APP_PREFERENCES_CURRENT_THEME_S, Constants.THEME_LITERA_G);
            }catch (Exception e){}
        }

        switch (newThemeLitera){
            case Constants.THEME_LITERA_G:
                newTheme = R.style.Theme_StandartGreen;
                break;
            case Constants.THEME_LITERA_L:
                newTheme = R.style.Theme_StandartLemon;
                break;
            case Constants.THEME_LITERA_P:
                newTheme = R.style.Theme_StandartPink;
                break;
            case Constants.THEME_LITERA_C:
                newTheme = R.style.Theme_StandartCornflower;
                break;
        }
        return newTheme;
    }

}
