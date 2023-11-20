package runner;

import javafx.animation.ScaleTransition;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class Coin extends Rectangle {

    private final int cost;

    private Coin(int cost, Image image) {
        this.cost = cost;
        setFill(new ImagePattern(image));
        setWidth(50);
        setHeight(50);

        ScaleTransition st = new ScaleTransition(Duration.millis(500), this);
        st.setFromX(1);
        st.setToX(0.1);
        st.setAutoReverse(true);
        st.setCycleCount(Integer.MAX_VALUE);
        st.play();
    }

    public static Coin createBronze() {
        return new Coin(100, new Image(Main.class.getResourceAsStream("coins/coinBronze.png")));
    }

    public static Coin createSilver() {
        return new Coin(200, new Image(Main.class.getResourceAsStream("coins/coinSilver.png")));
    }

    public static Coin createGold() {
        return new Coin(300, new Image(Main.class.getResourceAsStream("coins/coinGold.png")));
    }

    public static Coin createRandom() {
        int i = new Random().nextInt(3);
        List<Supplier<Coin>> list = Arrays.asList(
                Coin::createBronze,
                Coin::createSilver,
                Coin::createGold
        );
        return list.get(i).get();
    }

    public int getCost() {
        return cost;
    }

}
