package com.uenchi.stitchescamera.camera.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cgfay.filter.glfilter.resource.bean.ResourceData;
import com.cgfay.uitls.utils.BitmapUtils;
import com.uenchi.stitchescamera.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : uenchi
 * @date : 2021/4/9
 * @desc :
 */
public class FilterAdapter  extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {

    private Context mContext;
    private int mSelectPosition = -1;
    private List<ResourceData> mFilterDataList = new ArrayList<>();

    public FilterAdapter(Context context, List<ResourceData> filterDataList) {
        mContext = context;
        mFilterDataList = filterDataList;
    }

    @NonNull
    @Override
    public FilterAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_filter, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.imageView = view.findViewById(R.id.filter_thumbnail);
        viewHolder.textView = view.findViewById(R.id.filter_name);
        viewHolder.selectView = view.findViewById(R.id.select_view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FilterAdapter.ViewHolder holder, int position) {
        ResourceData resourceData = mFilterDataList.get(position);
        if (resourceData.thumbPath.startsWith("assets://")) {
            holder.imageView.setImageBitmap(BitmapUtils.getImageFromAssetsFile(mContext,
                    mFilterDataList.get(position).thumbPath.substring("assets://".length())));
        } else {
            holder.imageView.setImageBitmap(BitmapUtils.getBitmapFromFile(mFilterDataList.get(position).thumbPath));
        }
        holder.textView.setText(resourceData.ch);
        if (position == mSelectPosition) {
            holder.selectView.setVisibility(View.VISIBLE);
        } else {
            holder.selectView.setVisibility(View.INVISIBLE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectPosition == position) {
                    return;
                }
                int lastSelected = mSelectPosition;
                mSelectPosition = position;
                notifyItemChanged(lastSelected, 0);
                notifyItemChanged(position, 0);
                if (mFilterChangeListener != null) {
                    mFilterChangeListener.onFilterChanged(mFilterDataList.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFilterDataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        View selectView;

        public ViewHolder(View itemView) {
            super(itemView);

        }
    }

    public void clean() {
        mSelectPosition = -1;
        notifyDataSetChanged();
    }

    private OnFilterChangeListener mFilterChangeListener;

    public void setFilterChangeListener(OnFilterChangeListener filterChangeListener) {
        mFilterChangeListener = filterChangeListener;
    }

    public interface OnFilterChangeListener {
        void onFilterChanged(ResourceData resourceData);
    }

}