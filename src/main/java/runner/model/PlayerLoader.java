package runner.model;

import javafx.scene.image.Image;
import runner.Main;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerLoader {

    public static Player load(String jumpImgPath, String... walkImagesPaths) {
        Image jumpImage = new Image(Main.class.getResourceAsStream(jumpImgPath));
        List<Image> walkImages = Arrays.stream(walkImagesPaths).map(path -> {
            return new Image(Main.class.getResourceAsStream(path));
        }).collect(Collectors.toList());
        return new Player(jumpImage, walkImages);
    }

}
