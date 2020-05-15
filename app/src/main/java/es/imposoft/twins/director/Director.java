package es.imposoft.twins.director;

import es.imposoft.twins.builders.ConcreteBuilderLevel;
import es.imposoft.twins.components.Chronometer;
import es.imposoft.twins.components.DeckTheme;
import es.imposoft.twins.components.GameMode;
import es.imposoft.twins.components.GameType;

public class Director {

    private DeckTheme deckTheme;

    public Director(DeckTheme deckTheme){
        this.deckTheme = deckTheme;
    }

    public void constructStandardGame(ConcreteBuilderLevel builder){
        builder.setCardTheme(deckTheme);
        builder.setMinScore(0);
        builder.setCardAmount(16);
        builder.setRevealTime(3);
        builder.setChronometer(45, Chronometer.NORMAL);
        //builder.setScoreManager(new ScoreStandard());
        builder.setGameMode(GameMode.STANDARD);
        builder.setId(1);
        builder.setGameType(GameType.NORMAL);
    }

    public void constructCasualGame(ConcreteBuilderLevel builder){
        builder.setCardTheme(deckTheme);
        builder.setMinScore(0);
        builder.setCardAmount(20);
        builder.setRevealTime(3);
        builder.setChronometer(90, Chronometer.NONE);
        //builder.setScoreManager(new ScoreFree());
        builder.setGameMode(GameMode.CASUAL);
        builder.setId(2);
        builder.setGameType(GameType.NORMAL);
    }

    public void constructLevel1(ConcreteBuilderLevel builder){
        builder.setCardTheme(DeckTheme.EMOJI);
        builder.setMinScore(5);
        builder.setCardAmount(16);
        builder.setRevealTime(3);
        builder.setChronometer(60, Chronometer.DESCENDING);
        //builder.setScoreManager(new ScoreLevels());
        builder.setGameMode(GameMode.LEVELS);
        builder.setId(3);
        builder.setGameType(GameType.NORMAL);
    }

    public void constructLevel2(ConcreteBuilderLevel builder){
        builder.setCardTheme(DeckTheme.CARS);
        builder.setMinScore(25);
        builder.setCardAmount(16);
        builder.setRevealTime(2);
        builder.setChronometer(50, Chronometer.DESCENDING);
        //builder.setScoreManager(new ScoreLevels());
        builder.setGameMode(GameMode.LEVELS);
        builder.setId(4);
        builder.setGameType(GameType.NORMAL);
    }

    public void constructLevel3(ConcreteBuilderLevel builder){
        builder.setCardTheme(DeckTheme.EMOJI);
        builder.setMinScore(30);
        builder.setCardAmount(20);
        builder.setRevealTime(2);
        builder.setChronometer(60, Chronometer.DESCENDING);
        //builder.setScoreManager(new ScoreLevels());
        builder.setGameMode(GameMode.LEVELS);
        builder.setId(5);
        builder.setGameType(GameType.NORMAL);
    }

    public void constructLevel4(ConcreteBuilderLevel builder){
        builder.setCardTheme(DeckTheme.CARS);//Deck.EMOJI
        builder.setMinScore(55);
        builder.setCardAmount(20);
        builder.setRevealTime(1);
        builder.setChronometer(50, Chronometer.DESCENDING);
        //builder.setScoreManager(new ScoreLevels());
        builder.setGameMode(GameMode.LEVELS);
        builder.setId(6);
        builder.setGameType(GameType.NORMAL);
    }

    public void constructLevel5(ConcreteBuilderLevel builder){
        builder.setCardTheme(DeckTheme.EMOJI);//Deck.EMOJI
        builder.setMinScore(50);
        builder.setCardAmount(24);
        builder.setRevealTime(1);
        builder.setChronometer(50, Chronometer.DESCENDING);
        //builder.setScoreManager(new ScoreLevels());
        builder.setGameMode(GameMode.LEVELS);
        builder.setId(7);
        builder.setGameType(GameType.NORMAL);
    }
}
