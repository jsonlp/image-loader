package com.lpan.image_loader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.lpan.image.ImageLoader;

public class MainActivity extends AppCompatActivity {
    public static final String URL = "http://ww4.sinaimg.cn/large/610dc034gw1fa0ppsw0a7j20u00u0thp.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = findViewById(R.id.image);

        ImageLoader.getInstance(getApplicationContext()).display(URL, imageView, 2000, 600);
    }
}
