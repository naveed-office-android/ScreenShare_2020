package com.screen.share.newone.record;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxBusRecord {

    public void post(Object o) {
        mBus.onNext(o);
    }

    private final Subject<Object> mBus;

    public <T> Observable<T> toObservable(Class<T> eventType) {
        return mBus.ofType(eventType);
    }

    public static RxBusRecord getDefault() {
        return RxBusHolder.sInstance;
    }

    private static class RxBusHolder {
        private static final RxBusRecord sInstance = new RxBusRecord();
    }

    private RxBusRecord() {
        mBus = PublishSubject.create();
    }
}