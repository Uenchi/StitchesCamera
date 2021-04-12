package com.uenchi.editlibrary.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.util.Pair;

import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.cgfay.filter.glfilter.color.bean.DynamicColorData;
import com.cgfay.filter.glfilter.resource.ResourceCodec;
import com.cgfay.filter.glfilter.resource.ResourceDataCodec;
import com.cgfay.uitls.utils.BitmapUtils;
import com.uenchi.editlibrary.utils.GlUtil;

import java.io.IOException;

public class GlColorFilter extends GlFilter {
    private static final String FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "uniform sampler2D inputTexture;\n" +
            "uniform sampler2D mipTexture; // lookup texture\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);\n" +
            "    mediump float blueColor = textureColor.b * 63.0;\n" +
            "    mediump vec2 quad1;\n" +
            "    quad1.y = floor(floor(blueColor) / 8.0);\n" +
            "    quad1.x = floor(blueColor) - (quad1.y * 8.0);\n" +
            "    mediump vec2 quad2;\n" +
            "    quad2.y = floor(ceil(blueColor) / 8.0);\n" +
            "    quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n" +
            "    highp vec2 texPos1;\n" +
            "    texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "    texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "    highp vec2 texPos2;\n" +
            "    texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "    texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "    lowp vec4 newColor1 = texture2D(mipTexture, texPos1);\n" +
            "    lowp vec4 newColor2 = texture2D(mipTexture, texPos2);\n" +
            "    lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n" +
            "    gl_FragColor = vec4(newColor.rgb, textureColor.w);\n" +
            "}";

    enum PngFilter {
        WHITE, LANGMAN, QINGXIN, WEIMEI, FENNEN, HUIJIU, LANDIAO,
    }

    private static final String DIR_NAME = "filters/";

    private int mFairyTaleTexture;
    private int mipTextureHandle;
    private DynamicColor dynamicColor;
    // 资源加载器
    private ResourceDataCodec mResourceCodec;
    private String mFolderPath;
    // 动态滤镜数据
    private DynamicColorData mColorData;

    public GlColorFilter( DynamicColor dynamicColor) {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
        this.dynamicColor = dynamicColor;
        mColorData = dynamicColor.filterList.get(0);
        mFolderPath = dynamicColor.unzipPath.startsWith("file://") ? dynamicColor.unzipPath.substring("file://".length()) : dynamicColor.unzipPath;
        Pair pair = ResourceCodec.getResourceFile(mFolderPath);
        if (pair != null) {
            mResourceCodec = new ResourceDataCodec(mFolderPath + "/" + (String) pair.first, mFolderPath + "/" + pair.second);
        }
        if (mResourceCodec != null) {
            try {
                mResourceCodec.init();
            } catch (IOException e) {
                mResourceCodec = null;
            }
        }
    }

    public GlColorFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    @Override
    public void initProgram() {
        super.initProgram();
        mipTextureHandle = GLES30.glGetUniformLocation(program, "mipTexture");
        createTexture();
    }

    /**
     * 创建纹理
     */
    private void createTexture() {
        mFairyTaleTexture = GlUtil.createTextureFromAssets(getBitmap());
    }

    private Bitmap getBitmap(){
        Bitmap bitmap = null;
        if (mResourceCodec != null) {
            bitmap = mResourceCodec.loadBitmap(mColorData.uniformDataList.get(0).value);
        }
        if (bitmap == null) {
            bitmap = BitmapUtils.getBitmapFromFile(mFolderPath + "/" + String.format(mColorData.uniformDataList.get(0).value));
        }
        return bitmap;
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mFairyTaleTexture);
        GLES30.glUniform1i(mipTextureHandle, 1);
    }

    @Override
    public void release() {
        super.release();
        GLES30.glDeleteTextures(1, new int[]{mFairyTaleTexture}, 0);
    }
}
