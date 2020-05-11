package es.imposoft.twins.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import es.imposoft.twins.Card;
import es.imposoft.twins.components.Deck;
import es.imposoft.twins.R;
import es.imposoft.twins.Scoreboard;
import es.imposoft.twins.gametypes.Game;
import es.imposoft.twins.plantilla.*;

public class GameActivity extends AppCompatActivity {
    Deck themeCard;
    Chronometer chronoTimer;
    Button pauseButton;
    Button restartButton;

    private int maxCards;
    Button[] buttons;
    Card[] cards;
    Context context;
    int tapCounter, pauseTapCounter, visibleCards;
    Scoreboard scoreboard;

    int acertadosSeguidos;
    boolean anteriorAcertada, pausedGame;

    int score;
    private int restantMatches;
    private boolean isClickable;
    List<Card> pairs = new ArrayList<>();

    long timeWhenStarted, timeWhenStopped;

    Bundle windowInfo;
    Game game;
    Gson gson;
    AbstractScore scoreManager;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        windowInfo = getIntent().getExtras();

        gson = new Gson();
        game = gson.fromJson((String) windowInfo.get("GAME"),Game.class);
        System.out.println(game.printGame());
        context = getApplicationContext();
        super.onCreate(savedInstanceState);

        selectLayout();

        pauseButton = findViewById(R.id.button_pause);
        pauseButton.setVisibility(View.INVISIBLE);

        restartButton = findViewById(R.id.button_restart);
        restartButton.setVisibility(View.INVISIBLE);

        chronoTimer = findViewById(R.id.text_timer);
        timeWhenStopped = 0;
        setChronometerType();

        pauseTapCounter = 0;
        tapCounter = 0;
        maxCards = game.getCardAmount();
        restantMatches = maxCards / 2;
        visibleCards = 0;

        buttons = new Button[maxCards];
        cards = new Card[maxCards];
        isClickable = false;
        pausedGame = false;

        score = 0;
        scoreboard = new Scoreboard();
        getScoreManager();

        acertadosSeguidos = 0;
        anteriorAcertada = false;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        scoreboard.loadHighscores(sp);

        fillArray();
        createCards();
        themeCard = game.getDeck();
        assignCardTheme(themeCard);

    }

    private void getScoreManager() {
        switch(game.getGameMode()) {
            case CASUAL:
                scoreManager = new ScoreFree();
                break;
            case LEVELS:
                scoreManager = new ScoreLevels();
                break;
            case STANDARD:
                scoreManager = new ScoreStandard();
                break;
        }
    }

    private void selectLayout() {
        switch (game.getCardAmount()) {
            case 16:
                setContentView(R.layout.activity_gamescene4x4);
                break;
            case 20:
                setContentView(R.layout.activity_gamescene4x5);
                break;
            case 24:
                setContentView(R.layout.activity_gamescene4x6);
                break;
        }
    }

    public void showScoreboard(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        scoreboard.addScore(score);
        scoreboard.saveHighscores(sp);

        Intent intent = new Intent(GameActivity.this, PopupActivity.class);
        Gson gson = new Gson();
        String gscoreboard = gson.toJson(scoreboard);
        intent.putExtra("SCORE",gscoreboard);
        intent.putExtra("TYPE", PopupActivity.WindowType.SCOREBOARD);

        startActivityForResult(intent,1);
    }

    public void showGameOver(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //scoreboard.addScore(score);
        scoreboard.saveHighscores(sp);

        Intent intent = new Intent(GameActivity.this, PopupActivity.class);
        Gson gson = new Gson();
        String gscoreboard = gson.toJson(scoreboard);
        intent.putExtra("SCORE",gscoreboard);
        intent.putExtra("TYPE", PopupActivity.WindowType.GAMEOVER);
        startActivityForResult(intent,1);
    }

    @SuppressLint("WrongViewCast")
    public void changeSprite(View view) {
        if(tapCounter == 0) {
            turnAllCards();
            Handler revealTime = new Handler();
            revealTime.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                public void run() {
                    turnAllCards();
                    startChronometer();
                    isTimeOver();
                    pauseButton.setVisibility(View.VISIBLE);
                    restartButton.setVisibility(View.VISIBLE);
                }
            }, game.getRevealSeconds()*1000);

        } else {
            for (Card card : cards) {
                if(visibleCards < 2 && !pausedGame)
                    if (card.getCardButton().getId() == view.getId()) {
                        card.turnCard();
                        visibleCards++;
                        card.getCardButton().setClickable(false);
                        if(pairs.size()<2)  {
                            pairs.add(card);
                        }
                        if (pairs.size() == 2) {
                            if (pairs.get(0).equals(pairs.get(1))) {
                                restantMatches--;
                                pairs.get(0).setPaired();
                                pairs.get(1).setPaired();
                                anteriorAcertada = true;
                                updateScore();//si acierta
                                pairs.clear();
                                visibleCards = 0;
                                stopChronometer();
                            } else {
                                anteriorAcertada = false;
                                updateScore();//si falla
                                Handler secs1 = new Handler();
                                secs1.postDelayed(new Runnable() {
                                    @RequiresApi(api = Build.VERSION_CODES.M)
                                    public void run() {
                                        turnVisibleCards();
                                        visibleCards = 0;
                                    }
                                }, 1000);
                                pairs.get(0).getCardButton().setClickable(true);
                                pairs.get(1).getCardButton().setClickable(true);

                                pairs.clear();
                            }
                        }
                    }
            }
        }
        tapCounter++;
    }

    @SuppressLint("SetTextI18n")
    private void updateScore() {
    /**
     * Si crono = DESCENDENTE / NONE -> */
        score = scoreManager.updateScore(anteriorAcertada);
        ((TextView) findViewById(R.id.text_score)).setText("Puntos: " + score);
    }

    private void turnAllCards() {
        for (Card card : cards) {
            card.turnCard();
        }
        setClickable(buttons);
    }

    private void turnVisibleCards() {
        for (Card card : cards) {
            card.turnVisibleCards();
        }
    }

    private void fillArray(){
        for(int i = 0; i < maxCards; i++) {
            int imageID = getResources().getIdentifier("imgPos" + i,"id", getPackageName());
            //int buttonID = getResources().getIdentifier("imgPos" + i,"id", getPackageName());
            buttons[i] = findViewById(imageID);
        }
    }

    private void createCards() {
        for (int i = 0; i < maxCards; i++) {
            cards[i] = new Card(buttons[i], context);
        }
    }

    private void stopChronometer() {
        if(restantMatches == 0) {
            chronoTimer.stop();
            Handler secs1 = new Handler();
            secs1.postDelayed(new Runnable() {
                public void run() {
                    showScoreboard();
                }
            }, 800);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startChronometer() {
        chronoTimer.setBase(SystemClock.elapsedRealtime());
        timeWhenStarted = chronoTimer.getBase();
        if (chronoTimer.isCountDown()) {
            chronoTimer.setBase(timeWhenStarted + (game.getSeconds() * 1000));
        }
        chronoTimer.start();
    }


    private void isTimeOver(){
        chronoTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if (timeWhenStarted + (game.getSeconds() * 1000) <= SystemClock.elapsedRealtime()) {
                    chronoTimer.stop();
                    Handler secs1 = new Handler();
                    secs1.postDelayed(new Runnable() {
                        public void run() {
                            showGameOver();
                        }
                    }, 1000);
                }
            }
        });
    }

    public void pauseGame(View view) {
        if(pauseTapCounter % 2 == 0) {
            timeWhenStopped = (chronoTimer.getBase() - SystemClock.elapsedRealtime());
            chronoTimer.stop();
            pausedGame = !pausedGame;
        }
        else {
            chronoTimer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            chronoTimer.start();
            pausedGame = !pausedGame;
        }
        pauseTapCounter++;
    }

    public void reiniciar(View view) {
        Intent intent = new Intent(this, GameActivity.class);

        Gson gson = new Gson();
        String newGame = gson.toJson(game);
        intent.putExtra("GAME",newGame);

        startActivity(intent);
        this.finish();
    }

    private void setClickable(Button[] buttons) {
        for (Button b: buttons) {
            if(getButton(b).isPaired()) {}
            else b.setClickable(isClickable);
        }
        isClickable = !isClickable;
    }

    public void onFinishPressed(View view){
        Intent intent = new Intent(GameActivity.this, PopupActivity.class);
        intent.putExtra("TYPE", PopupActivity.WindowType.WARNING);
        startActivityForResult(intent,0);
    }

    private Card getButton(Button button) {
        for (Card c : cards) {
            if(c.getCardButton() == button) return c;
        }
        return null;
    }


    /**
     * Formato de nombre de imagen: "tema + numero"
     * los numeros de las barajas seran (de momento) como minimo del 0 - 7 (incluidos)
     * (ya que tenemos 16 cartas)*/
    private void assignCardTheme(Deck theme) {
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < cards.length; i++) numbers.add(i);

        int aleatory;
        int position;
        ArrayList<Card> shuffled = new ArrayList<Card>();

        while (!numbers.isEmpty()) {
            aleatory = (int) (Math.random()*numbers.size());
            position = numbers.get(aleatory);
            numbers.remove(aleatory);
            shuffled.add(cards[position]);
        }

        ArrayList<Integer> images = new ArrayList<Integer>();
        for (int i = 0; i < maxCards/2; i++)
            images.add(getResources().getIdentifier(theme.toString().toLowerCase() + i, "drawable", getPackageName()));

        for (int i = 0; i < shuffled.size(); i++)
            shuffled.get(i).setFrontImage(BitmapFactory.decodeResource(context.getResources(), images.get(i/2)));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Method executed from the popup window
        if (data != null) {
            switch ((Integer) data.getExtras().get("WINDOW")) {
                case 0:
                    //Called from the finish game popup
                    if (resultCode == RESULT_OK) {
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                    }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setChronometerType(){
        switch(game.getChronometerType()) {
            case NONE:
                chronoTimer.setVisibility(View.GONE);
                break;
            case NORMAL:
                chronoTimer.setVisibility(View.VISIBLE);
                break;
            case DESCENDING:
                chronoTimer.setVisibility(View.VISIBLE);
                chronoTimer.setCountDown(true);
                break;
        }
    }
}
