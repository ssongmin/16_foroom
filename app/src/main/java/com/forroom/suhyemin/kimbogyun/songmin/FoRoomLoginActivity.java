package com.forroom.suhyemin.kimbogyun.songmin;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.forroom.suhyemin.kimbogyun.songmin.common.ForRoomConstant;
import com.forroom.suhyemin.kimbogyun.songmin.valueobject.LoginValueObject;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class FoRoomLoginActivity extends AppCompatActivity {

    CallbackManager callbackManager;

    ImageButton loginEmailId, joinEmailId, loginFacebookId;
    EditText emailText, passwordText;
    String joinemail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login_page);
        emailText = (EditText) findViewById(R.id.login_id_text);
        passwordText = (EditText) findViewById(R.id.login_pw_text);

//        Log.e("ssong way", PropertyManager.getInstance().getway() + "입니다");
//        Log.e("ssong id", PropertyManager.getInstance().getid()+"    id");
        if(PropertyManager.getInstance().getway().equalsIgnoreCase("e")){
//            if(PropertyManager.getInstance().getid().length()!=0){
                emailText.setText(PropertyManager.getInstance().getmail());
                passwordText.setText(PropertyManager.getInstance().getPassword());
//            }

//            new emailAsync().execute(PropertyManager.getInstance().getid(), PropertyManager.getInstance().getPassword());
            new emailAsync().execute(PropertyManager.getInstance().getmail(), PropertyManager.getInstance().getPassword());
        }else if(PropertyManager.getInstance().getway().equalsIgnoreCase("f")){
            Intent intent = new Intent(FoRoomLoginActivity.this, Login_Auth_Activity.class);
            intent.putExtra("login_way", PropertyManager.getInstance().getway());
            intent.putExtra("login_token", PropertyManager.getInstance().getid());
            intent.putExtra("user_name", PropertyManager.getInstance().getname());
            startActivity(intent);
            finish();
        }
//        if (PropertyManager.getInstance().getid().length() != 0) {
//            ForRoomApplication.USER_ID = PropertyManager.getInstance().getid();
//            Intent intent = new Intent(FoRoomLoginActivity.this, Login_Auth_Activity.class);
//            intent.putExtra("login_way", PropertyManager.getInstance().getway());
//            intent.putExtra("login_token", PropertyManager.getInstance().getid());
//            intent.putExtra("user_name", PropertyManager.getInstance().getname());
//            startActivity(intent);
//            finish();
//        }
        callbackManager = CallbackManager.Factory.create();




        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            public void onSuccess(LoginResult loginResult) {
                String userId = loginResult.getAccessToken().getUserId();
                String accessToken = loginResult.getAccessToken().getToken();
                String user_name;
                String login_tocken = loginResult.getAccessToken().getUserId().toString();
                PropertyManager.getInstance().setID(login_tocken);
                Profile profile = Profile.getCurrentProfile();
                if (profile != null) {
                    user_name = profile.getName();
                    PropertyManager.getInstance().setName(user_name);
                } else {
                    user_name = "닉네임";

                    PropertyManager.getInstance().setName(user_name);
                }
//                Log.e("유저네임", user_name + "");
                Intent intent = new Intent(FoRoomLoginActivity.this, Login_Auth_Activity.class);
                intent.putExtra("login_token", login_tocken);
                intent.putExtra("user_name", user_name);
                intent.putExtra("login_way", "F");
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });
        loginEmailId = (ImageButton) findViewById(R.id.email_login_btn);
        loginEmailId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new emailAsync().execute(emailText.getText().toString(), passwordText.getText().toString());
            }
        });
        loginFacebookId = (ImageButton) findViewById(R.id.facebook_login_btn);
        loginFacebookId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(FoRoomLoginActivity.this,
                        Arrays.asList("public_profile", "email", "user_friends"));
            }
        });
        joinEmailId = (ImageButton) findViewById(R.id.email_join_btn);
        joinEmailId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();

                joinDialog dialog = new joinDialog(FoRoomLoginActivity.this);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        });

    }

    int increment;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("FBLogin", "" + (++increment));
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public class emailAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String id = params[0];
            String pw = params[1];
            String result = "";

            HttpURLConnection conn = null;
            BufferedReader fromServer = null;
            StringBuilder queryBuf = new StringBuilder();
            try {
                queryBuf.append("id=" + id)
                        .append("&way=" + "E")
                        .append("&password=" + pw);

                URL target = new URL(ForRoomConstant.TARGET_URL + ForRoomConstant.USER_AUTH_LOGIN);
                conn = (HttpURLConnection) target.openConnection();
                conn.setConnectTimeout(10000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                OutputStream toServer = conn.getOutputStream();
                toServer.write(new String(queryBuf.toString()).getBytes("UTF-8"));
                toServer.close();
                int resCode = conn.getResponseCode();
                if (resCode == HttpURLConnection.HTTP_OK) {
                    fromServer = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line = "";
                    StringBuilder jsonBuf = new StringBuilder();
                    while ((line = fromServer.readLine()) != null) {
                        jsonBuf.append(line);
                    }
                    JSONObject responseData = new JSONObject(jsonBuf.toString());
                    if (responseData.getString("result").equalsIgnoreCase("LOGIN")) {
                        result = responseData.getJSONObject("userinfo").getString("usr_id");
                        Log.e("DB 버전", "" + responseData.getInt("goods_version"));
                        ForRoomApplication.USER_ID = result;


                        ForoomSQLiteOpenHelper.DB_VERSION = responseData.getInt("goods_version");
                    } else {
                        result = "FAIL";
                    }

                }
            } catch (Exception e) {
                Log.e("erroeroerooeoro", "login", e);
            } finally {
                if (fromServer != null) {
                    try {
                        fromServer.close();
                    } catch (Exception e) {

                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
            return result;


        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equalsIgnoreCase("FAIL")) {
                Toast.makeText(getApplicationContext(), "아이디가 없거나 패스워드 오류입니다.", Toast.LENGTH_SHORT).show();
            } else {
                PropertyManager.getInstance().setID(emailText.getText().toString());
                PropertyManager.getInstance().setPassword(passwordText.getText().toString());
                PropertyManager.getInstance().setWay("E");
                Intent intent = new Intent(FoRoomLoginActivity.this, Login_Auth_Activity.class);
                intent.putExtra("login_way", "E");
                startActivity(intent);
                finish();
            }
        }
    }


    public class AsyncTaskEmailJoinJSONList extends AsyncTask<LoginValueObject, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(LoginValueObject... params) {
            LoginValueObject param = params[0];
            String result = "";
            HttpURLConnection conn = null;
            BufferedReader fromServer = null;
            StringBuilder queryBuf = new StringBuilder();
            try {
                queryBuf.append("id=" + param.userid)
                        .append("&name=" + param.name)
                        .append("&password=" + param.pwd);

                URL target = new URL(ForRoomConstant.TARGET_URL + ForRoomConstant.USER_JOIN);
                conn = (HttpURLConnection) target.openConnection();
                conn.setConnectTimeout(10000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                OutputStream toServer = conn.getOutputStream();
                toServer.write(new String(queryBuf.toString()).getBytes("UTF-8"));
                toServer.close();
                int resCode = conn.getResponseCode();
                if (resCode == HttpURLConnection.HTTP_OK) {
                    fromServer = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line = "";
                    StringBuilder jsonBuf = new StringBuilder();
                    while ((line = fromServer.readLine()) != null) {
                        jsonBuf.append(line);
                    }
                    JSONObject responseData = new JSONObject(jsonBuf.toString());

                    result = responseData.getString("result");
                    joinemail = param.userid;
                    if (result.equalsIgnoreCase("JOIN")) {
                        Log.e("DB 버전", "" + responseData.getInt("goods_version"));
                        ForoomSQLiteOpenHelper.DB_VERSION = responseData.getInt("goods_version");
//                    Log.e("login id",responseData.getJSONObject("userinfo").getString("usr_id") );
                        ForRoomApplication.USER_ID = responseData.getJSONObject("userinfo").getString("usr_id");
                        PropertyManager.getInstance().setID(responseData.getJSONObject("userinfo").getString("usr_id") );
                    }
                }
            } catch (Exception e) {
//                Log.e("erroeroerooeoro", "login", e);
            } finally {
                if (fromServer != null) {
                    try {
                        fromServer.close();
                    } catch (Exception e) {

                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
                return result;
            }

        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equalsIgnoreCase("JOIN")) {

                PropertyManager.getInstance().setEmail(joinemail);

                Intent intent = new Intent(FoRoomLoginActivity.this, Login_Auth_Activity.class);

                intent.putExtra("login_way", "E");
                startActivity(intent);
                finish();
            } else if (s.equalsIgnoreCase("DUPLICATE")) {
                Toast.makeText(getApplicationContext(), "아이디 중복입니다. 다른 아이디를 사용해 주세요", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "서버와 통신이 원활하지 않습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class joinDialog extends Dialog {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
            lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lpWindow.dimAmount = 0.8f;
            getWindow().setAttributes(lpWindow);
            setContentView(R.layout.dialog_email_join);

            ImageView iv2 = (ImageView) findViewById(R.id.login_login_cancle);
            ImageView iv1 = (ImageView) findViewById(R.id.login_login_ok);

            iv1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText emailtext = (EditText) findViewById(R.id.login_login_email_text);
                    EditText passwordtext = (EditText) findViewById(R.id.login_login_password_text);
                    EditText passwordchecktext = (EditText) findViewById(R.id.login_login_password_check_text);
                    EditText nicktext = (EditText) findViewById(R.id.login_login_hhh_text);

                    String email = emailtext.getText().toString();
                    String password = passwordtext.getText().toString();
                    String passwordcheck = passwordchecktext.getText().toString();
                    String nick = nicktext.getText().toString();
                    PropertyManager.getInstance().setName(nick);

//                        Intent intent = new Intent(FoRoomLoginActivity.this, Login_Auth_Activity.class);
//                        intent.putExtra("login_token", email);
//                        intent.putExtra("user_name", nick);
//                        intent.putExtra("login_way", "E");

//                        startActivity(intent);
//                        finish();
                    if (!password.equalsIgnoreCase(passwordcheck)) {
                        Toast.makeText(getApplicationContext(), "비밀번호를 확인해 주세요", Toast.LENGTH_SHORT).show();
                    } else if (4 > password.length() || password.length() > 13) {
                        Toast.makeText(getApplicationContext(), "비밀번호는 5글자에서 12글자 사이로 정해주세요", Toast.LENGTH_SHORT).show();
                    } else if (email.split("@").length == 0) {
                        Toast.makeText(getApplicationContext(), "올바른 이메일 형식이 아닙니다", Toast.LENGTH_SHORT).show();
                    } else if (nick.length() == 0) {

                    } else {
                        PropertyManager.getInstance().setName(nick);

                        PropertyManager.getInstance().setWay("E");
                        PropertyManager.getInstance().setPassword(password);

                        LoginValueObject LVO = new LoginValueObject();
                        LVO.name = nick;
                        LVO.pwd = password;
                        LVO.userid = email;
                        new AsyncTaskEmailJoinJSONList().execute(LVO);
                    }


                }
            });
            iv2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                        Toast.makeText(ForRoomApplication.getFRContext(), "취소눌름", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            });

        }

        public joinDialog(Context context) {
            super(context);


        }
    }


}
