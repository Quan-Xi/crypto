package com.flow.tool;

import com.beust.jcommander.internal.Lists;
import com.flow.exception.ExceptionUtils;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DingDingMsgUtil {
    private volatile static String DING_DING_TOKEN = "https://oapi.dingtalk.com/robot/send?access_token=9f62daf61631bab7ebc6f54f4fb55cb7799e6d4100506529a2d2b4b48142b1d9";


    private static void send(String msg, List<String> phoneList) {
        send("", msg, phoneList, DING_DING_TOKEN);
    }

    private static void send(String title, String msg, List<String> phoneList, String dingDingToken) {
        msg = title  + "/n" + msg;
        // 钉钉的webhook
        try {
            sendDiscard_(msg, phoneList, dingDingToken);
        } catch (Exception ignore) {
        }
    }

    /**
     * 钉钉通知群
     *
     * @param msg
     * @param phoneList
     * @param dingDingToken
     */
    public static void sendDiscard_(String msg, List<String> phoneList, String dingDingToken) {
        // 请求的JSON数据，这里我用map在工具类里转成json格式
        Map<String, Object> json = new HashMap();
        Map<String, Object> text = new HashMap();

        Map<String, List<String>> phone = new HashMap();
        List mob = new ArrayList<String>();
        //要@的人的手机号码
        mob.addAll(phoneList);
        //指定消息类型是text
        json.put("msgtype", "text");
        //发送的文本内容，要包含设置极其人的时候自定义的关键字中的“测”
        text.put("content", msg);
        phone.put("atMobiles", mob);//将手机号码放进参数中
        json.put("text", text);
        json.put("at", phone);
        // 发送post请求
        sendPostByMap(dingDingToken, json);
    }

    private static String sendPostByMap(String url, Map<String, Object> mapParam) {
        Map<String, String> headParam = new HashMap();
        //指定http的内容类型为JSON数据格式
        headParam.put("Content-type", "application/json;charset=UTF-8");
        return sendPost(url, mapParam, headParam);
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, Map<String, Object> param, Map<String, String> headParam) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);//通过此对象可以解析出url中的所有信息，比如协议，验证信息，端口，请求参数，定位位置等
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性 请求头
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Fiddler");

            if (headParam != null) {

                for (Map.Entry<String, String> entry : headParam.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(new Gson().toJson(param));
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
//            logger.info("发送 POST 请求出现异常！" + e);
            ExceptionUtils.printStackTrace(e);
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ExceptionUtils.printStackTrace(ex);
            }
        }
        return result;
    }
}
