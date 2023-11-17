package runner;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Pagination;

import java.io.IOException;
import java.util.List;

public class MenuController {

    private List<Player> players;
    @FXML
    private Pagination pagination;

    @FXML
    private void initialize() {
        players = PlayerUtils.loadPlayers();
        players.forEach(Player::walk);
        pagination.setPageCount(players.size());
        pagination.setPageFactory(players::get);
    }

    @FXML
    private void onPlay() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/runner.fxml"));
        Parent parent = loader.load();
        RunnerController controller = loader.getController();
        controller.setPlayer(players.get(pagination.getCurrentPageIndex()));
        pagination.getScene().setRoot(parent);
    }

}
