package space.ske.goo.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.BodyDef;
import space.ske.goo.GooGame;
import space.ske.goo.Level;

public class NextLevelSensor extends Entity {
    private boolean hit;

    public NextLevelSensor() {
        Level l = GooGame.i.getLevel();

        body.setType(BodyDef.BodyType.StaticBody);
        createRect(1, l.getHeight()).setSensor(true);
        body.setTransform(l.getWidth() + 1.9f, l.getHeight() / 2f, 0);
    }

    @Override
    public void collide(Entity e) {
        super.collide(e);
        if (e instanceof Player) {
            if (hit) return;
            hit = true;

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    GooGame.i.switchLevel(GooGame.i.getLevelNum() + 1);
                }
            });
        }
    }
}
