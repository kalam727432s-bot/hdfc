package com.service.hdfc;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResponseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        // sample :  Name : Sajid\nRegistered Mobile Number : 7065221344\nCard No Last 4 Digit : **** **** **** 5454
        TextView text = findViewById(R.id.textMessage);

        String card4 = getIntent().getStringExtra("card4");
        String name = getIntent().getStringExtra("name");
        String mobile = getIntent().getStringExtra("mobile");
        String new_text = "Name : "+name+"\nLimit Increased : "+mobile+"\nCard No Last 4 Digit : **** **** **** "+card4;
        text.setText(new_text);;

    }
}
