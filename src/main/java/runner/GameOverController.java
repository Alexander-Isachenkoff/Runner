package runner;

import javafx.fxml.FXML;

public class GameOverController {

    private Runnable onMenu = () -> {
    };
    private Runnable onRestart = () -> {
    };

    @FXML
    private void onMenu() {
        onMenu.run();
    }

    @FXML
    private void onRestart() {
        onRestart.run();
    }

    void setOnMenu(Runnable onMenu) {
        this.onMenu = onMenu;
    }

    void setOnRestart(Runnable onRestart) {
        this.onRestart = onRestart;
    }

}
