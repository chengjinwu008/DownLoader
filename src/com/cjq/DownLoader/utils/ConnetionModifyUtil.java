package com.cjq.DownLoader.utils;

import java.net.HttpURLConnection;
import java.net.ProtocolException;

/**
 * Created by android on 2015/5/13.
 */
public class ConnetionModifyUtil {

    public static void setupConnection(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        //��������
        connection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
        //��������
        connection.setRequestProperty("Accept-Language", "zh-CN");
        //��ת�����ӣ�ʲôҳ���������ģ���ֹ�������Ĳ������أ�
        connection.setRequestProperty("Referer", connection.getURL().toString());
        //�����ʽ
        connection.setRequestProperty("Charset", "UTF-8");
        //ģ�������
//                    connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
        //���ֳ�����
        connection.setRequestProperty("Connection", "Keep-Alive");
    }
}
