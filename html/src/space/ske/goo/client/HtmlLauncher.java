package space.ske.goo.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import space.ske.goo.GooGame;

public class HtmlLauncher extends GwtApplication {

    @Override
    public GwtApplicationConfiguration getConfig() {
        GwtApplicationConfiguration gwtApplicationConfiguration = new GwtApplicationConfiguration(960, 640);
        gwtApplicationConfiguration.antialiasing = true;
        return gwtApplicationConfiguration;
    }

    @Override
    public ApplicationListener createApplicationListener() {
        return new GooGame();
    }
}