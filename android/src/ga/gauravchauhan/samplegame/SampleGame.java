package ga.gauravchauhan.samplegame;

import com.badlogic.gdx.Game;

public class SampleGame extends Game {
    @Override
    public void create() {
        setScreen(new ThrustCopterScene());
    }
}
