package es.imposoft.twins.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import es.imposoft.twins.components.Deck;
import es.imposoft.twins.R;
import es.imposoft.twins.builders.ConcreteBuilderLevel;
import es.imposoft.twins.director.Director;
import es.imposoft.twins.gametypes.Game;



public class SelectionGameModeActivity extends AppCompatActivity {
    Context context;
    Deck deck;
    Game game;
    Bundle windowInfo;

    //TODO Crear aqui con el director una partida segun el modo de juego seleccionada y pasarselo a GameActivity
    /*
        Director director = new Director();
        ConcreteBuilderLevel levelBuilder =  new ConcreteBuilderLevel();
        director.constructStandardGame(levelBuilder);
        Game partida = levelBuilder.getResult();
     */
    private Director director;
    private ConcreteBuilderLevel levelBuilder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectgamemode);

        windowInfo = getIntent().getExtras();
        deck = (Deck) windowInfo.get("THEME");
        director = new Director(deck);
        levelBuilder =  new ConcreteBuilderLevel();
<<<<<<< Updated upstream
=======




        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_selectgamemode);
        /* adapt the image to the size of the display */
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Bitmap bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(),R.drawable.background),size.x,size.y,true);
        /* fill the background ImageView with the resized image */
        ImageView iv_background = (ImageView) findViewById(R.id.iv_background);
        iv_background.setImageBitmap(bmp);
>>>>>>> Stashed changes
    }

    public void openLevelsLayout(View view) {
        Intent intent = new Intent(this, SelectLevelActivity.class);
        intent.putExtra("THEME", deck);
        if(getIntent().getIntegerArrayListExtra("LEVEL") != null)
            intent.putIntegerArrayListExtra("SUCCEEDED", getIntent().getIntegerArrayListExtra("LEVEL"));
        startActivity(intent);
        this.finish();
    }

    public void playCasualGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);

        director.constructCasualGame(levelBuilder);
        game = levelBuilder.getResult();

        Gson gson = new Gson();
        String newGame = gson.toJson(game);
        intent.putExtra("GAME",newGame);

        startActivity(intent);
        this.finish();
    }

    public void playStandardGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);

        director.constructStandardGame(levelBuilder);
        game = levelBuilder.getResult();

        Gson gson = new Gson();
        String newGame = gson.toJson(game);
        intent.putExtra("GAME",newGame);

        startActivity(intent);
        this.finish();
    }

    public void goBack(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

}
