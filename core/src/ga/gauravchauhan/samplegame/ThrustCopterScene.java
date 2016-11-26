package ga.gauravchauhan.samplegame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

@SuppressWarnings("FieldCanBeLocal")
public class ThrustCopterScene extends ScreenAdapter {

    // region propterties
    private final static Vector2 damping = new Vector2(.99f, .99f);
    private static final float TAP_DRAW_TIME_MAX = 1f;
    private static final float TOUCH_IMPULSE = 500;
    private static final int METEOR_SPEED = 50;
    BitmapFont font;

    ParticleEffect explosion;
    ParticleEffect smoke;
    Music music;

    AssetManager manager;
    OrthographicCamera camera;
    TextureAtlas atlas;
    SpriteBatch batch;
    ThrustCopter game;
    Animation shield;
    Animation plane;

    float nextMeteorIn;
    float terrainOffset;
    float tapDrawTime;
    float planeAnimTime;
    boolean meteorInScene;

    Vector2 planeVelocity = new Vector2();
    Vector2 planePosition = new Vector2();
    Vector2 planeDefaultPosition = new Vector2();
    Vector2 gravity = new Vector2();
    Vector2 tmpVector = new Vector2();
    Vector2 meteorPosition = new Vector2();
    Vector2 meteorVelocity = new Vector2();
    Vector2 scrollVelocity = new Vector2();
    Vector2 lastPillarPosition = new Vector2();
    // Actually not a vector, it is a time tuple (3).
    Vector3 pickupTiming = new Vector3();
    Vector3 touchPosition = new Vector3();

    Array<Vector2> pillars = new Array<Vector2>();
    TextureRegion bgRegion;
    TextureRegion terrainBelow;
    TextureRegion terrainAbove;
    TextureRegion tapIndicator;
    TextureRegion tap1;
    TextureRegion pillarDown;
    Texture gameOver; this is not good on page 92//////
    TextureRegion pillarUp;
    TextureRegion selectedMeteorTexture;

    Array<TextureAtlas.AtlasRegion> meteorTextures = new Array<TextureAtlas.AtlasRegion>();
    GameState gameState = GameState.INIT;
    Rectangle planeRect = new Rectangle();

    Rectangle obstacleRect = new Rectangle();
    Sound spawnSound;
    Sound tapSound;

    Sound crashSound;
    int starCount;
    int fuelCount;
    @SuppressWarnings("FieldCanBeLocal")
    private float deltaPosition;
    private Pickup tempPickup;
    private Array<Pickup> pickUpInScene = new Array<Pickup>();
    private int shieldCount;
    private int fuelPercentage;
    private Texture fuelIndicator;
    private int score;
    // endregion

    public ThrustCopterScene(ThrustCopter thrustCopter) {
        game = thrustCopter;
        batch = game.batch;
        atlas = game.atlas;
        camera = game.camera;
        manager = game.manager;
        font = game.font;

        fuelIndicator = manager.get("life.png", Texture.class);
        tap1 = atlas.findRegion("tap1");
        tapIndicator = atlas.findRegion("tap2");
        bgRegion = atlas.findRegion("background");
        pillarUp = atlas.findRegion("rockGrassUp");
        pillarDown = atlas.findRegion("rockGrassDown");
        terrainBelow = atlas.findRegion("groundGrass");

        terrainAbove = new TextureRegion(terrainBelow);
        terrainAbove.flip(true, true);

        plane = new Animation(.05f,
                atlas.findRegion("planeRed1"),
                atlas.findRegion("planeRed2"),
                atlas.findRegion("planeRed3")
        );
        plane.setPlayMode(Animation.PlayMode.LOOP);

        shield = new Animation(.1f,
                atlas.findRegion("shield1"), atlas.findRegion("shield2"),
                atlas.findRegion("shield3"), atlas.findRegion("shield2")
        );
        shield.setPlayMode(Animation.PlayMode.LOOP);

        meteorTextures.add(atlas.findRegion("meteorBrown_med2"));
        meteorTextures.add(atlas.findRegion("meteorBrown_med1"));
        meteorTextures.add(atlas.findRegion("meteorBrown_small1"));
        meteorTextures.add(atlas.findRegion("meteorBrown_small2"));
        meteorTextures.add(atlas.findRegion("meteorBrown_tiny1"));
        meteorTextures.add(atlas.findRegion("meteorBrown_tiny2"));

        music = manager.get("sounds/journey.mp3", Music.class);
        music.setLooping(true);
        music.play();

        tapSound = manager.get("sounds/pop.ogg", Sound.class);
        crashSound = manager.get("sounds/crash.ogg", Sound.class);
        spawnSound = manager.get("sounds/alarm.ogg", Sound.class);

        smoke = manager.get("Smoke", ParticleEffect.class);
        explosion = manager.get("Explosion", ParticleEffect.class);
        resetScene();
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        updateScene(delta);
        drawScene();
    }

    private void resetScene() {
        meteorInScene = false;
        nextMeteorIn = (float) Math.random() * 5;
        pickupTiming.x = (float) (1 + Math.random() * 2);
        pickupTiming.y = (float) (3 + Math.random() * 2);
        pickupTiming.z = (float) (1 + Math.random() * 3);
        terrainOffset = 0;
        planeAnimTime = 0;
        tapDrawTime = 0;
        starCount = 0;
        score = 0;
        shieldCount = 15;
        fuelCount = 100;
        fuelPercentage = 114;
        planeVelocity.set(100, 0);
        scrollVelocity.set(5, 0);
        gravity.set(0, -3);
        planeDefaultPosition.set(250 - 88 / 2, 240 - 73 / 2);
        planePosition.set(planeDefaultPosition.x, planeDefaultPosition.y);
        pillars.clear();
        pickUpInScene.clear();
        addPillar();
        smoke.setPosition(planePosition.x + 20, planePosition.y + 20);
    }

    private void updateScene(float deltaTime) {
        if (Gdx.input.justTouched()) {
            tapSound.play();
            if (gameState == GameState.INIT) {
                gameState = GameState.ACTION;
                return;
            }
            if (gameState == GameState.GAME_OVER) {
                gameState = GameState.INIT;
                resetScene();
                return;
            }
            if (fuelCount > 0) {
                touchPosition.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(touchPosition);
                tmpVector.set(planePosition.x, planePosition.y);
                tmpVector.sub(touchPosition.x, touchPosition.y).nor();
                planeVelocity.mulAdd(tmpVector,
                        TOUCH_IMPULSE - MathUtils.clamp(Vector2.dst(touchPosition.x, touchPosition.y,
                                planePosition.x, planePosition.y), 0, TOUCH_IMPULSE));
                tapDrawTime = TAP_DRAW_TIME_MAX;
            }
        }

        if (gameState != GameState.ACTION) {
            return;
        }
        smoke.setPosition(planePosition.x + 20, planePosition.y + 30);

        planeAnimTime += deltaTime;
        planeVelocity.scl(damping);
        planeVelocity.add(gravity);
        planeVelocity.add(scrollVelocity);
        planePosition.mulAdd(planeVelocity, deltaTime);
        deltaPosition = planePosition.x - planeDefaultPosition.x;
        terrainOffset -= deltaPosition;
        planePosition.x = planeDefaultPosition.x;
        if (-terrainOffset > terrainBelow.getRegionWidth()) {
            terrainOffset = 0;
        }
        if (terrainOffset > 0) {
            terrainOffset -= terrainBelow.getRegionWidth();
        }
        planeRect.set(planePosition.x + 16, planePosition.y, 50, 73);

        if (meteorInScene) {
            meteorPosition.mulAdd(meteorVelocity, deltaTime);
            meteorPosition.x -= deltaPosition;
            if (meteorPosition.x < -10) {
                meteorInScene = false;
            }
            obstacleRect.set(meteorPosition.x + 2, meteorPosition.y + 2,
                    selectedMeteorTexture.getRegionWidth() - 4,
                    selectedMeteorTexture.getRegionHeight() - 4);
            if (planeRect.overlaps(obstacleRect)) {
                endGame();
            }

        }
        for (Vector2 vec : pillars) {
            vec.x -= deltaPosition;
            if (vec.x + pillarUp.getRegionWidth() < -10) {
                pillars.removeValue(vec, false);
            }
            if (vec.y == 1) {
                obstacleRect.set(vec.x + 10, 0, pillarUp.getRegionWidth() - 20,
                        pillarUp.getRegionHeight() - 10);
            } else {
                obstacleRect.set(vec.x + 10, 480 - pillarDown.getRegionHeight() + 10,
                        pillarUp.getRegionWidth() - 20, pillarUp.getRegionHeight());
            }

            if (planeRect.overlaps(obstacleRect)) {
                endGame();
            }
        }
        for (Pickup pickup : pickUpInScene) {
            pickup.pickupPosition.x -= deltaPosition;
            if (pickup.pickupPosition.x + pickup.pickupTexture.getRegionWidth() < -10) {
                pickUpInScene.removeValue(pickup, false);
            }
            obstacleRect.set(pickup.pickupPosition.x, pickup.pickupPosition.y,
                    pickup.pickupTexture.getRegionWidth(), pickup.pickupTexture.getRegionHeight());
            if (planeRect.overlaps(obstacleRect)) {
                pickIt(pickup);
            }
        }
        if (lastPillarPosition.x < 400) {
            addPillar();
        }
        if (planePosition.y < terrainBelow.getRegionHeight() - 35 ||
                planePosition.y + 73 > 480 - terrainAbove.getRegionHeight() + 35) {
            endGame();
        }

        tapDrawTime -= deltaTime;
        nextMeteorIn -= deltaTime;
        if (nextMeteorIn <= 0) {
            launchMeteor();
        }
        checkAndCreatePickup(deltaTime);
        fuelCount -= 6 * deltaTime;
        fuelPercentage = 114 * fuelCount / 100;
        shieldCount -= deltaTime;
        score += deltaTime;
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

        if (tapDrawTime > 0 && gameState == GameState.ACTION) {
            batch.draw(tapIndicator, touchPosition.x - 29.5f, touchPosition.y - 29.5f);
        }
        if (gameState == GameState.INIT) {
            batch.draw(tap1, planePosition.x, planePosition.y - 80);
        }
        for (Pickup pickup : pickUpInScene) {
            batch.draw(pickup.pickupTexture, pickup.pickupPosition.x, pickup.pickupPosition.y);
        }
        smoke.draw(batch);
        batch.draw(plane.getKeyFrame(planeAnimTime), planePosition.x, planePosition.y);
        if (shieldCount > 0) {
            batch.draw(shield.getKeyFrame(planeAnimTime), planePosition.x - 20, planePosition.y);
        }
        if (meteorInScene) {
            batch.draw(selectedMeteorTexture, meteorPosition.x, meteorPosition.y);
        }
        font.draw(batch, "" + (starCount + score), 700, 450);
        batch.setColor(Color.BLACK);
        batch.draw(fuelIndicator, 10, 350);
        batch.setColor(Color.WHITE);
        batch.draw(fuelIndicator, 10, 350, 0, 0, fuelPercentage, 119);
        if (gameState == GameState.GAME_OVER) {
            explosion.draw(batch);
        }
        batch.end();
    }

    private void endGame() {
        if (gameState != GameState.GAME_OVER) {
            gameState = GameState.GAME_OVER;
        }
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

    private void launchMeteor() {
        nextMeteorIn = (float) (1.5 + Math.random() * 5);
        if (meteorInScene) {
            return;
        }
        spawnSound.play();
        meteorInScene = true;
        int id = (int) (Math.random() * meteorTextures.size);
        selectedMeteorTexture = meteorTextures.get(id);
        meteorPosition.x = 810;
        meteorPosition.y = (float) (80 + Math.random() * 320);
        Vector2 destination = new Vector2();
        destination.x = -10;
        destination.y = (float) (80 + Math.random() * 320);
        destination.sub(meteorPosition).nor();
        meteorVelocity.mulAdd(destination, METEOR_SPEED);
    }

    private void checkAndCreatePickup(float delta) {
        pickupTiming.sub(delta);
        if (pickupTiming.x <= 0) {
            pickupTiming.x = (float) (.5 + Math.random() * .5);
            if (addPickup(Pickup.STAR))
                pickupTiming.x = (float) (1 + Math.random() * 2);
        }
        if (pickupTiming.y <= 0) {
            pickupTiming.y = (float) (.5 + Math.random() * .5);
            if (addPickup(Pickup.FUEL))
                pickupTiming.y = (float) (3 + Math.random() * 2);
        }
        if (pickupTiming.z <= 0) {
            pickupTiming.z = (float) (.5 + Math.random() * .5);
            if (addPickup(Pickup.SHIELD))
                pickupTiming.z = (float) (10 + Math.random() * 3);
        }
    }

    private boolean addPickup(int pickupType) {
        Vector2 randomPos = new Vector2(820, (float) (80 + Math.random() * 320));
        for (Vector2 vec : pillars) {
            if (vec.y == 1) {
                obstacleRect.set(vec.x, 0, pillarUp.getRegionWidth(),
                        pillarUp.getRegionHeight());
            } else {
                obstacleRect.set(vec.x, 0, pillarDown.getRegionWidth(),
                        pillarDown.getRegionHeight());
            }

            if (obstacleRect.contains(randomPos)) {
                return false;
            }
        }
        tempPickup = new Pickup(pickupType, manager);
        tempPickup.pickupPosition.set(randomPos);
        pickUpInScene.add(tempPickup);
        return true;
    }

    private void pickIt(Pickup pickup) {
        pickup.pickupSound.play();
        switch (pickup.pickupType) {
            case Pickup.STAR:
                starCount += pickup.pickupValue;
                break;
            case Pickup.SHIELD:
                shieldCount = pickup.pickupValue;
                break;
            case Pickup.FUEL:
                fuelCount = pickup.pickupValue;
                break;
        }
        pickUpInScene.removeValue(pickup, false);
    }

    @Override
    public void dispose() {
        gameOver.dispose();
        tapSound.dispose();
        crashSound.dispose();
        spawnSound.dispose();
        music.dispose();
        pillars.clear();
        smoke.dispose();
        explosion.dispose();
        meteorTextures.clear();
    }

    enum GameState {
        INIT, ACTION, GAME_OVER
    }
}
