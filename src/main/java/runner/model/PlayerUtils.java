package runner.model;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerUtils {

    private static final String PLAYERS_DIR = "images/players";

    public static List<Player> loadPlayers() {
        return Stream.of(new File(PLAYERS_DIR).listFiles())
                .map(file -> {
                    String jumpImgPath = PLAYERS_DIR + "/" + file.getName() + "/jump.png";
                    return PlayerLoader.load(jumpImgPath, Arrays.stream(new File(file.getPath() + "/walk").list()).map(s -> {
                        return PLAYERS_DIR + "/" + file.getName() + "/walk/" + s;
                    }).toArray(String[]::new));
                })
                .collect(Collectors.toList());
    }

}
