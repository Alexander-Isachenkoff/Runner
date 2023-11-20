package runner.model;

import javafx.animation.*;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;

import java.util.List;

public class Player extends GameObject {

    private static final double WIDTH = 40;
    private static final double HEIGHT = 54;
    private final Image jumpImage;
    private final TranslateTransition jumpTransition;
    private final Timeline walkAnimation;

    public Player(Image jumpImage, List<Image> walkImages) {
        this.jumpImage = jumpImage;
        getImgRect().setWidth(WIDTH);
        getImgRect().setHeight(HEIGHT);

        getColliderRect().setWidth(WIDTH * 0.55);
        getColliderRect().setHeight(HEIGHT);
        getColliderRect().setTranslateX(WIDTH * 0.25);

        walkAnimation = new Timeline();
        walkAnimation.setCycleCount(Animation.INDEFINITE);
        for (int i = 0; i < walkImages.size(); i++) {
            Image image = walkImages.get(i);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(50 * i), new KeyValue(getImgRect().fillProperty(), new ImagePattern(image)));
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
            getImgRect().setFill(new ImagePattern(jumpImage));
        }
    }

    public boolean inJump() {
        return jumpTransition.getStatus() == Animation.Status.RUNNING;
    }

    public void stop() {
        jumpTransition.stop();
        walkAnimation.stop();
    }

}
