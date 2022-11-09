package ru.kai.mcard;
// Google Analytics

/**
 * Created by akabanov on 05.05.2016.
 */

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import ru.kai.mcard.di.pack.Application.DaggerIAppComponent;
import ru.kai.mcard.di.pack.Application.IAppComponent;
import ru.kai.mcard.di.pack.Application.AppModule;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */

// класс для работы с Google Analitics. Прописан как родительский в манифесте.

public class MCardApplication extends Application {
    private Tracker mTracker;

    private IAppComponent appComponent;
    //private SQLiteDatabase db;
    private static MCardApplication instance;

    private RefWatcher refWatcher = null; // for LeakCanary

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    // процедура добавлена из-за ошибки: Unable to get provider com.google.firebase.provider.FirebaseInitProvider: java.lang.ClassNotFoundException: com.google.firebase.provider.FirebaseInitProvider
    // также добавлена зависимость в gradle app-level
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        this.initializeAppComponent();

        // fonts - calligraphy library
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-RobotoRegular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        if (Constants.DEBUG_MODE) {
            refWatcher = LeakCanary.install(this);

            // Stetho start *************************************************************************
            // Gradle dependency on Stetho (debug with Google Chrome development tools)
            // Create an InitializerBuilder
            Stetho.InitializerBuilder initializerBuilder =
                    Stetho.newInitializerBuilder(this);

            // Enable Chrome DevTools
            initializerBuilder.enableWebKitInspector(
                    Stetho.defaultInspectorModulesProvider(this)
            );

            // Enable command line interface
            initializerBuilder.enableDumpapp(
                    Stetho.defaultDumperPluginsProvider(this)
            );

            // Use the InitializerBuilder to generate an Initializer
            Stetho.Initializer initializer = initializerBuilder.build();

            // Initialize Stetho with the Initializer
            Stetho.initialize(initializer);
            // Stetho end *************************************************************************
        }

/*
        DBHelper dbHelperInstance = DBHelper.getInstance(this);
        db = dbHelperInstance.open();
        DBMethodsS dbMethodsS = DBMethodsS.getInstance(db);
*/
    }

    private void initializeAppComponent(){
        this.appComponent = DaggerIAppComponent.builder()
                .appModule(new AppModule(this.getApplicationContext()))
                .build();
    }

    public static MCardApplication getInstance(){
        return instance;
    }

    public IAppComponent getAppComponent(){
        return this.appComponent;
    }

    // for LeakCanary
    public static RefWatcher getRefWatcher(Context context) {
        MCardApplication application = (MCardApplication) context.getApplicationContext();
        return application.refWatcher;
/*
        // than in Fragment:
        @Override public void onDestroy() {
            super.onDestroy();
            RefWatcher refWatcher = ExampleApplication.getRefWatcher(getActivity());
            refWatcher.watch(this);
        }
*/
    }


}
