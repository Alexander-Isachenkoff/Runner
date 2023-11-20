package runner;

import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;

public class Player extends Group {

    private static final double WIDTH = 40;
    private static final double HEIGHT = 54;
    private final Image jumpImage;
    private final TranslateTransition jumpTransition;
    private final Timeline walkAnimation;
    private final Rectangle imgRect = new Rectangle();
    private final Rectangle colliderRect = new Rectangle();

    public Player(Image jumpImage, List<Image> walkImages) {
        this.jumpImage = jumpImage;
        getChildren().addAll(imgRect, colliderRect);
        imgRect.setWidth(WIDTH);
        imgRect.setHeight(HEIGHT);

        colliderRect.setWidth(WIDTH * 0.55);
        colliderRect.setHeight(HEIGHT);
        colliderRect.setTranslateX(WIDTH * 0.25);
        colliderRect.setFill(Color.TRANSPARENT);

        walkAnimation = new Timeline();
        walkAnimation.setCycleCount(Animation.INDEFINITE);
        for (int i = 0; i < walkImages.size(); i++) {
            Image image = walkImages.get(i);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(50 * i), new KeyValue(imgRect.fillProperty(), new ImagePattern(image)));
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
            imgRect.setFill(new ImagePattern(jumpImage));
        }
    }

    public Bounds getColliderBounds() {
        return localToParent(colliderRect.getBoundsInParent());
    }

    public boolean inJump() {
        return jumpTransition.getStatus() == Animation.Status.RUNNING;
    }

    public void stop() {
        jumpTransition.stop();
        walkAnimation.stop();
    }

}
