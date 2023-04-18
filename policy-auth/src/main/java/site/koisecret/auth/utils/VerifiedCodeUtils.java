package site.koisecret.auth.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * @author by chengsecret
 * @date 2023/3/31.
 */
public class VerifiedCodeUtils {
    //生成验证码
    public static String getCode(){
        Random random=new Random();
        String code="";
        for(int i=0;i<6;i++){
            int rand=random.nextInt(10);
            code+=rand;
        }
        return code;
    }

    /**
     * 随机生成字符串
     * @param length
     * @return
     */
    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 发送验证码
     * @param phone 手机号
     * @param code 验证码
     * @return
     */
    public static String sentCode(String phone, String code) {
        String testUsername = "1180290010"; //在短信宝注册的用户名
        String testPassword = "b89660479dd745ccb5b09fc2e48bcd88"; //在短信宝注册的密码
        String testContent = "【政策检索】您的验证码是："+ code + "，验证码有效期一分钟，如非本人操作，请忽略本短信。";

        String httpUrl = "http://api.smsbao.com/sms";

        StringBuffer httpArg = new StringBuffer();
        httpArg.append("u=").append(testUsername).append("&");
        httpArg.append("p=").append(md5(testPassword)).append("&");
        httpArg.append("m=").append(phone).append("&");
        httpArg.append("c=").append(encodeUrlString(testContent, "UTF-8"));

        String result = request(httpUrl, httpArg.toString());
        return result;
    }

    public static String request(String httpUrl, String httpArg) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();
        httpUrl = httpUrl + "?" + httpArg;

        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = reader.readLine();
            if (strRead != null) {
                sbf.append(strRead);
                while ((strRead = reader.readLine()) != null) {
                    sbf.append("\n");
                    sbf.append(strRead);
                }
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String md5(String plainText) {
        StringBuffer buf = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }

    public static String encodeUrlString(String str, String charset) {
        String strret = null;
        if (str == null)
            return str;
        try {
            strret = java.net.URLEncoder.encode(str, charset);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return strret;
    }
}
