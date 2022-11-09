package ru.kai.mcard.domain.interactor;

import java.util.concurrent.Executor;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import ru.kai.mcard.utility.Utility;

/**
 * Abstract class for a Use Case (Interactor in terms of Clean Architecture).
 * This interface represents a execution unit for different use cases (this means any use case
 * in the application should implement this contract).
 *
 * By convention each UseCase implementation will return the result using a {@link DisposableObserver}
 * that will execute its job in a background thread and will post the result in the UI thread.
 */
public abstract class BaseUseCase<T, Params> {

  private final CompositeDisposable disposables;

  public BaseUseCase() {
    this.disposables = new CompositeDisposable();
  }

  /**
   * Builds an {@link Flowable} which will be used when executing the current {@link BaseUseCase}.
   */
  public abstract Flowable<T> buildUseCaseFlowable(Params params);
  /**
   * Executes the current use case.
   *
   * @param subscriber {@link DisposableObserver} which will be listening to the observable build
   * by {@link #buildUseCaseFlowable(Params)} ()} method.
   * @param params Parameters (Optional) used to build/execute this use case.
   */
  public void execute(DisposableSubscriber<T> subscriber, Params params) {
    Utility.checkNotNull(subscriber);

    final Flowable<T> flowable = this.buildUseCaseFlowable(params)
            .subscribeOn(Schedulers.io())
//        .subscribeOn(Schedulers.from(new ThreadPerTaskExecutor()))
            .observeOn(AndroidSchedulers.mainThread());
    addDisposable(flowable.subscribeWith(subscriber));
  }

  /**
   * Dispose from current {@link CompositeDisposable}.
   */
  public void dispose() {
    if (!disposables.isDisposed()) {
      disposables.dispose();
    }
  }

  /**
   * Dispose from current {@link CompositeDisposable}.
   */
  private void addDisposable(Disposable disposable) {
    Utility.checkNotNull(disposable);
    Utility.checkNotNull(disposables);
    disposables.add(disposable);
  }

  class ThreadPerTaskExecutor implements Executor {
    public void execute(Runnable r) {
      new Thread(r).start();
    }
  }
}
