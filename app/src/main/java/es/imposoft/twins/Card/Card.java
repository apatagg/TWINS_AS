package es.imposoft.twins.Card;

import android.graphics.Bitmap;
import android.widget.Button;

public interface Card {
    Bitmap getFrontImage();
    void setFrontImage(Bitmap frontImage);
    void setBackImage(Bitmap backImage);
    Button getCardButton();
    void setPaired();
    void turnCard();
    void turnVisibleCards();
    boolean equals(ConcreteCard concreteCard);
    int getPoints();
    void setPoints(int points);
}
