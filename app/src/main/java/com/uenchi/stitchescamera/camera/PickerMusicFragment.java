package com.uenchi.stitchescamera.camera;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgfay.uitls.bean.MusicData;
import com.cgfay.uitls.scanner.LocalMusicScanner;
import com.uenchi.stitchescamera.R;
import com.uenchi.stitchescamera.camera.adapter.MusicAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : uenchi
 * @date : 2021/4/12
 * @desc :
 */
public class PickerMusicFragment  extends AppCompatDialogFragment implements LocalMusicScanner.MusicScanCallbacks,
        MusicAdapter.OnMusicItemSelectedListener {

    public static final String TAG = "MusicPickerFragment";

    private FragmentActivity mActivity;

    private LocalMusicScanner mMusicScanner;
    private RecyclerView mRecyclerView;
    private MusicAdapter mAdapter;
    private View mLlvMusic;
    private TextView mMusic;
    private OnMusicSelectedListener mMusicSelectedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActivity) {
            mActivity = (FragmentActivity) context;
        } else if (getActivity() != null) {
            mActivity = getActivity();
        }
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.MusicDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picker_music, container, false);
        initView(view);
        return view;
    }

    private void initView(@NonNull View view) {
        mRecyclerView = view.findViewById(R.id.music_list);
        mLlvMusic = view.findViewById(R.id.llvMusic);
        mMusic = view.findViewById(R.id.music);

        view.findViewById(R.id.iv_close).setOnClickListener(v -> {
            if (mMusicSelectedListener != null) {
                mMusicSelectedListener.onMusicSelectClose();
            }
        });

        view.findViewById(R.id.delMusic).setOnClickListener(v -> {
            if (mMusicSelectedListener != null) {
                mMusicSelectedListener.onMusicDelete();
            }
        });

        if (null != mMusicSelectedListener) {
            MusicData musicData = mMusicSelectedListener.onInitMusicData();
            if (null != musicData) {
                mLlvMusic.setVisibility(View.VISIBLE);
                mMusic.setText(musicData.getName());
            } else {
                mLlvMusic.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new MusicAdapter(new ArrayList<>());
        mAdapter.setOnMusicSelectedListener(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
        mMusicScanner = new LocalMusicScanner(getActivity(), this);
        mMusicScanner.scanLocalMusic();
    }

    @Override
    public void onResume() {
        super.onResume();
        initDismissListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMusicScanner.destroy();
    }

    private void initDismissListener() {
        if (getDialog() != null) {
            getDialog().setOnDismissListener(dialog -> {
                closeFragment();
            });
        }
    }

    /**
     * 关闭当前页面
     */
    private void closeFragment() {
        if (getParentFragment() != null) {
            getParentFragment()
                    .getChildFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commitAllowingStateLoss();
        } else if (mActivity != null) {
            mActivity.getSupportFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onMusicScanFinish(Cursor cursor) {
        List<MusicData> musicData = new ArrayList<>();
        if (null != cursor && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String sbr = path.substring(path.length() - 3);
                if ("mp3".equals(sbr) && duration >= 60 * 1000) {
                    MusicData music = new MusicData(id, name, path, duration);
                    musicData.add(music);
                }
            } while (cursor.moveToNext());
        }
        mAdapter.setMusicData(musicData);
    }

    @Override
    public void onMusicScanReset() {
        mAdapter.setMusicData(new ArrayList<>());
    }

    @Override
    public void onMusicItemSelected(MusicData musicData) {
        if (mMusicSelectedListener != null) {
            mMusicSelectedListener.onMusicSelected(musicData);
        }
    }

    /**
     * 音乐选中监听器
     */
    public interface OnMusicSelectedListener {
        MusicData onInitMusicData();

        void onMusicSelectClose();

        void onMusicSelected(MusicData musicData);

        void onMusicDelete();
    }

    /**
     * 添加音乐选中监听器
     *
     * @param listener
     */
    public void addOnMusicSelectedListener(PickerMusicFragment.OnMusicSelectedListener listener) {
        mMusicSelectedListener = listener;
    }
}