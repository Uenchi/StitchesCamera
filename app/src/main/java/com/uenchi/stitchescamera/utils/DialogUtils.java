package com.uenchi.stitchescamera.utils;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.uenchi.stitchescamera.R;
import com.uenchi.stitchescamera.view.LoadingView;

import per.goweii.anylayer.AnyLayer;
import per.goweii.anylayer.dialog.DialogLayer;
import per.goweii.anylayer.utils.AnimatorHelper;
import per.goweii.anylayer.widget.SwipeLayout;

public class DialogUtils {

    public static DialogUtils instance = null;

    public static DialogUtils getInstance() {
        if (instance == null) {
            synchronized (DialogUtils.class) {
                if (instance == null) {
                    instance = new DialogUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 加载中对话框
     */
    private Dialog mLoadingDialog;
    private LoadingView loadingView;

    public void showLoading(Context mContext) {
        if (EmptyUtils.isNotEmpty(mLoadingDialog) && !mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
            return;
        }

        if (EmptyUtils.isNotEmpty(mLoadingDialog) && mLoadingDialog.isShowing()) return;

        mLoadingDialog = createDialogCenter(mContext, R.layout.dialog_loading);

        Window window = mLoadingDialog.getWindow();
        window.setDimAmount(0f);

        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setCancelable(false);

        loadingView = mLoadingDialog.findViewById(R.id.loadingView);
        mLoadingDialog.show();
        loadingView.start();
    }

    public void dismissLoading() {
        if (EmptyUtils.isEmpty(mLoadingDialog)) return;
        loadingView.stop();
        mLoadingDialog.dismiss();

        loadingView = null;
        mLoadingDialog = null;
    }

    public boolean isLoading() {
        if (EmptyUtils.isNotEmpty(mLoadingDialog) && mLoadingDialog.isShowing()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 创建底部对话框
     *
     * @param mContext
     * @param resource --布局
     * @return
     */
    public Dialog createDialogBottom(Context mContext, int resource) {
        Dialog mDialog = createDialogCenter(mContext, resource);
        mDialog.getWindow().setGravity(Gravity.BOTTOM);
        mDialog.getWindow().setWindowAnimations(R.style.DialogAnimTranslateInStyle);
        return mDialog;
    }


    public DialogLayer createDialogBottom(Context context, View view) {
        return AnyLayer.dialog(context)
                .contentView(view)
                .backgroundDimAmount(0.7f)
                .cancelableOnTouchOutside(true)
                .swipeDismiss(SwipeLayout.Direction.BOTTOM)
                .contentAnimator(new DialogLayer.AnimatorCreator() {
                    @Override
                    public Animator createInAnimator(View content) {
                        return AnimatorHelper.createBottomInAnim(content);
                    }

                    @Override
                    public Animator createOutAnimator(View content) {
                        return AnimatorHelper.createBottomOutAnim(content);
                    }
                });
    }

    /**
     * 创建头部对话框
     *
     * @param mContext
     * @param resource --布局
     * @return
     */
    public Dialog createDialogTop(Context mContext, int resource) {
        Dialog mDialog = createDialogCenter(mContext, resource);
        mDialog.getWindow().setGravity(Gravity.TOP);
        mDialog.getWindow().setWindowAnimations(0);
        return mDialog;
    }

    /**
     * 创建右侧对话框
     *
     * @param mContext
     * @param resource
     * @return
     */
    public Dialog createDialogRight(Context mContext, int resource) {
        Dialog mDialog = createDialogCenter(mContext, resource);
        mDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mDialog.getWindow().setGravity(Gravity.RIGHT);
        mDialog.getWindow().setWindowAnimations(R.style.DialogAnimRightTranslateInStyle);
        return mDialog;
    }

    /**
     * 创建中间对话宽
     *
     * @param mContext
     * @param resource --布局
     * @return
     */
    public Dialog createDialogCenter(Context mContext, int resource) {
        Dialog mDialog = new Dialog(mContext, R.style.DialogStyle);
        View vTmp = LayoutInflater.from(mContext).inflate(resource, null);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setContentView(vTmp);
        Window window = mDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        window.setDimAmount(0.7f);
        return mDialog;
    }
}
