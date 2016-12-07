package com.yxh.init_tool;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
private Button but_phone;
    private TextView textView;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    textView.setText("导入失败");
                    break;
                case 1:
                    textView.setText("导入成功");
                    break;
            }
        };
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        but_phone = (Button) findViewById(R.id.but_phone);
        textView = (TextView) findViewById(R.id.textView);
        but_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DownUtil.DownloadThread(MainActivity.this,handler, DownUtil.TYPE_CONTACT,
                        "http://proxy.zed1.cn:88/data/cgi/user!list.action?size=50").start();
            }
        });
    }
}
