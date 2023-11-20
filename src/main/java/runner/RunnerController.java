package runner;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class RunnerController {

    private static final String RECORD_FILE = "record";
    private static final double ACC = 5;
    private final Set<KeyCode> keysPressed = new HashSet<>();
    private final List<Image> obstaclesImages = FileUtils.getImages("obstacles");
    private final Set<Rectangle> obstacles = new HashSet<>();
    private final double START_SPEED = 300;
    private final double START_TIME_OUT = 2;
    private final IntegerProperty record = new SimpleIntegerProperty(-1);
    private final IntegerProperty score = new SimpleIntegerProperty(-1);
    @FXML
    private AnchorPane gamePane;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label recordLabel;

    private Player player;

    private long lastUpdate;
    private long lastGen;
    private AnimationTimer timer;
    private double speed = START_SPEED;
    private double genTimeOut = START_TIME_OUT;
    private long startTime;

    private static void saveRecord(int value) {
        try (DataOutputStream os = new DataOutputStream(Files.newOutputStream(Paths.get(RECORD_FILE)))) {
            os.writeInt(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int loadRecord() {
        try (DataInputStream inputStream = new DataInputStream(Files.newInputStream(Paths.get(RECORD_FILE)))) {
            return inputStream.readInt();
        } catch (IOException e) {
            return 0;
        }
    }

    @FXML
    private void initialize() {
        score.addListener((observable, oldValue, newValue) -> {
            scoreLabel.setText(String.valueOf(newValue.intValue()));
        });
        record.addListener((observable, oldValue, newValue) -> {
            recordLabel.setText(String.valueOf(newValue.intValue()));
            saveRecord(newValue.intValue());
        });

        record.set(loadRecord());

        gamePane.sceneProperty().addListener((observable, oldValue, scene) -> {
            if (scene == null) {
                return;
            }
            scene.setOnKeyPressed(event -> {
                keysPressed.add(event.getCode());
            });
            scene.setOnKeyReleased(event -> {
                keysPressed.remove(event.getCode());
            });
        });

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update(now);
            }
        };
    }

    void setPlayer(Player player) {
        this.player = player;
        gamePane.getChildren().add(player);
        AnchorPane.setBottomAnchor(player, 0.0);
    }

    void restart() {
        player.setLayoutX(0);
        player.setTranslateX(100);
        player.setTranslateY(0);
        speed = START_SPEED;
        genTimeOut = START_TIME_OUT;
        keysPressed.clear();
        gamePane.getChildren().removeAll(obstacles);
        obstacles.clear();
        timer.stop();
        lastUpdate = 0;
        timer.start();
        score.set(0);
    }

    private void update(long now) {
        if (lastUpdate == 0) {
            startTime = now;
            lastUpdate = now;
        }
        double dtSeconds = (now - lastUpdate) / 1e9;
        lastUpdate = now;

        if (!player.inJump()) {
            if (keysPressed.isEmpty()) {
                player.walk();
            }
            if (keysPressed.contains(KeyCode.SPACE) || keysPressed.contains(KeyCode.UP)) {
                player.jump();
            }
        }

        for (Rectangle obstacle : new ArrayList<>(obstacles)) {
            obstacle.setTranslateX(obstacle.getTranslateX() - speed * dtSeconds);
            if (obstacle.getTranslateX() < -obstacle.getWidth()) {
                gamePane.getChildren().remove(obstacle);
                obstacles.remove(obstacle);
            }
            if (obstacle.getBoundsInParent().intersects(player.getColliderBounds())) {
                gameOver();
            }
        }

        if (lastUpdate - lastGen > genTimeOut * 1e9) {
            lastGen = lastUpdate;
            int i = new Random().nextInt(obstaclesImages.size());
            Image image = obstaclesImages.get(i);
            Rectangle rectangle = new Rectangle(image.getWidth(), image.getHeight());
            rectangle.setFill(new ImagePattern(image));
            gamePane.getChildren().add(rectangle);
            AnchorPane.setBottomAnchor(rectangle, 0.0);
            rectangle.setTranslateX(gamePane.getWidth());
            obstacles.add(rectangle);
        }

        speed += dtSeconds * ACC;
        genTimeOut -= dtSeconds * 0.01;

        score.set((int) ((now - startTime) / 1e8));
        if (score.get() > record.get()) {
            record.set(score.get());
        }
    }

    private void gameOver() {
        timer.stop();
        player.stop();

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/game_over.fxml"));
        Parent load;
        try {
            load = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        VBox wrapper = new VBox(load);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setFillWidth(false);
        gamePane.getChildren().add(wrapper);
        AnchorPane.setTopAnchor(wrapper, 0.0);
        AnchorPane.setBottomAnchor(wrapper, 0.0);
        AnchorPane.setLeftAnchor(wrapper, 0.0);
        AnchorPane.setRightAnchor(wrapper, 0.0);

        GameOverController controller = loader.getController();

        controller.setOnMenu(this::onMenu);
        controller.setOnRestart(() -> {
            gamePane.getChildren().remove(wrapper);
            restart();
        });
    }

    @FXML
    private void onMenu() {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/menu.fxml"));
        Parent parent;
        try {
            parent = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        gamePane.getScene().setRoot(parent);
    }

}
