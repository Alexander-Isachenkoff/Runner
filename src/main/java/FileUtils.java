import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
    static List<Image> getImages(String dir) {
        return Stream.of(new File(Main.class.getResource(dir).getFile()).listFiles())
                .map(file -> {
                    try {
                        return new Image(Files.newInputStream(file.toPath()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}
