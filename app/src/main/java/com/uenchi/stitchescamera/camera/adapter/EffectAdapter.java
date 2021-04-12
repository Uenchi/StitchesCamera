package com.uenchi.stitchescamera.camera.adapter;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cgfay.uitls.utils.BitmapUtils;
import com.uenchi.editlibrary.effect.EffectFilterType;
import com.uenchi.stitchescamera.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : uenchi
 * @date : 2021/4/12
 * @desc :
 */
public class EffectAdapter extends RecyclerView.Adapter<EffectAdapter.ImageHolder> {

    private Context mContext;
    private int mSelected = -1;
    private List<EffectFilterType> mEffectList = new ArrayList<>();
    private GestureDetector mGestureDetector;
    private boolean isAdding = false;
    private View mPressView;

    public EffectAdapter(Context context, List<EffectFilterType> effectTypeList) {
        mContext = context;
        mEffectList.addAll(effectTypeList);
        mGestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onShowPress(MotionEvent e) {
                super.onShowPress(e);
                if (isAdding) return;
                if (null != mPressView) {
                    ImageHolder holder = (ImageHolder) mPressView.getTag();
                    int position = holder.getAdapterPosition();
                    if (null != mEffectChangeListener) {
                        mEffectChangeListener.onBeginEffectChanged(mEffectList.get(position));
                    }
                    isAdding = true;
                }
            }
        });
    }

    /**
     * 切换特效数据
     *
     * @param effectTypeList
     */
    public void changeEffectData(List<EffectFilterType> effectTypeList) {
        mEffectList.clear();
        if (effectTypeList != null) {
            mEffectList.addAll(effectTypeList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_effect_view, parent, false);
        ImageHolder viewHolder = new ImageHolder(view);
        viewHolder.effectName = view.findViewById(R.id.item_effect_name);
        viewHolder.effectImage = view.findViewById(R.id.item_effect_image);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageHolder holder, final int position) {
        if (mEffectList.get(position).getThumb().startsWith("assets://")) {
            holder.effectImage.setImageBitmap(BitmapUtils.getImageFromAssetsFile(mContext,
                    mEffectList.get(position).getThumb().substring("assets://".length())));
        } else {
            holder.effectImage.setImageBitmap(BitmapUtils.getBitmapFromFile(mEffectList.get(position).getThumb()));
        }
        holder.effectName.setText(mEffectList.get(position).getName());
        holder.itemView.setTag(holder);
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                int actionMasked = event.getActionMasked();
                switch (actionMasked) {
                    case MotionEvent.ACTION_DOWN:
                        if (isAdding) {
                            return false;
                        }
                        if (mPressView == null) {
                            mPressView = v;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_POINTER_UP:
                        if (v != mPressView) {
                            return true;
                        }

                        ImageHolder holder = (ImageHolder) mPressView.getTag();
                        int position = holder.getAdapterPosition();
                        if (null != mEffectChangeListener) {
                            mEffectChangeListener.onEndEffectChanged(mEffectList.get(position));
                        }
                        isAdding = false;
                        mPressView = null;

                        break;
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mEffectList == null) ? 0 : mEffectList.size();
    }

    class ImageHolder extends RecyclerView.ViewHolder {
        // 预览缩略图
        public ImageView effectImage;
        // 预览文字
        public TextView effectName;

        public ImageHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * 特效改变监听器
     */
    public interface OnEffectChangeListener {

        void onBeginEffectChanged(EffectFilterType effectType);

        void onEndEffectChanged(EffectFilterType effectType);
    }

    public void setOnEffectChangeListener(OnEffectChangeListener listener) {
        mEffectChangeListener = listener;
    }

    private OnEffectChangeListener mEffectChangeListener;
}