package es.imposoft.twins.view.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.gson.Gson;

import es.imposoft.twins.persistance.database.SucceededChallenge;
import es.imposoft.twins.logic.card.Card;
import es.imposoft.twins.logic.singleton.MusicService;
import es.imposoft.twins.persistance.database.SucceededLevel;
import es.imposoft.twins.logic.components.Deck;
import es.imposoft.twins.logic.components.DeckTheme;
import es.imposoft.twins.R;
import es.imposoft.twins.persistance.database.Scoreboard;
import es.imposoft.twins.logic.components.GameMode;
import es.imposoft.twins.logic.gametypes.Game;
import es.imposoft.twins.logic.prototype.*;

public class GameActivity extends AppCompatActivity {
    DeckTheme themeCard;
    Chronometer chronoTimer;
    Button pauseButton, restartButton;
    TextView scoreText;

    int maxCards, visibleCards, restantMatches;
    Button[] buttons;
    ArrayList<Card> cards;
    Context context;
    int tapCounter, pauseTapCounter, tapErrors, maxErrors;
    Scoreboard scoreboard;
    int score, levelPlayed, challengePlayed;

    boolean previousCorrect, isGamePaused, isClickable;
    List<Card> pairs = new ArrayList<>();

    long timeWhenStarted, timeWhenStopped;

    final int SOUNDSECONDS = 15000;

    Bundle windowInfo;
    Game game;
    Gson gson;
    AbstractScore scoreManager;
    SucceededLevel succeededLevels;
    private SucceededChallenge succeededChallenge;
    Deck deck;

    SharedPreferences sharedPreferences;
    GameMode gameMode;
    MusicService musicEngine;
    Handler timeHandler;
    Intent intent;
    private String gscoreboard, glevels, gchallenge, email;

    GoogleSignInAccount signedInAccount;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        windowInfo = getIntent().getExtras();
        timeHandler = new Handler();

        gson = new Gson();
        game = gson.fromJson((String) windowInfo.get("GAME"),Game.class);

        if(windowInfo.get("LEVEL") != null) {
            levelPlayed = (int) windowInfo.get("LEVEL");
        } else if(windowInfo.get("CHALLENGE") != null){
            challengePlayed = (int) windowInfo.get("CHALLENGE");
        }

        context = getApplicationContext();
        super.onCreate(savedInstanceState);

        musicEngine = MusicService.getInstance(getApplicationContext());
        musicEngine.stopMusic();
        musicEngine.startGameMusic(game.getSong());

        signedInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(signedInAccount == null) email = "";
        else email = signedInAccount.getEmail();

        selectLayout();
        findAndFillViewParametres();
        initializeVariables();
        setChronometerType();
        getScoreManager();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        scoreboard.loadHighscores(sharedPreferences);

        scoreText.setVisibility(View.VISIBLE);

        if(isLevelMode()) {
            succeededLevels = new SucceededLevel(gameMode.ordinal(), email);
            succeededLevels.loadSuccedeedLevels(sharedPreferences);
        } else if(isChallengeMode()){
            succeededChallenge = new SucceededChallenge(email);
            succeededChallenge.loadChallenges(sharedPreferences);
            deck.addChallengesWon(succeededChallenge.getSuccedeedChallenges());
            scoreText.setVisibility(View.GONE);
        }

        fillButtonsArray();
        deck.assignCardTheme(themeCard, cards, game, buttons, context, email);
    }

    public void showScoreboard(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        scoreboard.addScore(score);
        scoreboard.saveHighscores(sharedPreferences);

        musicEngine.stopMusic();

        intent = new Intent(GameActivity.this, PopupActivity.class);
        gson = new Gson();
        gscoreboard = gson.toJson(scoreboard);
        intent.putExtra("SCORE",gscoreboard);
        intent.putExtra("THEME",themeCard);
        intent.putExtra("LEVELMODE", false);
        intent.putExtra("CHALLENGE", false);
        if(isLevelMode()) {
            if(!succeededLevels.getSuccedeedLevels().contains(levelPlayed)) {
                succeededLevels.addSuccedeedLevel(levelPlayed);
            }
            succeededLevels.saveSucceededLevels(sharedPreferences);
            glevels = gson.toJson(succeededLevels);
            intent.putExtra("LEVELMODE", true);
            intent.putExtra("TYPE", PopupActivity.WindowType.SCOREBOARD);
        } else if(isChallengeMode()){
            if(!succeededChallenge.getSuccedeedChallenges().contains(challengePlayed)) {
                succeededChallenge.addSuccedeedChallenges(challengePlayed);
            }
            succeededChallenge.saveChallenges(sharedPreferences);
            gchallenge = gson.toJson(succeededChallenge);
            intent.putExtra("CHALLENGE", true);
            intent.putExtra("CHALLENGEPLAYED", challengePlayed);
            intent.putExtra("TYPE", PopupActivity.WindowType.CHALLENGE);
        } else {
            intent.putExtra("TYPE", PopupActivity.WindowType.SCOREBOARD);
        }
        startActivityForResult(intent,1);
    }

    public void showGameOver(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        scoreboard.saveHighscores(sharedPreferences);

        intent = new Intent(GameActivity.this, PopupActivity.class);
        gson = new Gson();
        gscoreboard = gson.toJson(scoreboard);
        intent.putExtra("TYPE", PopupActivity.WindowType.GAMEOVER);
        intent.putExtra("SCORE",gscoreboard);
        intent.putExtra("THEME",themeCard);
        intent.putExtra("LEVELMODE", false);
        intent.putExtra("CHALLENGE", false);
        if(isLevelMode()) {
            succeededLevels.saveSucceededLevels(sharedPreferences);
            glevels = gson.toJson(succeededLevels);
            intent.putExtra("LEVELMODE", true);
        }
        else if(isChallengeMode()){
            succeededChallenge.saveChallenges(sharedPreferences);
            gchallenge = gson.toJson(succeededChallenge);
            intent.putExtra("CHALLENGE", true);
        }
        startActivityForResult(intent,1);
    }

    @SuppressLint("WrongViewCast")
    public void changeSprite(View view) {
        if(tapCounter == 0) {
            turnAllCards();
            timeHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                public void run() {
                    startGame();
                }
            }, game.getRevealSeconds() * 1000);
        } else {
            for (Card concreteCard : cards) {
                if(visibleCards < 2 && !isGamePaused)
                    if (concreteCard.getCardButton().getId() == view.getId()) {
                        concreteCard.turnCard();
                        visibleCards++;
                        concreteCard.getCardButton().setClickable(false);
                        if(pairs.size()<2) pairs.add(concreteCard);
                        if(pairs.size() == 2) setPaired();
                    }
            }
        }
        stopChronometer();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startGame() {
        turnAllCards();
        startChronometer();
        isTimeOver();
        pauseButton.setVisibility(View.VISIBLE);
        restartButton.setVisibility(View.VISIBLE);
        tapCounter++;
    }

    @SuppressLint("SetTextI18n")
    private void updateScore(Card card) {
        if(!isChallengeMode()) {
            score = scoreManager.updateScore(previousCorrect, card);
            String poinsText = getLocalePointsText();
            scoreText.setText(poinsText + score);
        }
    }

    private String getLocalePointsText() {
        Locale current = getResources().getConfiguration().locale;
        String poinsText = "Not loaded" + current.toLanguageTag();
        switch(current.toLanguageTag()){
            case "en-US":
            case "en":
                poinsText = "Points: ";
                break;
            case "en-GB":
                poinsText = "Points: ";
                break;
            case "es":
            case "es-ES":
                poinsText = "Puntos: ";
                break;
            case "pt":
                poinsText = "Pontos: ";
                break;
            case "fr":
                poinsText = "Points: ";
                break;
            case "ca":
                poinsText = "Punts: ";
                break;
        }
        return poinsText;
    }

    private void turnAllCards() {
        for (Card concreteCard : cards) {
            concreteCard.turnCard();
        }
        setClickable(buttons);
    }

    private void turnVisibleCards() {
        for (Card concreteCard : cards) {
            concreteCard.turnVisibleCards();
        }
    }

    private void setClickable(Button[] buttons) {
        for (Button b: buttons) {
            for (Card c : cards)
                if(c.getCardButton() == b) b.setClickable(isClickable);
        }
        isClickable = !isClickable;
    }

    private void fillButtonsArray(){
        for(int i = 0; i < maxCards; i++)
            buttons[i] = findViewById(getResources().getIdentifier("imgPos" + i,"id", getPackageName()));
    }

    private void stopChronometer() {
        if(!isCasualMode())
            if(tapErrors == maxErrors) timeHandler.postDelayed(new Runnable() {
                public void run() {
                    showGameOver();
                }
            }, 650);
        if(restantMatches == 0) {
            chronoTimer.stop();
            timeHandler.postDelayed(new Runnable() {
                public void run() {
                    if(score >= game.getMinScore()) {
                        if (game.getId() != 8) showScoreboard();
                        else if ((timeWhenStarted + (game.getSeconds() * 1000) - (SOUNDSECONDS / 3) <= SystemClock.elapsedRealtime())) {
                            showScoreboard();
                        } else showGameOver();
                    }else showGameOver();
                }
            }, 650);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void isTimeOver(){
        if(chronoTimer.isCountDown()) {
            chronoTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    if (timeWhenStarted + (game.getSeconds() * 1000) <= SystemClock.elapsedRealtime()) {
                        chronoTimer.stop();
                        musicEngine.stopExtraSound();
                        timeHandler.postDelayed(new Runnable() {
                            public void run() {
                                showGameOver();
                            }
                        }, 650);
                    } else if (timeWhenStarted + (game.getSeconds() * 1000) - SOUNDSECONDS <= SystemClock.elapsedRealtime()) {
                        musicEngine.startExtraSound(R.raw.clocksound);
                    }
                }
            });
        }
    }

    public void pauseGame(View view) {
        if(pauseTapCounter % 2 == 0) {
            timeWhenStopped = (chronoTimer.getBase() - SystemClock.elapsedRealtime());
            chronoTimer.stop();
            musicEngine.stopMusic();
            musicEngine.stopExtraSound();
            isGamePaused = !isGamePaused;
        }
        else {
            timeWhenStarted = SystemClock.elapsedRealtime() + timeWhenStopped;
            chronoTimer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            chronoTimer.start();
            musicEngine.startGameMusic(game.getSong());
            if(timeWhenStarted + (game.getSeconds() * 1000) - SOUNDSECONDS <= SystemClock.elapsedRealtime())
                musicEngine.startExtraSound(R.raw.clocksound);
            isGamePaused = !isGamePaused;
        }
        pauseTapCounter++;
    }

    public void restart(View view) {
        intent = new Intent(this, GameActivity.class);
        Gson gson = new Gson();
        String newGame = gson.toJson(game);
        intent.putExtra("GAME",newGame);
        startActivity(intent);
        this.finish();
    }

    public void onFinishPressed(View view){
        pauseGame(view);
        Intent intent = new Intent(GameActivity.this, PopupActivity.class);
        intent.putExtra("TYPE", PopupActivity.WindowType.WARNING);
        intent.putExtra("THEME", themeCard);
        intent.putExtra("LEVELMODE", false);
        intent.putExtra("CHALLENGE", false);
        musicEngine.stopExtraSound();
        if(isLevelMode()) {
            intent.putExtra("LEVELMODE", true);
        } else if (isChallengeMode()) {
            intent.putExtra("CHALLENGE", true);
        }
        startActivityForResult(intent,0);
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

    private void getScoreManager() {
        switch(gameMode) {
            case CASUAL:
                scoreManager = new ScoreCasual();
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
    private boolean isLevelMode() { return gameMode.equals(GameMode.LEVELS); }

    private boolean isChallengeMode() { return gameMode.equals(GameMode.CHALLENGE);}

    private boolean isCasualMode() { return gameMode.equals(GameMode.CASUAL); }

    private void findAndFillViewParametres() {
        pauseButton = findViewById(R.id.button_pause);
        pauseButton.setVisibility(View.INVISIBLE);
        restartButton = findViewById(R.id.button_restart);
        restartButton.setVisibility(View.INVISIBLE);
        scoreText = findViewById(R.id.textview_score);
        chronoTimer = findViewById(R.id.textview_timer);
    }

    private void initializeVariables() {
        timeWhenStopped = 0;
        pauseTapCounter = 0;
        tapCounter = 0;
        tapErrors = 0;
        maxCards = game.getCardAmount();
        restantMatches = maxCards / 2;
        maxErrors = game.getMaxFails();
        visibleCards = 0;
        buttons = new Button[maxCards];
        cards = new ArrayList<>(maxCards);
        isClickable = false;
        isGamePaused = false;
        deck = new Deck();
        gameMode = game.getGameMode();
        score = 0;
        scoreboard = new Scoreboard(game.getId(), email);
        previousCorrect = false;
        gscoreboard = ""; glevels = "";
        themeCard = game.getDeckTheme();
    }

    private void setPaired() {
        if (pairs.get(0).equals(pairs.get(1))) {
            musicEngine.startExtraSound(R.raw.parejaacertada);
            for (Card c : pairs) c.setPaired();
            restantMatches--;
            previousCorrect = true;
            visibleCards = 0;
            tapErrors = 0;
        } else {
            tapErrors++;
            previousCorrect = false;
            timeHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                public void run() {
                    turnVisibleCards();
                    visibleCards = 0;
                }
            }, 1000);
            for (Card c : pairs) c.getCardButton().setClickable(true);
        }
        if(!isChallengeMode()) updateScore(pairs.get(0));
        pairs.clear();
    }

}
