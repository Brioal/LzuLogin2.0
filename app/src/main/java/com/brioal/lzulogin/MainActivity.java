package com.brioal.lzulogin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.brioal.lzulogin.receiver.LoginReceiver;

public class MainActivity extends BaseActivity {
    public static int RESULT_SUCCESS = 100;
    public static int RESULT_NAME_ERROT = 98;
    public static int RESULT_PASSWORD_ERROT = 97;
    private EditText et_name;
    private EditText et_pass;
    private static Button btn_save;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RESULT_SUCCESS) {
                btn_save.setText("登录成功,请关闭此页面");
            } else if (msg.what == RESULT_NAME_ERROT) {
                btn_save.setText("用户名错误");
                et_name.setError("用户名错误");
            } else if (msg.what == RESULT_PASSWORD_ERROT) {

                btn_save.setText("登录失败,密码错误");
                et_pass.setError("密码错误");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_name = (EditText) findViewById(R.id.main_et_name);
        et_pass = (EditText) findViewById(R.id.main_et_password);
        btn_save = (Button) findViewById(R.id.main_btn_save);
        preferences = MainActivity.this.getSharedPreferences("LoginInfo", this.MODE_PRIVATE);
        editor = preferences.edit();
        et_name.setText(preferences.getString("Username", ""));
        et_pass.setText(preferences.getString("Password", ""));
        et_pass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                showLogining();
                return true;
            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogining();

            }
        });

    }

    public void showLogining() {
        editor.putString("Username", et_name.getText().toString());
        editor.putString("Password", et_pass.getText().toString());
        editor.apply();
        btn_save.setText("正在登录");
        new Thread(LoginReceiver.initRunnable(handler, MainActivity.this, et_name.getText().toString(), et_pass.getText().toString())).start();

    }


}
