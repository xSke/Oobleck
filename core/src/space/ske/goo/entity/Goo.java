package space.ske.goo.entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import space.ske.goo.GooGame;
import space.ske.goo.util.SpringingContext2D;

public class Goo extends Entity {

    private final Fixture            shape;
    private       SpringingContext2D posSpring;
    private       GooType            type;
    private       float              time;

    public Goo(GooType type) {
        this(type, 0.1f);
    }

    public Goo(GooType type, float radius) {
        this.type = type;
        shape = createShape(radius);
        Filter f = new Filter();
        f.categoryBits = 0x02;
        f.maskBits = (short) type.mask;
        shape.setFilterData(f);
        shape.setFriction(100);

        posSpring = new SpringingContext2D(1, 8);
        body.setLinearDamping(1);
    }

    @Override
    public void update(final float deltaTime) {
        super.update(deltaTime);

        if (time > 0.5f && type == GooType.PLAYER_SPRAY) {
            Filter f = shape.getFilterData();
            f.maskBits |= 0x4;
            shape.setFilterData(f);
        }

        time += deltaTime;

        if (type != GooType.ENEMY_SHOOT) {
            final Vector2 pos = getBody().getPosition().cpy();
            GooGame.i.getWorld().QueryAABB(new QueryCallback() {
                @Override
                public boolean reportFixture(Fixture fixture) {
                    if (fixture.getBody().getUserData() instanceof Goo && fixture.getBody() != body) {
                        Goo other = (Goo) fixture.getBody().getUserData();

                        Vector2 delta = other.body.getPosition().sub(pos).nor().cpy();
                        float dst = pos.dst(other.getBody().getPosition());

                        Vector2 vel = getBody().getLinearVelocity();

                        float dstFrac = 1 / dst;
                        vel.mulAdd(delta, dstFrac * deltaTime * 20);

                        getBody().setLinearVelocity(vel);
                    }
                    return false;
                }
            }, pos.x - 2, pos.y - 2, pos.x + 2, pos.y + 2);
        }
    }

    public float getTime() {
        return time;
    }

    @Override
    public void draw(ShapeRenderer sr) {
        super.draw(sr);

        Vector2 pos = body.getPosition();
        sr.circle(pos.x, pos.y, 0.3f, 8);
    }

    @Override
    public void collide(Fixture f) {
        super.collide(f);

        if (!type.canPlayerPickUp && type != GooType.LAVA) {
            if (!(f.getBody().getUserData() instanceof Goo) || ((Goo) f.getBody().getUserData()).getType() != type) {
                if (type != GooType.ENEMY_SHOOT || !(f.getBody().getUserData() instanceof Shooter)) {
                    destroy();
                }
            }
        }
    }

    public GooType getType() {
        return type;
    }

    public enum GooType {
        PLAYER_SPRAY(0x0b, true, true),
        PLAYER_HURT(0x0f, true, false),
        DECORATIVE(0x01, false, false),
        ENEMY_SHOOT(0x05, false, false),
        LAVA(0x07, false, true);

        private int     mask;
        private boolean canPlayerPickUp;
        private boolean canHurt;

        GooType(int mask, boolean canPlayerPickUp, boolean canHurt) {
            this.mask = mask;
            this.canPlayerPickUp = canPlayerPickUp;
            this.canHurt = canHurt;
        }

        public boolean isCanPlayerPickUp() {
            return canPlayerPickUp;
        }

        public boolean isCanHurtEnemy() {
            return canHurt;
        }
    }
}
