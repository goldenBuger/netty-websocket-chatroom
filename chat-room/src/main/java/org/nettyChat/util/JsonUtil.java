package org.nettyChat.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class JsonUtil {

    static GsonBuilder gb = new GsonBuilder();

    private static final Gson gson;
    static {
         gb.disableHtmlEscaping();//这句话什么意思
         gson = gb.create();
    }
    public static byte[] object2JsonBytes(Object obj){
        String json = pojoToJson(obj);
        try{
            return json.getBytes("UTF-8");
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return null;
    }

    //使用谷歌 Gson 将 POJO 转成字符串
    public static String pojoToJson(Object obj) {
        //String json = new Gson().toJson(obj);
//        String json = JSON.toJSONString(obj);
        String json = gson.toJson(obj);
        return json;
    }

    /**
     * 字节数组转回json，json转回对象
     * @param bytes
     * @param tClass
     * @return
     * @param <T>
     */
    public static <T> T jsonBytes2Object(byte[] bytes, Class<T> tClass) {

        //尽量把对象转换成JSON保存更稳妥
        try {
            String json = new String(bytes, "UTF-8");
            T t = jsonToPojo(json, tClass);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static <T> T jsonToPojo(String json, Class<T> tClass) {
//        T t = JSON.parseObject(json, tClass);
        T t = gson.fromJson(json, tClass);
        return t;
    }

    //使用 谷歌 json 将字符串转成 POJO对象
    public static <T> T jsonToPojo(String json, Type type) {
        T t = gson.fromJson(json, type);
        return t;
    }

    public static <T> T jsonToPojo(String json, TypeToken typeToken)
    {
        T t = gson.fromJson(json, typeToken.getType());
        return t;
    }
}
