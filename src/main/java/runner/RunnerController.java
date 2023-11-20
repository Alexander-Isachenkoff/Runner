package runner;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import runner.model.Coin;
import runner.model.GameObject;
import runner.model.Obstacle;
import runner.model.Player;

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
    private final Set<GameObject> gameObjects = new HashSet<>();
    private final double START_SPEED = 300;
    private final double START_TIME_OUT = 2;
    private final IntegerProperty record = new SimpleIntegerProperty(-1);
    private final DoubleProperty score = new SimpleDoubleProperty(-1);
    @FXML
    private AnchorPane gamePane;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label recordLabel;

    private Player player;

    private long lastUpdate;
    private long lastObstacleGen;
    private AnimationTimer timer;
    private double speed = START_SPEED;
    private double obstacleGenTimeOut = START_TIME_OUT;

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
        obstacleGenTimeOut = START_TIME_OUT;
        keysPressed.clear();
        gamePane.getChildren().removeAll(gameObjects);
        gameObjects.clear();
        timer.stop();
        lastUpdate = 0;
        timer.start();
        score.set(0);
    }

    private void update(long now) {
        if (lastUpdate == 0) {
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

        for (GameObject gameObject : new ArrayList<>(gameObjects)) {
            gameObject.setTranslateX(gameObject.getTranslateX() - speed * dtSeconds);
            if (gameObject.getTranslateX() < -gameObject.getBoundsInParent().getWidth()) {
                gamePane.getChildren().remove(gameObject);
                gameObjects.remove(gameObject);
            }

            if (gameObject.getColliderBounds().intersects(player.getColliderBounds())) {
                if (gameObject instanceof Obstacle) {
                    gameOver();
                }
                if (gameObject instanceof Coin) {
                    Coin coin = (Coin) gameObject;
                    gamePane.getChildren().remove(coin);
                    gameObjects.remove(coin);
                    score.set(score.get() + coin.getCost());
                }
            }
        }

        if (lastUpdate - lastObstacleGen > obstacleGenTimeOut * 1e9) {
            lastObstacleGen = lastUpdate;
            int i = new Random().nextInt(obstaclesImages.size());
            Image image = obstaclesImages.get(i);
            Obstacle obstacle = new Obstacle(image);
            gamePane.getChildren().add(obstacle);
            AnchorPane.setBottomAnchor(obstacle, 0.0);
            obstacle.setTranslateX(gamePane.getWidth());
            gameObjects.add(obstacle);

            Coin coin = Coin.createRandom();
            gamePane.getChildren().add(coin);
            AnchorPane.setBottomAnchor(coin, 0.0);
            coin.setTranslateX(gamePane.getWidth() - 100);
            gameObjects.add(coin);
        }

        speed += dtSeconds * ACC;
        obstacleGenTimeOut -= dtSeconds * 0.01;

        score.set(score.get() + dtSeconds * 10);
        int intScore = (int) score.get();
        if (intScore > record.get()) {
            record.set(intScore);
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
