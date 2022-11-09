package ru.kai.mcard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

/**
 * Created by akabanov on 27.03.2017. //
 */

// Анимационный диалог
public class PryvacyPolicyActivityAccept extends AppCompatActivity implements View.OnClickListener{
    private RelativeLayout pp_main;
    private final float aFloat1 = 1.0f;
    private final float float08 = 0.8f;

    // настройки
    private SharedPreferences mSettings;

    private Button btnPPCancel, btnPPOK;
    private CheckBox chb_PPAccept;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pryvacy_policy_accept);

        mSettings = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);

        btnPPCancel = (Button)findViewById(R.id.btnPPCancel);
        if (btnPPCancel != null) {
            btnPPCancel.setOnClickListener(this);
        }
        btnPPOK = (Button)findViewById(R.id.btnPPOK);
        if (btnPPOK != null) {
            btnPPOK.setOnClickListener(this);
        }
        chb_PPAccept = (CheckBox)findViewById(R.id.chb_PPAccept);
        if (chb_PPAccept != null) {
            chb_PPAccept.setOnClickListener(this);
        }

        pp_main = (RelativeLayout)findViewById(R.id.pp_main);

        getInitialAnimator().start();

    }

    public void ppOpen(View view) {
        Intent questionIntent = new Intent(PryvacyPolicyActivityAccept.this, PryvacyPolicyActivity.class);
        startActivityForResult(questionIntent, 1);
        overridePendingTransition(R.anim.pp_accept_alfa0,R.anim.pp_accept_alfa);

    }

    private AnimatorSet getInitialAnimator(){
        AnimatorSet set1 = new AnimatorSet();
        set1.setDuration(1000).playTogether(
                ObjectAnimator.ofFloat(pp_main, View.ALPHA, 0f, aFloat1),
                ObjectAnimator.ofFloat(pp_main, View.SCALE_X, 0.2f, aFloat1),
                ObjectAnimator.ofFloat(pp_main, View.SCALE_Y, 0.2f, aFloat1),
                ObjectAnimator.ofFloat(pp_main, "rotationY", 0f, 180f)
        );

        AnimatorSet set2 = new AnimatorSet();
        set2.playTogether(
                ObjectAnimator.ofFloat(pp_main, View.SCALE_X, aFloat1, float08),
                ObjectAnimator.ofFloat(pp_main, View.SCALE_Y, aFloat1, float08)
        );
        AnimatorSet set3 = new AnimatorSet();
        set3.setDuration(1000).playTogether(
                set2,
                ObjectAnimator.ofFloat(pp_main, "rotationY", 180f, 0f)
        );

        AnimatorSet set4 = new AnimatorSet();
        set4.setDuration(1000).playTogether(
                ObjectAnimator.ofFloat(pp_main, View.SCALE_X, float08, aFloat1),
                ObjectAnimator.ofFloat(pp_main, View.SCALE_Y, float08, aFloat1)
        );
        //set4.setInterpolator(new AccelerateInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(
                set1,
                set3,
                set4
        );

        return set;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnPPCancel:
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
                break;
            case R.id.btnPPOK:
                // Запоминаем, приняли или нет...
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putBoolean(Constants.APP_PREFERENCES_SHOW_PRIVACY_POLICY, false); // false - не показывать больше это окно
                editor.apply();

                Intent intent1 = new Intent();
                setResult(RESULT_OK, intent1);
                finish();
                break;
            case R.id.chb_PPAccept:
                // с анимацией:
/*
                // get the center for the clipping circle
                int cx = (btnPPOK.getLeft() + btnPPOK.getRight()) / 2;
                int cy = (btnPPOK.getTop() + btnPPOK.getBottom()) / 2;

                if (chb_PPAccept.isChecked()){
                    // get the final radius for the clipping circle
                    int finalRadius = Math.max(btnPPOK.getWidth(), btnPPOK.getHeight());

                    // create the animator for this view (the start radius is zero)
                    Animator anim =
                            ViewAnimationUtils.createCircularReveal(btnPPOK, cx, cy, 0, finalRadius);

                    // make the view visible and start the animation
                    btnPPOK.setEnabled(true);
                    anim.start();
                }else{
                    // get the initial radius for the clipping circle
                    int initialRadius = btnPPOK.getWidth();

                    // create the animation (the final radius is zero)
                    Animator anim =
                            ViewAnimationUtils.createCircularReveal(btnPPOK, cx, cy, initialRadius, 0);

                    // make the view invisible when the animation is done
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            btnPPOK.setEnabled(false);
                        }
                    });
                    // start the animation
                    anim.start();
                }
*/
                // анимация краткосрочно увеличивает кнопку и сразу возвращает ее размеры на место.
                AnimatorSet set1 = new AnimatorSet();
                set1.setDuration(200).playTogether(
                        ObjectAnimator.ofFloat(btnPPOK, View.SCALE_X, aFloat1, 2.5f),
                        ObjectAnimator.ofFloat(btnPPOK, View.SCALE_Y, aFloat1, 2.5f)
                );
                AnimatorSet set2 = new AnimatorSet();
                set2.setDuration(200).playTogether(
                        ObjectAnimator.ofFloat(btnPPOK, View.SCALE_X, 2.5f, aFloat1),
                        ObjectAnimator.ofFloat(btnPPOK, View.SCALE_Y, 2.5f, aFloat1)
                );
                AnimatorSet set = new AnimatorSet();
                set.playSequentially(
                        set1,
                        set2
                );
                set.start();

                btnPPOK.setEnabled(chb_PPAccept.isChecked());

                break;

        }
    }
}
