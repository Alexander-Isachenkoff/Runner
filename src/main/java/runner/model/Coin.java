package runner.model;

import javafx.animation.ScaleTransition;
import javafx.scene.image.Image;
import javafx.util.Duration;
import runner.FileUtils;

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
        return new Coin(10, FileUtils.loadImage("images/coins/coinBronze.png"));
    }

    private static Coin createSilver() {
        return new Coin(20, FileUtils.loadImage("images/coins/coinSilver.png"));
    }

    private static Coin createGold() {
        return new Coin(30, FileUtils.loadImage("images/coins/coinGold.png"));
    }

    public static Coin createRandom() {
        List<Supplier<Coin>> list = Arrays.asList(
                Coin::createBronze,
                Coin::createSilver,
                Coin::createGold
        );
        int i = new Random().nextInt(list.size());
        return list.get(i).get();
    }

    public int getCost() {
        return cost;
    }

}
