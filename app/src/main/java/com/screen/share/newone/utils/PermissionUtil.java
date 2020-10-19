package com.screen.share.newone.utils;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class PermissionUtil {
    public interface PermissionsListener {
        void onGranted();
    }

    public static void requestPermission(final Activity activity, final PermissionsListener listener, final String... permissions) {
        RxPermissions rxPermissions = new RxPermissions((FragmentActivity) activity);
        rxPermissions.request(permissions)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            listener.onGranted();
                        } else {
                            Toast.makeText(activity, "Permission Needed", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
