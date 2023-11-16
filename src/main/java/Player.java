import javafx.animation.*;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;

public class Player extends Rectangle {

    private final Image jumpImage;
    private final TranslateTransition jumpTransition;
    private final Timeline walkAnimation;

    public Player(String dir) {
        setWidth(40);
        setHeight(54);

        jumpImage = new Image(Main.class.getResourceAsStream("players/" + dir + "/jump.png"));

        List<Image> walkImages = FileUtils.getImages("players/" + dir + "/walk");

        walkAnimation = new Timeline();
        walkAnimation.setCycleCount(Animation.INDEFINITE);
        for (int i = 0; i < walkImages.size(); i++) {
            Image image = walkImages.get(i);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(50 * i), new KeyValue(fillProperty(), new ImagePattern(image)));
            walkAnimation.getKeyFrames().add(keyFrame);
        }

        jumpTransition = new TranslateTransition(Duration.millis(400), this);
        jumpTransition.setByY(-100);
        jumpTransition.setAutoReverse(true);
        jumpTransition.setCycleCount(2);
    }

    public void walk() {
        walkAnimation.play();
    }

    public void jump() {
        if (!inJump()) {
            walkAnimation.jumpTo(Duration.ZERO);
            walkAnimation.stop();
            jumpTransition.play();
            setFill(new ImagePattern(jumpImage));
        }
    }

    public boolean inJump() {
        return jumpTransition.getStatus() == Animation.Status.RUNNING;
    }

    public void stop() {
        walkAnimation.stop();
    }

}
