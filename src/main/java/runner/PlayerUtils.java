package runner;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerUtils {

    public static List<Player> loadPlayers() {
        return Stream.of(new File(Main.class.getResource("players").getFile()).listFiles())
                .map(file -> new Player(file.getName()))
                .collect(Collectors.toList());
    }

}
