package inf112.roborally.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import inf112.roborally.game.RoboRallyGame;
import inf112.roborally.game.board.ProgramCard;
import inf112.roborally.game.gui.ProgramRegisterDisplay;
import inf112.roborally.game.objects.Player;
import inf112.roborally.game.enums.Rotate;

public class TestScreen implements Screen {
    private final RoboRallyGame game;
    private ProgramRegisterDisplay programRegisterDisplay;
    private Player player;
    private Sprite background;


    public TestScreen(RoboRallyGame game) {
        this.game = game;
        player = new Player(0,0);
        programRegisterDisplay = new ProgramRegisterDisplay(player);
        for(int i = 0; i < 9; i++){
            player.receiveCard(new ProgramCard(Rotate.NONE,2,0));
        }

        background = new Sprite(new Texture("assets/img/testscreen.png"));
        game.fixedViewPort.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float v) {
        float r = 0/255f;
        float g = 20/255f;
        float b = 15/255f;

        Gdx.gl.glClearColor(r,g,b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(game.fixedCamera.combined);
        game.batch.begin();
        background.draw(game.batch);
        programRegisterDisplay.draw(game.batch);
        game.batch.end();
        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            Gdx.app.exit();
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.D)){
            player.takeDamage();
            player.update();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S)){
            player.getRegisters().pickCard(0);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)){
            player.returnCards();
        }
    }

    @Override
    public void resize(int w, int h) {
        game.fixedViewPort.update(w,h);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
    }
}
