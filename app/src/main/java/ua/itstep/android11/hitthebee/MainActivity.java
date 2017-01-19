package ua.itstep.android11.hitthebee;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnStart;
    Button btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnExit = (Button) findViewById(R.id.btnExit);

        btnStart.setOnClickListener(this);
        btnExit.setOnClickListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnStart:
                Intent intent = new Intent(getApplicationContext(), GameBoardActivity.class);
                startActivity(intent);
                break;

            case R.id.btnExit:
                finish();
        }
    }


} //MainActivity
