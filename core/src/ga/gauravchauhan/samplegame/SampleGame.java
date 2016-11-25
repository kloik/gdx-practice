package ga.gauravchauhan.samplegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SampleGame extends ApplicationAdapter {

    private final static Vector2 damping = new Vector2(.99f, .99f);
    private static final float TAP_DRAW_TIME_MAX = 1f;
    private static final float TOUCH_IMPULSE = 500;
    Viewport viewport;
    SpriteBatch batch;
    FPSLogger fpsLogger;
    OrthographicCamera camera;
    Animation plane;
    float terrainOffset;
    float tapDrawTime;
    float planeAnimTime;
    Vector2 planeVelocity = new Vector2();
    Vector2 planePosition = new Vector2();
    Vector2 planeDefaultPosition = new Vector2();
    Vector2 gravity = new Vector2();
    Vector2 tmpVector = new Vector2();
    Vector2 scrollVelocity = new Vector2();
    Vector3 touchPosition = new Vector3();
    Array<Vector2> pillars = new Array<Vector2>();
    TextureAtlas atlas;
    TextureRegion bgRegion;
    TextureRegion terrainBelow;
    TextureRegion terrainAbove;
    TextureRegion tapIndicator;
    TextureRegion tap1;
    Texture gameOver;
    GameState gameState = GameState.INIT;
    Vector2 lastPillarPosition = new Vector2();
    TextureRegion pillarUp;
    TextureRegion pillarDown;
    Rectangle planeRect = new Rectangle(), obstacleRect = new Rectangle();

    @Override
    public void create() {
        batch = new SpriteBatch();
        fpsLogger = new FPSLogger();
        // img = new Texture("badlogic.jpg");

        atlas = new TextureAtlas(Gdx.files.internal("ThrustCopter.pack"));

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        viewport = new FitViewport(800, 480, camera);

        tap1 = atlas.findRegion("tap1");
        tapIndicator = atlas.findRegion("tap2");
        gameOver = new Texture("gameOver.png");
        bgRegion = atlas.findRegion("background");
        terrainBelow = atlas.findRegion("groundGrass");
        terrainAbove = new TextureRegion(terrainBelow);
        pillarUp = atlas.findRegion("rockGrassUp");
        pillarDown = atlas.findRegion("rockGrassDown");
        terrainAbove.flip(true, true);

        plane = new Animation(.05f,
                atlas.findRegion("planeRed1"),
                atlas.findRegion("planeRed2"),
                atlas.findRegion("planeRed3")
        );
        plane.setPlayMode(Animation.PlayMode.LOOP);

        resetScene();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        fpsLogger.log();
        updateScene();
        drawScene();
    }

    private void updateScene() {

        if (planePosition.y < terrainBelow.getRegionHeight() - 35 ||
                planePosition.y + 73 > 480 - terrainAbove.getRegionHeight() + 35) {
            if (gameState != GameState.GAME_OVER) {
                gameState = GameState.GAME_OVER;
            }
        }

        if (Gdx.input.justTouched()) {
            if (gameState == GameState.INIT) {
                gameState = GameState.ACTION;
                return;
            }
            if (gameState == GameState.GAME_OVER) {
                gameState = GameState.INIT;
                resetScene();
                return;
            }
            touchPosition.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPosition);
            // touch position done

            tmpVector.set(planePosition.x, planePosition.y);
            tmpVector.sub(touchPosition.x, touchPosition.y).nor();
            planeVelocity.mulAdd(tmpVector,
                    TOUCH_IMPULSE - MathUtils.clamp(Vector2.dst(touchPosition.x, touchPosition.y,
                            planePosition.x, planePosition.y), 0, TOUCH_IMPULSE));
            tapDrawTime = TAP_DRAW_TIME_MAX;
        }

        if (gameState != GameState.ACTION) {
            return;
        }
        final float deltaTime = Gdx.graphics.getDeltaTime();

        tapDrawTime -= deltaTime;

        planeAnimTime += deltaTime;

        planeVelocity.scl(damping);
        planeVelocity.add(gravity);
        planeVelocity.add(scrollVelocity);
        planePosition.mulAdd(planeVelocity, deltaTime);

        terrainOffset -= planePosition.x - planeDefaultPosition.x;

        if (-terrainOffset > terrainBelow.getRegionWidth()) {
            terrainOffset = 0;
        }

        if (terrainOffset > 0) {
            terrainOffset -= terrainBelow.getRegionWidth();
        }

        planeRect.set(planePosition.x + 16, planePosition.y, 50, 73);
        for (Vector2 vec : pillars) {
            float deltaPosition = planePosition.x - planeDefaultPosition.x;
            vec.x -= deltaPosition;
            if (vec.x + pillarUp.getRegionWidth() < -10) {
                pillars.removeValue(vec, false);
            }
            if (vec.y == 1) {
                obstacleRect.set(vec.x + 10, 0, pillarUp.getRegionWidth() - 20, pillarUp.getRegionHeight() - 10);
            } else {
                obstacleRect.set(vec.x ) // Page No. 70
            }
        }
        if (lastPillarPosition.x < 400) {
            addPillar();
        }
    }

    private void drawScene() {
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.disableBlending();
        batch.draw(bgRegion, 0, 0);
        batch.enableBlending();
        for (Vector2 vec : pillars) {
            if (vec.y == 1) {
                batch.draw(pillarUp, vec.x, 0);
            } else {
                batch.draw(pillarDown, vec.x, 480 - pillarDown.getRegionHeight());
            }
        }

        batch.draw(terrainBelow, terrainOffset, 0);
        batch.draw(terrainBelow, terrainOffset + terrainBelow.getRegionWidth(), 0);
        batch.draw(terrainAbove, terrainOffset, 480 - terrainAbove.getRegionHeight());
        batch.draw(terrainAbove, terrainOffset + terrainBelow.getRegionWidth(), 480 - terrainAbove.getRegionHeight());
        batch.draw(plane.getKeyFrame(planeAnimTime), planePosition.x, planePosition.y);

        if (tapDrawTime > 0) {
            batch.draw(tapIndicator, touchPosition.x - 29.5f, touchPosition.y - 29.5f);
        }

        if (gameState == GameState.INIT) {
            batch.draw(tap1, planePosition.x, planePosition.y - 80);
        } else if (gameState == GameState.GAME_OVER) {
            batch.draw(gameOver, 400 - 206, 240 - 80);
        }

        batch.end();
    }

    private void resetScene() {
        scrollVelocity.set(4, 0);
        terrainOffset = 0;
        planeAnimTime = 0;
        planeVelocity.set(10, 0);
        gravity.set(0, -4);
        planeDefaultPosition.set(400 - 88 / 2, 240 - 73 / 2);
        planePosition.set(planeDefaultPosition.x, planeDefaultPosition.y);
    }

    @Override
    public void dispose() {
        batch.dispose();
        atlas.dispose();
        gameOver.dispose();
    }

    private void addPillar() {
        Vector2 pillarPosition = new Vector2();
        if (pillars.size == 0) {
            pillarPosition.x = (float) (800 + Math.random() * 600);
        } else {
            pillarPosition.x = lastPillarPosition.x + (float) (600 + Math.random() * 600);
        }

        pillarPosition.y = MathUtils.randomBoolean() ? 1 : -1;
        lastPillarPosition = pillarPosition;
        pillars.add(pillarPosition);
    }

    enum GameState {
        INIT, ACTION, GAME_OVER
    }
}
