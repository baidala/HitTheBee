package ua.itstep.android11.hitthebee;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class GameBoardActivity extends AppCompatActivity {

    private BeeSurfaceView beeSurfaceView;
    private DrawThread drawThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView( R.layout.activity_game_board );

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        beeSurfaceView = (BeeSurfaceView)findViewById(R.id.vBeeSurface);
        //drawThread = beeSurfaceView.getThread();

        beeSurfaceView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);


        if (savedInstanceState == null) {
            // we were just launched: set up a new game
            //if (drawThread != null) drawThread.setState(DrawThread.STATE_READY);
            Log.d(Prefs.LOG_TAG, this.getClass().getSimpleName()+ " SIS is null");
        } else {
            // we are being restored: resume a previous game
            if (drawThread != null) drawThread.restoreState(savedInstanceState);
            Log.d(Prefs.LOG_TAG, this.getClass().getSimpleName()+ " SIS is nonnull");
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        //mBeeSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mBeeSurfaceView.onResume();
    }

}
