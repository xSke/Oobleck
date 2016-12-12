package space.ske.goo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import space.ske.goo.util.SpringingContext1D;

public class UI {
    private ShapeRenderer sr = new ShapeRenderer();
    private GooRenderer gr;

    private SpringingContext1D gooLevel = new SpringingContext1D(0.6f, 4);

    public UI(GooRenderer gr) {
        this.gr = gr;
    }

    public void update(float delta) {
        gooLevel.update(delta);
    }

    public void draw() {
        float maxGoo = 100;
        float gooFrac = gooLevel.value / maxGoo;

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();


        float fullBarHeight = h - 96 * 2;
        float actualBarHeight = fullBarHeight * gooFrac;
        float border = 6;

        gr.begin(210, 90, 90);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.rect(w - 96 - border * 2, 96 - border * 2, 24 + border * 4, fullBarHeight + border * 4);
        sr.end();
        gr.end(0, 1.5f);

        gr.begin(210, 100, 15);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.rect(w - 96 - border, 96 - border, 24 + border * 2, fullBarHeight + border * 2);
        sr.end();
        gr.end(0, 1.5f);

        gr.begin(210, 90, 90);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.rect(w - 96, 96, 24, actualBarHeight);
        sr.end();
        gr.end(0, 1.5f);
    }

    public void setGooLevel(float level) {
        gooLevel.target = level;
    }
}
