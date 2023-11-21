package runner;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
    static List<Image> getImages(String dir) {
        return Stream.of(new File(dir).listFiles())
                .map(File::getPath)
                .map(FileUtils::loadImage)
                .collect(Collectors.toList());
    }

    public static Image loadImage(String path) {
        try {
            return new Image(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
