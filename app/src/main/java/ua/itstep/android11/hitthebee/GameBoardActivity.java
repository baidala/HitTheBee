package ua.itstep.android11.hitthebee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GameBoardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( new BeeSurfaceView(this) );

    }
}
