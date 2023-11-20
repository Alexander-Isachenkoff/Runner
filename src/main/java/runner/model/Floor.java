package runner.model;

import javafx.geometry.Side;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import runner.Main;

public class Floor extends Pane {

    public static final int SIZE = 30;
    private final Image image;

    public Floor() {
        setMinHeight(SIZE);
        image = new Image(Main.class.getResourceAsStream("floor/grassMid.png"));
        updateBackground(0);
    }

    public void updateBackground(double shift) {
        BackgroundPosition position = new BackgroundPosition(Side.LEFT, shift, false, Side.TOP, 0, false);
        BackgroundSize size = new BackgroundSize(400, SIZE, false, false, false, false);
        setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, position, size)));
    }

}
