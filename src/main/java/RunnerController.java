import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
    public AnchorPane gamePane;
    public Label scoreLabel;
    public Label recordLabel;

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

        restart();
    }

    public void setPlayer(Player player) {
        this.player = player;
        gamePane.getChildren().add(player);
        player.setTranslateX(80);
        AnchorPane.setBottomAnchor(player, 0.0);
    }

    private void restart() {
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
            if (keysPressed.contains(KeyCode.SPACE)) {
                player.jump();
            }
        }

        for (Rectangle obstacle : obstacles) {
            obstacle.setTranslateX(obstacle.getTranslateX() - speed * dtSeconds);
            if (obstacle.getBoundsInParent().intersects(player.getBoundsInParent())) {
                timer.stop();
                player.stop();
                Alert alert = new Alert(Alert.AlertType.NONE, "", ButtonType.OK);
                alert.setHeaderText("Game Over");
                alert.setOnHidden(event -> {
                    restart();
                });
                alert.show();
            }
        }

        if (lastUpdate - lastGen > genTimeOut * 1e9) {
            lastGen = lastUpdate;
            int i = new Random().nextInt(obstaclesImages.size());
            Image image = obstaclesImages.get(i);
            Rectangle rectangle = new Rectangle(image.getWidth(), image.getHeight());
            rectangle.setFill(new ImagePattern(image));
            gamePane.getChildren().add(rectangle);
//            rectangle.setStroke(Color.RED);
//            rectangle.setStrokeWidth(1);
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

}