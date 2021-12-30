package com.example.testdemo.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testdemo.MainActivity;
import com.example.testdemo.R;


public class LoginActivity extends AppCompatActivity {

    private EditText et_Username, et_PassWord;
    private Button btn_Login;
    private TextView tv_Forget,tv_Register;
    boolean isFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_Username = this.findViewById(R.id.et_account);
        et_PassWord = this.findViewById(R.id.et_pwd);
        btn_Login = this.findViewById(R.id.btn_login);
        tv_Forget = this.findViewById(R.id.tv_forgetPwd);
        tv_Register = this.findViewById(R.id.tv_register);
        //用户名
        et_Username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //当点击用户名输入框时，清空密码框
                    et_PassWord.setText("");
                } else {
                    String username = et_Username.getText().toString();
                    if (username.length() < 4) {
                        Toast.makeText(LoginActivity.this, "用户名长度必须大于4,请重新输入", Toast.LENGTH_SHORT).show();
                        et_Username.setText("");

                    }
                }
            }
        });
        //密码框
        et_PassWord.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                } else {
                    String password = et_PassWord.getText().toString();
                    if (password.length() < 6 || password.length() > 12) {
                        Toast.makeText(LoginActivity.this, "密码长度必须为6-12位,请重新输入", Toast.LENGTH_LONG).show();
                        et_PassWord.setText("");

                    }
                }
            }
        });
        //打开数据库
        SQLiteDatabase database = openOrCreateDatabase("user.db", MODE_PRIVATE, null);
        //若不存在user表，则创建user表
        String createSQL = "create table IF NOT EXISTS user(username text,password text,password2 text,phone text)";
        database.execSQL(createSQL);//执行创表语句
        //登录事件监听
        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFlag = false;
                String username = et_Username.getText().toString();
                String password = et_PassWord.getText().toString();
                //判断输入是否为空，若为空，给出提示
                if (username.equals("") || password.equals("")) {
                    Toast.makeText(LoginActivity.this, "请输入账号或密码！", Toast.LENGTH_SHORT).show();
                    isFlag = true;
                }
                //查询账号密码，若数据库表为空，提示用户注册

                Cursor cursor = database.query("user", new String[]{"username,password"}, null, null, null, null, null);
                if (cursor.getCount() == 0) {
                    Toast.makeText(LoginActivity.this, "请先注册账号！", Toast.LENGTH_SHORT).show();
                    isFlag=true;
                }
                //若数据库表不为空，查看用户输入的账号密码是否与数据库表中的相匹配，若匹配，登录成功，跳转到主界面
                // 否则无法登录，给出账号密码错误提示
                else {
                    while (cursor.moveToNext()) {

                        if (username.equals(cursor.getString(cursor.getColumnIndex("username"))) && password.equals(cursor.getString(cursor.getColumnIndex("password")))) {
                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            isFlag = true;
                            break;

                        }
                    }
                    if (isFlag == false) {
                        Toast.makeText(LoginActivity.this, "账号或密码错误，请重新输入", Toast.LENGTH_SHORT).show();
                    }
                }


            }


        });
        //修改密码监听事件，点击忘记密码，跳转到修改密码界面
        tv_Forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgetPwdActivity.class);
                startActivity(intent);

            }
        });
        //注册监听事件，点击新用户，跳转到注册界面
        tv_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }


}