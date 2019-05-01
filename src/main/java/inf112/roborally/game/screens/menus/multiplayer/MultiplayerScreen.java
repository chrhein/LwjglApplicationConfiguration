package inf112.roborally.game.screens.menus.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import inf112.roborally.game.RoboRallyGame;
import inf112.roborally.game.screens.BasicScreen;
import inf112.roborally.game.screens.menus.MenuScreen;
import inf112.roborally.game.screens.menus.setup.SelectNumPlayers;
import inf112.roborally.game.tools.ButtonFactory;

public class MultiplayerScreen extends BasicScreen {

    private Screen previousScreen;

    public MultiplayerScreen(final RoboRallyGame game, Screen previousScreen) {
        super(game);
        this.previousScreen = previousScreen;
        final SelectNumPlayers selectNumPlayers = new SelectNumPlayers(game);
        selectNumPlayers.setPreviousScreen(this);
        final Screen joinServerScreen = new JoinServerScreen(game, this);

        TextButton join = ButtonFactory.createTextButton("Join Session", 2);
        join.setTransform(true);
        join.setWidth(700);
        join.setPosition(1920 / 2f - join.getWidth() / 2f, 550);
        join.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(joinServerScreen);
                //dispose();
            }
        });
        TextButton create = ButtonFactory.createTextButton("Create Session", 2);
        create.setTransform(true);
        create.setWidth(700);
        create.setPosition(1920 / 2f - create.getWidth() / 2f, 350);
        create.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(selectNumPlayers);
                selectNumPlayers.setNextScreen(null); //Setting to null instead of creating the HostServerScreen here.
                //dispose();
            }
        });

        stage.addActor(join);
        stage.addActor(create);
    }

    @Override
    protected void goToPreviousScreen() {
        game.setScreen(previousScreen);
        //dispose();
    }


    protected void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
        else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) ;
    }
}