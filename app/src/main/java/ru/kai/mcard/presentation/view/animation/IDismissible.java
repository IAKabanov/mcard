package ru.kai.mcard.presentation.view.animation;

//We use this to remove the Fragment only when the animation finished
public interface IDismissible {
    interface OnDismissedListener {
        void onDismissed();
    }

    void dismiss(OnDismissedListener listener);
}
