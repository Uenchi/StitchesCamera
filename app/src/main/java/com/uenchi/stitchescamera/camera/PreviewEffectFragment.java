package com.uenchi.stitchescamera.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgfay.camera.camera.CameraParam;
import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.cgfay.filter.glfilter.resource.FilterHelper;
import com.cgfay.filter.glfilter.resource.ResourceJsonCodec;
import com.cgfay.filter.glfilter.resource.bean.ResourceData;
import com.uenchi.stitchescamera.R;
import com.uenchi.stitchescamera.camera.adapter.FilterAdapter;

import java.io.File;

/**
 * @author : uenchi
 * @date : 2021/4/9
 * @desc :
 */
public class PreviewEffectFragment  extends Fragment {

    // 内容显示列表
    private View mContentView;

    // 布局管理器
    private LayoutInflater mInflater;
    private Activity mActivity;
    // 相机参数
    private CameraParam mCameraParam;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        } else {
            mActivity = getActivity();
        }
        mInflater = LayoutInflater.from(context);
        mCameraParam = CameraParam.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null == mContentView) {
            mContentView = inflater.inflate(R.layout.fragment_filter, container, false);
            initView(mContentView);
        }
        return mContentView;
    }

    private void initView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        View del = view.findViewById(R.id.vDel);
        View bg = view.findViewById(R.id.vBG);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));

        FilterAdapter filterAdapter = new FilterAdapter(getContext(), FilterHelper.getFilterList());
        recyclerView.setAdapter(filterAdapter);

        filterAdapter.setFilterChangeListener(new FilterAdapter.OnFilterChangeListener() {
            @Override
            public void onFilterChanged(ResourceData resourceData) {
                if (mActivity == null) {
                    return;
                }
                del.setSelected(true);

                if (!resourceData.name.equals("none")) {
                    String folderPath = FilterHelper.getFilterDirectory(mActivity) + File.separator + resourceData.unzipFolder;
                    DynamicColor color = null;
                    try {
                        color = ResourceJsonCodec.decodeFilterData(folderPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (null != mCallback) {
                        mCallback.onFilterSelect(color);
                    }
                } else {
                    if (null != mCallback) {
                        mCallback.onFilterSelect(null);
                    }
                }
            }
        });

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, getResources().getDisplayMetrics());
                outRect.right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, getResources().getDisplayMetrics());
            }
        });

        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (del.isSelected()) {
                    if (null != mCallback) {
                        mCallback.onDeleteFilter();
                        filterAdapter.clean();
                        del.setSelected(false);
                    }
                }
            }
        });

        bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mCallback) {
                    mCallback.onClose();
                }
            }
        });
    }

    private Callback mCallback;

    public void setCallback(PreviewEffectFragment.Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onFilterSelect(DynamicColor color);

        void onDeleteFilter();

        void onClose();
    }

}