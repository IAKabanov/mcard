package ru.kai.mcard.domain.interactor;

import io.reactivex.observers.DisposableObserver;
import io.reactivex.subscribers.DisposableSubscriber;

/**
 * Default {@link DisposableObserver} base class to be used whenever you want default error handling.
 */
public class CustDefaultSubscriber<T> extends DisposableSubscriber<T> {
  @Override public void onNext(T t) {
    // no-op by default.
  }

  @Override public void onComplete() {
    // no-op by default.
  }

  @Override public void onError(Throwable exception) {
    // no-op by default.
  }
}
