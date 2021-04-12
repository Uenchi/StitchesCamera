package com.uenchi.stitchescamera.camera.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cgfay.uitls.bean.MusicData;
import com.cgfay.uitls.utils.StringUtils;
import com.uenchi.stitchescamera.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : uenchi
 * @date : 2021/4/12
 * @desc :
 */
public class MusicAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnMusicItemSelectedListener mListener;
    private List<MusicData> mMusicData = new ArrayList<>();

    public MusicAdapter(List<MusicData> musicData) {
        if (null == musicData) {
            mMusicData = new ArrayList<>();
        } else {
            mMusicData = musicData;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(com.cgfay.utilslibrary.R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MusicViewHolder viewHolder = (MusicViewHolder) holder;
        MusicData musicData = mMusicData.get(position);
        viewHolder.mTextName.setText(musicData.getName());
        viewHolder.mTexDuration.setText(StringUtils.generateMillisTime((int) musicData.getDuration()));
        viewHolder.mLayoutMusic.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onMusicItemSelected(musicData);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMusicData.size();
    }

    public void setMusicData(List<MusicData> musicData) {
        if (null == musicData) {
            mMusicData = new ArrayList<>();
        } else {
            mMusicData = musicData;
        }
        notifyDataSetChanged();
    }

    /**
     * 设置选中音乐监听器
     *
     * @param listener
     */
    public void setOnMusicSelectedListener(OnMusicItemSelectedListener listener) {
        mListener = listener;
    }

    /**
     * 音乐选中监听器
     */
    public interface OnMusicItemSelectedListener {
        // 选中音乐
        void onMusicItemSelected(MusicData musicData);
    }

    class MusicViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLayoutMusic;
        private TextView mTextName;
        private TextView mTexDuration;

        public MusicViewHolder(View itemView) {
            super(itemView);
            mLayoutMusic = itemView.findViewById(R.id.layout_item_music);
            mTextName = itemView.findViewById(R.id.tv_music_name);
            mTexDuration = itemView.findViewById(R.id.tv_music_duration);
        }
    }
}