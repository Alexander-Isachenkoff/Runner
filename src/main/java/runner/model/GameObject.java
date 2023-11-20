package runner.model;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class GameObject extends Group {

    private final Rectangle imgRect = new Rectangle();
    private final Rectangle colliderRect = new Rectangle();

    GameObject() {
        setEffect(new DropShadow(BlurType.THREE_PASS_BOX, Color.GRAY, 10, 0, 10, 10));
        getChildren().addAll(imgRect, colliderRect);
        colliderRect.setFill(Color.TRANSPARENT);
    }

    GameObject(Image image) {
        this();
        imgRect.setFill(new ImagePattern(image));
        imgRect.setWidth(image.getWidth());
        imgRect.setHeight(image.getHeight());
    }

    Rectangle getImgRect() {
        return imgRect;
    }

    Rectangle getColliderRect() {
        return colliderRect;
    }

    public Bounds getColliderBounds() {
        return localToParent(colliderRect.getBoundsInParent());
    }

}
