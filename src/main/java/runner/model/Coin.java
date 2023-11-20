package runner.model;

import javafx.animation.ScaleTransition;
import javafx.scene.image.Image;
import javafx.util.Duration;
import runner.Main;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class Coin extends GameObject {

    private static final int SIZE = 50;
    private final int cost;

    private Coin(int cost, Image image) {
        super(image);
        this.cost = cost;

        getImgRect().setWidth(SIZE);
        getImgRect().setHeight(SIZE);
        getColliderRect().setWidth(SIZE / 2.);
        getColliderRect().setHeight(SIZE / 2.);
        getColliderRect().setTranslateX(SIZE * 0.25);
        getColliderRect().setTranslateY(SIZE * 0.25);

        ScaleTransition st = new ScaleTransition(Duration.millis(500), this);
        st.setFromX(1);
        st.setToX(0.1);
        st.setAutoReverse(true);
        st.setCycleCount(Integer.MAX_VALUE);
        st.play();
    }

    private static Coin createBronze() {
        return new Coin(100, new Image(Main.class.getResourceAsStream("coins/coinBronze.png")));
    }

    private static Coin createSilver() {
        return new Coin(200, new Image(Main.class.getResourceAsStream("coins/coinSilver.png")));
    }

    private static Coin createGold() {
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
