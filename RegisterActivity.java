package com.example.testdemo.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.testdemo.R;


public class RegisterActivity extends AppCompatActivity {

    private Button btn_Register;
    private EditText et_UserName, et_Password, et_Password2,et_Phone;
    private boolean isFlag = true;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //用户名及密码
        et_UserName = this.findViewById(R.id.zc_account);
        et_Password = this.findViewById(R.id.zc_pwd);
        et_Password2 = this.findViewById(R.id.zc_pwdCopy);
        //手机号
        et_Phone = this.findViewById(R.id.zc_phone);

        //确认注册按钮
        btn_Register = this.findViewById(R.id.btn_register);

        //用户名输入框失焦处理
        et_UserName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    isFlag = true;
                } else {
                    isFlag = true;
                    String username = et_UserName.getText().toString();
                    if (username.length() < 4) {
                        Toast.makeText(RegisterActivity.this, "用户名长度必须大于4,请重新输入", Toast.LENGTH_SHORT).show();
                        et_UserName.setText("");
                    }
                }
            }
        });
        //密码输入框失焦处理
        et_Password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    isFlag=true;
                } else {
                    isFlag = true;
                    String password = et_Password.getText().toString();
                    if (password.length() < 6 || password.length() > 12) {
                        Toast.makeText(RegisterActivity.this, "密码长度必须为6-12位,请重新输入", Toast.LENGTH_LONG).show();
                        et_Password.setText("");
                    }
                }
            }
        });
        //确认密码输入框失焦处理
        et_Password2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    isFlag=true;
                } else {
                    isFlag = true;
                    String password = et_Password.getText().toString();
                    String password2 = et_Password2.getText().toString();

                    if (!(password.equals(password2))) {
                        Toast.makeText(RegisterActivity.this, "两次密码不一致,请重新输入", Toast.LENGTH_SHORT).show();
                        et_Password2.setText("");

                    } else if (password2.length() < 6 || password2.length() > 12) {
                        Toast.makeText(RegisterActivity.this, "密码长度必须为6-12位", Toast.LENGTH_LONG).show();
                        et_Password2.setText("");
                    }

                }
            }

        });
        //手机号
        et_Phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    isFlag=true;
                } else {
                    isFlag = true;
                    String phone = et_Phone.getText().toString();
                    if (!(phone.length() == 11)) {
                        Toast.makeText(RegisterActivity.this, "请输入11位手机号", Toast.LENGTH_SHORT).show();
                        et_Phone.setText("");
                    }
                }
            }
        });

        //打开数据库或创建数据库
        SQLiteDatabase database = openOrCreateDatabase("user.db", MODE_PRIVATE, null);
        String createSQL = "create table IF NOT EXISTS user(username text,password text,password2 text,phone text)";
        database.execSQL(createSQL);
        //确认注册处理
        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取用户输入信息
                String username = et_UserName.getText().toString();
                String password = et_Password.getText().toString();
                String password2 = et_Password2.getText().toString();
                String phone = et_Phone.getText().toString();
                //判断用户输入是否为空，若有一个输入框为空，则给出提示信息

                if (username.equals("") || password.equals("") || password2.equals("") || phone.equals("")) {
                    Toast.makeText(RegisterActivity.this, "请输入完整信息", Toast.LENGTH_SHORT).show();
                    isFlag = false;


                }
                //判断用户是否存在，若已有此账号，则不允许重复注册，否则允许注册
                Cursor cursor = database.query("user", new String[]{"username"}, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    if (username.equals(cursor.getString(cursor.getColumnIndex("username")))) {
                        Toast.makeText(RegisterActivity.this, "该账户已存在", Toast.LENGTH_SHORT).show();
                        isFlag = false;//
                    }
                }

                if(!password.equals(password2)) {

                    Toast.makeText(RegisterActivity.this, "两次密码不一致", Toast.LENGTH_SHORT).show();

                    //若允许注册，则将用户输入的信息，插入到数据库表（user）中,插入成功跳转到登录界面
                }else if (!(phone.length() == 11)){
                    Toast.makeText(RegisterActivity.this, "请输入11位手机号！", Toast.LENGTH_SHORT).show();
                }
                else if (isFlag ) {
                    ContentValues values = new ContentValues();
                    //将数据放入values中
                    values.put("username", username);
                    values.put("password", password);
                    values.put("password2", password2);
                    values.put("phone", phone);
                    //用insert()方法将values中的数据插入到user表中
                    database.insert("user", null, values);
                    Toast.makeText(RegisterActivity.this, "注册成功,请登录！", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    database.close();
                }


            }
        });
    }

}