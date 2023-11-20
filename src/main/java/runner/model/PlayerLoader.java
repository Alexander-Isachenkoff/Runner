package runner.model;

import javafx.scene.image.Image;
import runner.FileUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class PlayerLoader {

    static Player load(String jumpImgPath, String... walkImagesPaths) {
        Image jumpImage = FileUtils.loadImage(jumpImgPath);
        List<Image> walkImages = Arrays.stream(walkImagesPaths)
                .map(FileUtils::loadImage)
                .collect(Collectors.toList());
        return new Player(jumpImage, walkImages);
    }

}
