package com.cgfay.filter.glfilter.resource;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.cgfay.filter.glfilter.resource.bean.ResourceData;
import com.cgfay.filter.glfilter.resource.bean.ResourceType;
import com.cgfay.uitls.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜数据助手
 */
public final class FilterHelper extends ResourceBaseHelper {
    // 滤镜存储路径
    private static final String FilterDirectory = "Filter";
    // 滤镜列表
    private static final List<ResourceData> mFilterList = new ArrayList<>();

    /**
     * 获取资源列表
     * @return
     */
    public static List<ResourceData> getFilterList() {
        return mFilterList;
    }

    /**
     * 初始化Asset目录下的资源
     * @param context
     */
    public static void initAssetsFilterOld(Context context) {
        FileUtils.createNoMediaFile(getFilterDirectory(context));
        // 清空旧数据
        mFilterList.clear();
        // 添加滤镜数据
        mFilterList.add(new ResourceData("none", "自然", "assets://filter/none.zip", ResourceType.NONE, "none", "assets://thumbs/filter/source.png"));
        mFilterList.add(new ResourceData("whitecat", "白皙","assets://filter/whitecat.zip", ResourceType.FILTER, "whitecat", "assets://thumbs/filter/whitecat.png"));
        mFilterList.add(new ResourceData("sunset", "仲夏","assets://filter/sunset.zip", ResourceType.FILTER, "sunset", "assets://thumbs/filter/sunset.png"));
        mFilterList.add(new ResourceData("sakura", "樱花","assets://filter/sakura.zip", ResourceType.FILTER, "sakura", "assets://thumbs/filter/sakura.png"));
        mFilterList.add(new ResourceData("romance", "浪漫","assets://filter/romance.zip", ResourceType.FILTER, "romance", "assets://thumbs/filter/romance.png"));
        mFilterList.add(new ResourceData("calm", "平静","assets://filter/calm.zip", ResourceType.FILTER, "calm", "assets://thumbs/filter/calm.png"));
        mFilterList.add(new ResourceData("cool", "凉爽","assets://filter/cool.zip", ResourceType.FILTER, "cool", "assets://thumbs/filter/cool.png"));
        mFilterList.add(new ResourceData("earlybird", "纯净","assets://filter/earlybird.zip", ResourceType.FILTER, "earlybird", "assets://thumbs/filter/earlybird.png"));
        mFilterList.add(new ResourceData("emerald", "翡翠","assets://filter/emerald.zip", ResourceType.FILTER, "emerald", "assets://thumbs/filter/emerald.png"));
        mFilterList.add(new ResourceData("fairytale", "童话","assets://filter/fairytale.zip", ResourceType.FILTER, "fairytale", "assets://thumbs/filter/fairytale.png"));
        mFilterList.add(new ResourceData("healthy", "魅力","assets://filter/healthy.zip", ResourceType.FILTER, "healthy", "assets://thumbs/filter/healthy.png"));
        mFilterList.add(new ResourceData("hefe", "动人","assets://filter/hefe.zip", ResourceType.FILTER, "hefe", "assets://thumbs/filter/hefe.png"));
        mFilterList.add(new ResourceData("amaro", "阿马罗","assets://filter/amaro.zip", ResourceType.FILTER, "amaro", "assets://thumbs/filter/amaro.png"));
        mFilterList.add(new ResourceData("anitque", "阿尼奇","assets://filter/anitque.zip", ResourceType.FILTER, "anitque", "assets://thumbs/filter/anitque.png"));
        mFilterList.add(new ResourceData("brooklyn", "布鲁克林","assets://filter/brooklyn.zip", ResourceType.FILTER, "brooklyn", "assets://thumbs/filter/brooklyn.png"));
        mFilterList.add(new ResourceData("freud", "弗洛伊德","assets://filter/freud.zip", ResourceType.FILTER, "freud", "assets://thumbs/filter/freud.png"));
        mFilterList.add(new ResourceData("hudson", "哈德逊","assets://filter/hudson.zip", ResourceType.FILTER, "hudson", "assets://thumbs/filter/hudson.png"));
        mFilterList.add(new ResourceData("kevin", "闪酷橘","assets://filter/kevin.zip", ResourceType.FILTER, "kevin", "assets://thumbs/filter/kevin.png"));
        mFilterList.add(new ResourceData("latte", "拿铁","assets://filter/latte.zip", ResourceType.FILTER, "latte", "assets://thumbs/filter/latte.png"));
        mFilterList.add(new ResourceData("lomo", "琥珀","assets://filter/lomo.zip", ResourceType.FILTER, "lomo", "assets://thumbs/filter/lomo.png"));
        mFilterList.add(new ResourceData("sketch", "素描","assets://filter/sketch.zip", ResourceType.FILTER, "sketch", "assets://thumbs/filter/sketch.png"));
        mFilterList.add(new ResourceData("blackcat", "深黑","assets://filter/blackcat.zip", ResourceType.FILTER, "blackcat", "assets://thumbs/filter/blackcat.png"));
        mFilterList.add(new ResourceData("blackwhite", "黑白","assets://filter/blackwhite.zip", ResourceType.FILTER, "blackwhite", "assets://thumbs/filter/blackwhite.png"));

        decompressResource(context, mFilterList);
    }

    public static void initAssetsFilter(Context context) {
        if (null != mFilterList && mFilterList.size() > 0) return;

        FileUtils.createNoMediaFile(getFilterDirectory(context));
        // 清空旧数据
        mFilterList.clear();

        mFilterList.add(new ResourceData("ziran", "自然","assets://filter/ziran.zip", ResourceType.FILTER, "ziran", "assets://thumbs/filter/ziran.jpg"));
        mFilterList.add(new ResourceData("baizan", "白皙","assets://filter/ziran.zip", ResourceType.FILTER, "ziran", "assets://thumbs/filter/ziran.jpg"));
        mFilterList.add(new ResourceData("baixue", "白雪","assets://filter/baixue.zip", ResourceType.FILTER, "baixue", "assets://thumbs/filter/baixue.jpg"));
        mFilterList.add(new ResourceData("chulian", "初恋","assets://filter/chulian.zip", ResourceType.FILTER, "chulian", "assets://thumbs/filter/chulian.jpg"));
        mFilterList.add(new ResourceData("chuxin", "初心","assets://filter/chuxin.zip", ResourceType.FILTER, "chuxin", "assets://thumbs/filter/chuxin.jpg"));
        mFilterList.add(new ResourceData("dongren", "动人","assets://filter/dongren.zip", ResourceType.FILTER, "dongren", "assets://thumbs/filter/dongren.jpg"));
        mFilterList.add(new ResourceData("qingchun", "清纯","assets://filter/qingchun.zip", ResourceType.FILTER, "qingchun", "assets://thumbs/filter/qingchun.jpg"));

        mFilterList.add(new ResourceData("chengshi", "城市","assets://filter/chengshi.zip", ResourceType.FILTER, "chengshi", "assets://thumbs/filter/chengshi.jpg"));
        mFilterList.add(new ResourceData("chunjing", "纯净","assets://filter/chunjing.zip", ResourceType.FILTER, "chunjing", "assets://thumbs/filter/chunjing.jpg"));
        mFilterList.add(new ResourceData("chunzhen", "纯真","assets://filter/chunzhen.zip", ResourceType.FILTER, "chunzhen", "assets://thumbs/filter/chunzhen.jpg"));
        mFilterList.add(new ResourceData("hupo", "琥珀","assets://filter/hupo.zip", ResourceType.FILTER, "hupo", "assets://thumbs/filter/hupo.jpg"));
        mFilterList.add(new ResourceData("qingxin", "清新","assets://filter/qingxin.zip", ResourceType.FILTER, "qingxin", "assets://thumbs/filter/qingxin.jpg"));

        mFilterList.add(new ResourceData("jiaotang", "焦糖","assets://filter/jiaotang.zip", ResourceType.FILTER, "jiaotang", "assets://thumbs/filter/jiaotang.jpg"));
        mFilterList.add(new ResourceData("kekou", "可口","assets://filter/kekou.zip", ResourceType.FILTER, "kekou", "assets://thumbs/filter/kekou.jpg"));
        mFilterList.add(new ResourceData("mifentao", "蜜粉桃","assets://filter/mifentao.zip", ResourceType.FILTER, "mifentao", "assets://thumbs/filter/mifentao.jpg"));
        mFilterList.add(new ResourceData("moka", "摩卡","assets://filter/moka.zip", ResourceType.FILTER, "moka", "assets://thumbs/filter/moka.jpg"));
        mFilterList.add(new ResourceData("qingliang", "清凉","assets://filter/qingliang.zip", ResourceType.FILTER, "qingliang", "assets://thumbs/filter/qingliang.jpg"));

        mFilterList.add(new ResourceData("fanchase", "反差色","assets://filter/fanchase.zip", ResourceType.FILTER, "fanchase", "assets://thumbs/filter/fanchase.jpg"));
        mFilterList.add(new ResourceData("riza", "日杂","assets://filter/riza.zip", ResourceType.FILTER, "riza", "assets://thumbs/filter/riza.jpg"));
        mFilterList.add(new ResourceData("pailide", "拍立得","assets://filter/pailide.zip", ResourceType.FILTER, "pailide", "assets://thumbs/filter/pailide.jpg"));
        mFilterList.add(new ResourceData("guowang", "过往","assets://filter/guowang.zip", ResourceType.FILTER, "guowang", "assets://thumbs/filter/guowang.jpg"));
        mFilterList.add(new ResourceData("shenhei", "深黑","assets://filter/shenhei.zip", ResourceType.FILTER, "shenhei", "assets://thumbs/filter/shenhei.jpg"));

        decompressResource(context, mFilterList);
    }

    /**
     * 解压所有资源
     * @param context
     * @param resourceList 资源列表
     */
    public static void decompressResource(Context context, List<ResourceData> resourceList) {
        // 检查路径是否存在
        boolean result = checkFilterDirectory(context);
        // 存放资源路径无法创建，则直接返回
        if (!result) {
            return;
        }

        String filterPath = getFilterDirectory(context);
        // 解码列表中的所有资源
        for (ResourceData item : resourceList) {
            if (item.type.getIndex() >= 0) {
                if (item.zipPath.startsWith("assets://")) {
                    decompressAsset(context, item.zipPath.substring("assets://".length()), item.unzipFolder, filterPath);
                } else if (item.zipPath.startsWith("file://")) {    // 绝对目录中的资源
                    decompressFile(item.zipPath.substring("file://".length()), item.unzipFolder, filterPath);
                }
            }
        }
    }

    /**
     * 检查滤镜路径是否存在
     * @param context
     */
    private static boolean checkFilterDirectory(Context context) {
        String resourcePath = getFilterDirectory(context);
        File file = new File(resourcePath);
        if (file.exists()) {
            return file.isDirectory();
        }
        return file.mkdirs();
    }

    /**
     * 获取滤镜路径
     * @param context
     * @return
     */
    public static String getFilterDirectory(Context context) {
        String resourcePath;
        // 判断外部存储是否可用，如果不可用则使用内部存储路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            resourcePath = context.getExternalFilesDir(FilterDirectory).getAbsolutePath();
        } else { // 使用内部存储
            resourcePath = context.getFilesDir() + File.separator + FilterDirectory;
        }
        return resourcePath;
    }

    /**
     * 删除某个滤镜
     * @param context
     * @param resource  资源对象
     * @return          删除操作结果
     */
    public static boolean deleteFilter(Context context, ResourceData resource) {
        if (resource == null || TextUtils.isEmpty(resource.unzipFolder)) {
            return false;
        }
        boolean result = checkFilterDirectory(context);
        if (!result) {
            return false;
        }
        // 获取资源解压的文件夹路径
        String resourceFolder = getFilterDirectory(context) + File.separator + resource.unzipFolder;
        File file = new File(resourceFolder);
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }
        return FileUtils.deleteDir(file);
    }

}
