package es.imposoft.twins.builders;

import es.imposoft.twins.components.Chronometer;

public interface LevelBuilder {
    void setChronometer(int seconds, Chronometer type); //Poner un tiempo no un int
    void setMinScore(int score);
    void setCardAmount(int amount); //Yo haria mas bien 3 tipos de partidas por cantidad de cartas (para facilitar programar)
    void setRevealTime(int tiempo);
}
