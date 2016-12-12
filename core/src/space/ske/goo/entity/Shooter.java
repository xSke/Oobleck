package space.ske.goo.entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import space.ske.goo.GooGame;

public class Shooter extends Enemy {
    private float timer;

    public Shooter() {
        super(1.4f, 1.4f, 0x07);
        timer = MathUtils.random(1.5f);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        timer += deltaTime;

        if (timer > 1.5f) {
            timer -= 1.5f;

            shoot();
        } else if (timer > 1.3f) {
            shear.target = 0.2f;
        } else if (timer > 1.1f) {
            shear.target = -0.2f;
        } else {
            shear.target = 0;
        }
    }

    private void shoot() {
        Vector2 playerPos = GooGame.i.getPlayer().body.getPosition();
        Vector2 thisPos = body.getPosition();

        Vector2 delta = playerPos.sub(thisPos);

        Goo gg = new Goo(Goo.GooType.ENEMY_SHOOT);
        gg.z = 1;
        gg.getBody().setTransform(thisPos, 0);

        float angle = delta.angle();
        if (angle > 180) angle -= 360;
        angle = MathUtils.clamp(angle, 15, 165);
        gg.getBody().setLinearVelocity(new Vector2(30, 0).rotate(angle));
        GooGame.i.getEntities().add(gg);
    }
}
