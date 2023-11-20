package runner.model;

import javafx.scene.image.Image;

public class Obstacle extends GameObject {

    public Obstacle(Image image) {
        super(image);
        getColliderRect().setWidth(image.getWidth());
        getColliderRect().setHeight(image.getHeight());
    }

}
