package com.cloudspace.jindun.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudspace.jindun.R;
import com.cloudspace.jindun.view.CircleImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity {

    @Bind(R.id.register_loge)
    CircleImageView mRegisterLoge;
    @Bind(R.id.register_et_id)
    EditText mRegisterEtId;
    @Bind(R.id.register_et_prass)
    EditText mRegisterEtPrass;
    @Bind(R.id.register_bt_showpass)
    Button mRegisterBtShowpass;
    @Bind(R.id.register_bt_showpass_up)
    Button mRegisterBtShowpassUp;
    @Bind(R.id.register_tv_forgetpass)
    TextView mRegisterTvForgetpass;
    private boolean isshow=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.register_loge, R.id.register_bt_showpass, R.id.register_bt_showpass_up, R.id.register_tv_forgetpass})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_loge:
                Toast.makeText(RegisterActivity.this, "1111", Toast.LENGTH_SHORT).show();
                break;
            case R.id.register_bt_showpass:
                Toast.makeText(RegisterActivity.this, "222", Toast.LENGTH_SHORT).show();
                //inputType
                if (isshow){
                    mRegisterEtPrass.setInputType(InputType.TYPE_CLASS_TEXT);
                    mRegisterBtShowpass.setText("隐藏密码");
                    isshow=false;
                }else {
                    mRegisterEtPrass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    mRegisterBtShowpass.setText("显示密码");
                    isshow=true;
                }

                break;
            case R.id.register_bt_showpass_up:
                Toast.makeText(RegisterActivity.this, "333", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClass(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.register_tv_forgetpass:
                Toast.makeText(RegisterActivity.this, "444", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent();
                intent2.setClass(RegisterActivity.this, ForgetpassActivity.class);
                startActivity(intent2);
                break;
        }
    }
}
