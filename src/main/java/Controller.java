import javafx.animation.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller {

    private static final String RECORD_FILE = "record";
    private static final double ACC = 5;
    private final Set<KeyCode> keysPressed = new HashSet<>();
    private final List<Image> obstaclesImages = getImages("obstacles");
    private final Set<Rectangle> obstacles = new HashSet<>();
    private final double START_SPEED = 300;
    private final double START_TIME_OUT = 2;
    private final IntegerProperty record = new SimpleIntegerProperty(-1);
    private final IntegerProperty score = new SimpleIntegerProperty(-1);
    public AnchorPane gamePane;
    public Label scoreLabel;
    public Label recordLabel;
    @FXML
    private Rectangle player;
    private Image jumpImage;
    private TranslateTransition jumpTransition;
    private Timeline walkAnimation;
    private long lastUpdate;
    private long lastGen;
    private AnimationTimer timer;
    private double speed = START_SPEED;
    private double genTimeOut = START_TIME_OUT;
    private long startTime;

    private static List<Image> getImages(String dir) {
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

        jumpImage = new Image(Main.class.getResourceAsStream("p1_jump.png"));

        List<Image> walkImages = getImages("walk");

        walkAnimation = new Timeline();
        walkAnimation.setCycleCount(Animation.INDEFINITE);
        for (int i = 0; i < walkImages.size(); i++) {
            Image image = walkImages.get(i);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(50 * i), new KeyValue(player.fillProperty(), new ImagePattern(image)));
            walkAnimation.getKeyFrames().add(keyFrame);
        }

        jumpTransition = new TranslateTransition(Duration.millis(400), player);
        jumpTransition.setByY(-120);
        jumpTransition.setAutoReverse(true);
        jumpTransition.setCycleCount(2);

        player.sceneProperty().addListener((observable, oldValue, scene) -> {
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

    private void jump() {
        player.setFill(new ImagePattern(jumpImage));
    }

    private void update(long now) {
        if (lastUpdate == 0) {
            startTime = now;
            lastUpdate = now;
        }
        double dtSeconds = (now - lastUpdate) / 1e9;
        lastUpdate = now;

        if (jumpTransition.getStatus() != Animation.Status.RUNNING) {
            if (keysPressed.isEmpty()) {
                walkAnimation.play();
            }
            if (keysPressed.contains(KeyCode.SPACE)) {
                if (jumpTransition.getStatus() != Animation.Status.RUNNING) {
                    walkAnimation.jumpTo(Duration.ZERO);
                    walkAnimation.stop();
                    jumpTransition.play();
                    jump();
                }
            }
        }

        for (Rectangle obstacle : obstacles) {
            obstacle.setTranslateX(obstacle.getTranslateX() - speed * dtSeconds);
            if (obstacle.getBoundsInParent().intersects(player.getBoundsInParent())) {
                timer.stop();
                walkAnimation.stop();
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
            rectangle.setStroke(Color.RED);
            rectangle.setStrokeWidth(1);
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
