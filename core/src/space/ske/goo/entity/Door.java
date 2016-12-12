package space.ske.goo.entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import space.ske.goo.GooGame;
import space.ske.goo.util.SpringingContext1D;

public class Door extends Entity {
    private SpringingContext1D h = new SpringingContext1D(1, 4);

    public Door() {
        createRect(1, 3);
        body.setType(BodyDef.BodyType.StaticBody);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        boolean isOpen = true;
        for (Entity entity : GooGame.i.getEntities()) {
            if (entity instanceof Enemy || entity instanceof Boss) {
                isOpen = false;
            }
        }

        h.target = isOpen ? 0 : 3;
        body.setActive(!isOpen);

        h.update(deltaTime);
    }

    @Override
    public void draw(ShapeRenderer sr) {
        super.draw(sr);

        Vector2 pos = body.getPosition();
        sr.rect(pos.x - 0.5f, pos.y - 1.5f, 1, h.value);
    }
}
