package ga.gauravchauhan.samplegame;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * It is the pickup
 */
public class Pickup {
    public static final int STAR = 0x1;
    public static final int SHIELD = 0x2;
    public static final int FUEL = 0x4;
    TextureRegion pickupTexture;
    Vector2 pickupPosition = new Vector2();
    int pickupType;
    int pickupValue;
    Sound pickupSound;

    public Pickup(int type, AssetManager manager) {
        TextureAtlas atlas = manager.get("ThrustCopter.pack", TextureAtlas.class);
        pickupType = type;
        switch (type) {
            case STAR:
                pickupTexture = atlas.findRegion("star_pickup");
                pickupValue = 5;
                pickupSound = manager.get("sounds/star.ogg");
                break;
            case SHIELD:
                pickupTexture = atlas.findRegion("shield_pickup");
                pickupValue = 15;
                pickupSound = manager.get("sounds/shield.ogg");
                break;
            case FUEL:
                pickupTexture = atlas.findRegion("fuel_pickup");
                pickupValue = 100;
                pickupSound = manager.get("sounds/fuel.ogg");
                break;
        }
    }
}
