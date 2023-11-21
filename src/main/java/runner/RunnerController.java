package runner;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import runner.model.*;

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
    private final List<Image> obstaclesImages = FileUtils.getImages("images/obstacles");
    private final Set<GameObject> gameObjects = new HashSet<>();
    private final double START_SPEED = 300;
    private final double START_TIME_OUT = 2;
    private final IntegerProperty record = new SimpleIntegerProperty(-1);
    private final DoubleProperty score = new SimpleDoubleProperty(-1);
    private final Image bgImage = FileUtils.loadImage("images/background/bg_desert.png");
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
    private double distance;
    private Floor floor;

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

        floor = new Floor();
        gamePane.getChildren().add(floor);
        AnchorPane.setBottomAnchor(floor, 0.0);
        AnchorPane.setLeftAnchor(floor, 0.0);
        AnchorPane.setRightAnchor(floor, 0.0);
    }

    void setPlayer(Player player) {
        this.player = player;
        gamePane.getChildren().add(0, player);
        AnchorPane.setBottomAnchor(player, (double) Floor.SIZE);
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
        distance = 0;
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

        double deltaDistance = speed * dtSeconds;
        distance += deltaDistance;

        floor.updateBackground(-distance);
        this.updateBackground();

        for (GameObject gameObject : new ArrayList<>(gameObjects)) {
            gameObject.setTranslateX(gameObject.getTranslateX() - deltaDistance);
            if (gameObject.getTranslateX() < -gameObject.getBoundsInParent().getWidth()) {
                gamePane.getChildren().remove(gameObject);
                gameObjects.remove(gameObject);
            }

            if (gameObject.intersects(player)) {
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
            addGameObject(obstacle);
            AnchorPane.setBottomAnchor(obstacle, (double) Floor.SIZE);
            obstacle.setTranslateX(gamePane.getWidth());

            Coin coin = Coin.createRandom();
            addGameObject(coin);
            AnchorPane.setBottomAnchor(coin, (double) Floor.SIZE);
            coin.setTranslateX(gamePane.getWidth() - 100);
        }

        speed += dtSeconds * ACC;
        obstacleGenTimeOut -= dtSeconds * 0.01;

        score.set(score.get() + dtSeconds * 10);
        int intScore = (int) score.get();
        if (intScore > record.get()) {
            record.set(intScore);
        }
    }

    private void addGameObject(GameObject gameObject) {
        gamePane.getChildren().add(0, gameObject);
        gameObjects.add(gameObject);
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

    private void updateBackground() {
        BackgroundPosition position = new BackgroundPosition(Side.LEFT, -distance / 10, false, Side.TOP, 0, false);
        gamePane.setBackground(new Background(new BackgroundImage(bgImage, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, position, BackgroundSize.DEFAULT)));
    }

}
