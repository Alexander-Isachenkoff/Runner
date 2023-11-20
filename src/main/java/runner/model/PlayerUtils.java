package runner.model;

import runner.Main;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerUtils {

    private static final String PLAYERS_DIR = "players";

    public static List<Player> loadPlayers() {
        URL resource = Main.class.getResource(PLAYERS_DIR);
        return Stream.of(new File(resource.getFile()).listFiles())
                .map(file -> {
                    String jumpImgPath = PLAYERS_DIR + "/" + file.getName() + "/jump.png";
                    return PlayerLoader.load(jumpImgPath, Arrays.stream(new File(file.getPath() + "/walk").list()).map(s -> {
                        return PLAYERS_DIR + "/" + file.getName() + "/walk/" + s;
                    }).toArray(String[]::new));
                })
                .collect(Collectors.toList());
    }

}
