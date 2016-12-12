package space.ske.goo.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import space.ske.goo.Assets;
import space.ske.goo.GooGame;

public class Enemy extends BoxShapedEntity {
    private boolean isDying;

    public Enemy(float w, float h, int mask) {
        super(w, h, 0x08, mask);
        z = 1;
        size.target.set(size.value.set(1, 1));
    }

    @Override
    public void update(float deltaTime) {
        if (isDying) {
            size.target.set(0, 0);
            size.damping = 1;

            if (size.value.len() < 0.05f) {
                die();
            }
        }

        super.update(deltaTime);
    }

    private void die() {
        destroy();
    }

    @Override
    public void collide(Entity e) {
        super.collide(e);

        if (e instanceof Goo && ((Goo) e).getType().isCanHurtEnemy()) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    killSelf();
                }
            });
        }
    }

    private void killSelf() {
        if (isDying) return;

        Assets.enemyDeath.play(MathUtils.random(0.9f, 1.1f), MathUtils.random(0.9f, 1.1f), MathUtils.random(-0.1f, 0.1f));
        GooGame.i.shake(0.2f);

        isDying = true;
        size.velocity.set(20, 20);

        spray(1, 30, 23);

        body.setActive(false);
    }
}
