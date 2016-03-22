package com.brioal.lzulogin.receiver;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import com.brioal.lzulogin.MainActivity;
import com.brioal.lzulogin.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Brioal on 2016/3/22.
 */
public class LoginReceiver extends BroadcastReceiver {
    private static final String TAG = "LoginReceiver";
    public static int RESULT_SUCCESS = 100;
    public static int RESULT_FAILD = 99;
    public static int RESULT_NAME_ERROT = 98;
    public static int RESULT_PASSWORD_ERROT = 97;
    private NotificationManager manager;
    private static final int NOTICATION_CANCLE_DELAY = 100;


    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RESULT_SUCCESS) {
                showSuccess();
            } else if (msg.what == RESULT_NAME_ERROT) {
                showErron("用户名错误");
            } else if (msg.what == RESULT_PASSWORD_ERROT) {
                showErron("密码错误");
            }
        }
    };
    ;
    private Context context;


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context = context;

        Login(context, intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showErron(String msg) {
        manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent3 = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);
        Notification notify3 = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("登录失败")
                .setContentTitle("登陆失败")
                .setContentText(msg)
                .setContentIntent(pendingIntent3).build();
        notify3.flags |= Notification.FLAG_AUTO_CANCEL;
        manager.notify(NOTICATION_CANCLE_DELAY, notify3);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showSuccess() {
        //获取NoticationManager对象
        manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        final Notification notify3 = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher) // 设置图标
                .setTicker("登录成功") // 设置在通知栏上滚动的信息
                .setContentTitle("登陆成功") // 设置主标题
                .build();
        notify3.flags |= Notification.FLAG_AUTO_CANCEL; // 点击自动消失
        notify3.defaults |= Notification.DEFAULT_VIBRATE; //使用自动的振动模式
        manager.notify(NOTICATION_CANCLE_DELAY, notify3); // 显示通知
    }

    public static Runnable initRunnable(final Handler handler, final Context context, final String userName, final String passWord) {
        Runnable runnable = new Runnable() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                String click_url = "http://10.10.1.254/cgi-bin/srun_portal";
                String parma = "action=login&username=" + userName + "@lzu.edu.cn&ac_id=12&type=1&wbaredirect=http://www.nuomi.com/?cid&mac\n" +
                        "=&nas_ip=&password=" + passWord + "&is_ldap=1";
                HttpURLConnection connection = null;
                URL url;
                try {
                    url = new URL(click_url);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.getOutputStream().write(parma.getBytes());
                    connection.connect(); //????
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        String s = getHtml(connection.getInputStream(), "GBK");
                        String errorMsg = new String(s.getBytes("GBK"), "UTF-8");
                        System.out.println("Html" + s);
                        if (s.contains("连接成功")) {
                            handler.sendEmptyMessage(MainActivity.RESULT_SUCCESS);
                        } else if (errorMsg.contains("密码错误")) {
                            //密码错误
                            handler.sendEmptyMessage(MainActivity.RESULT_PASSWORD_ERROT);
                        } else if (errorMsg.contains("用户名错误")) {
                            handler.sendEmptyMessage(MainActivity.RESULT_NAME_ERROT);

                        }


                        connection.disconnect();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        return runnable;
    }


    public void Login(final Context context, Intent intent) {
        SharedPreferences preferences;
        String mUsername = null;
        String mPassword = null;

        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            final Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                boolean isConnected = state == NetworkInfo.State.CONNECTED;
                if (isConnected) {
                    Log.i(TAG, "onReceive: 连接上可用的WIFi"); // 开始登录
                    preferences = context.getSharedPreferences("LoginInfo", Context.MODE_PRIVATE);
                    mUsername = preferences.getString("Username", "");
                    mPassword = preferences.getString("Password", "");
                    new Thread(initRunnable(handler, context, mUsername, mPassword)).start();
                } else {
                    Log.i(TAG, "onReceive: 未连接可用wifi");
                }
            }
        }
    }

    public static String getHtml(InputStream inputStream, String encode) {
        InputStream is = inputStream;
        String code = encode;
        BufferedReader reader = null;
        StringBuffer sb = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, encode));
            sb = new StringBuffer();
            String str = null;
            while ((str = reader.readLine()) != null) {
                if (str.isEmpty()) {

                } else {
                    sb.append(str);
                    sb.append("\n");
                }
            }
            reader.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
