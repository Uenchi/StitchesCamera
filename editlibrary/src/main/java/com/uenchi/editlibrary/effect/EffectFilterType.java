package com.uenchi.editlibrary.effect;

import android.os.Parcel;
import android.os.Parcelable;

public class EffectFilterType implements Parcelable {
    public static final int FILTER = 0;       // 滤镜特效
    public static final int TRANSITION = 1;   // 转场特效
    public static final int MULTIFRAME = 2;   // 分屏特效
    public static final int TIME = 3;         // 时间特效

    private int mimeType;    // 特效类型，滤镜特效、转场特效以及时间特效

    private String mThumb;  // 缩略图路径
    private String mName;   // 特效名
    private int id;         // 特效id，暂时没啥用。本来是预留做动态处理的id。

    public EffectFilterType(int mimeType, String name, int id, String thumbPath) {
        this.mimeType = mimeType;
        this.mName = name;
        this.id = id;
        this.mThumb = thumbPath;
    }

    private EffectFilterType(Parcel in) {
        id = in.readInt();
        mName = in.readString();
        mThumb = in.readString();
        mimeType = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(mName);
        dest.writeString(mThumb);
        dest.writeInt(mimeType);
    }

    public int getMimeType() {
        return mimeType;
    }

    public String getThumb() {
        return mThumb;
    }

    public String getName() {
        return mName;
    }

    public int getId() {
        return id;
    }

    public static final Creator<EffectFilterType> CREATOR = new Creator<EffectFilterType>() {
        @Override
        public EffectFilterType createFromParcel(Parcel source) {
            return new EffectFilterType(source);
        }

        @Override
        public EffectFilterType[] newArray(int size) {
            return new EffectFilterType[size];
        }
    };
}
