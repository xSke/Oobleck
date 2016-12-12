package space.ske.goo.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import space.ske.goo.Assets;
import space.ske.goo.GooGame;
import space.ske.goo.util.SpringingContext1D;
import space.ske.goo.util.SpringingContext2D;

public class Player extends BoxShapedEntity {
    public static final float SPRAY_TIME       = 0.2f;
    public static final float SPRAY_INTERVAL   = 0.01f;
    public static final float JUMP_FORCE       = 20f;
    public static final float SPRAY_PLAYER_ACC = 100f;
    public static final float SPRAY_MAX_SPEED  = 20f;
    public static final float SPRAY_GOO_FORCE  = -10f;
    public static final float MOVE_SPEED       = 10;
    public static final float MAX_GOO          = 100;

    private boolean isJumping;
    private boolean wasGrounded;
    private boolean isSpraying;

    private float sprayTimer;

    private float gooLevel = MAX_GOO;

    private boolean isDying;
    private float   deadTimer;

    private float airTimer;

    private SpringingContext1D gooSound = new SpringingContext1D(1, 4);

    public Player() {
        super(1.5f, 1.5f, 0x04, 0x0b);
        body.setFixedRotation(true);

        body.setTransform(10, 10, 0);

        size = new SpringingContext2D(0.2f, 4);
        size.value.set(size.target.set(1, 1));

        shear = new SpringingContext1D(0.2f, 4);
    }

    @Override
    public void update(float deltaTime) {
        if (isDying) {
            size.target.set(0, 0);

            if (size.value.len() < 0.05f) {
                deadTimer += deltaTime;
            }

            body.setActive(false);
            body.setLinearVelocity(0, 0);
            if (deadTimer > 2f) {
                isDying = false;
                body.setActive(true);
                deadTimer = 0;
                gooLevel = MAX_GOO;

                for (Entity g : GooGame.i.getEntities()){
                    if (g instanceof Goo && ((Goo) g).getType().isCanPlayerPickUp()) g.destroy();
                }

                getBody().setTransform(GooGame.i.getSpawnPoint(), 0);
            } else {
                super.update(deltaTime);
                return;
            }
        }

        boolean touchBottom = anythingTouches(bottomSensor);
        boolean touchLeft = anythingTouches(leftSensor);
        boolean toughRight = anythingTouches(rightSensor);

        boolean grounded = touchBottom;

        float input = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) input--;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) input++;

        /*if (touchLeft && input < 0) {
            body.setLinearVelocity(0, -input * MOVE_SPEED);
        } else if (toughRight && input > 0) {
            body.setLinearVelocity(0, input * MOVE_SPEED);
        } else {*/
        body.setLinearVelocity(input * MOVE_SPEED, body.getLinearVelocity().y);
        //}

        if (input != 0) {
            size.target.set(1.1f, 0.9f);
            shear.target = input * SPRAY_TIME;
        } else {
            size.target.set(1f, 1f);
            shear.target = 0;
        }

        if (grounded && body.getLinearVelocity().y <= 0) {
            isJumping = false;
        }

        if (!grounded) {
            airTimer += deltaTime;
        } else {
            airTimer = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (!isJumping && (grounded || airTimer < 0.1f)) {
                handleJump();
                isJumping = true;
            } else {
                isSpraying = true;
                isJumping = false;
            }
        }


        if (grounded && !wasGrounded) {
            size.velocity.set(20, -20);
            Assets.playerLand.play(MathUtils.random(0.8f, 1.0f), MathUtils.random(0.9f, 1.1f), MathUtils.random(-0.1f, 0.1f));
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            if (isSpraying) {
                handleSpray(deltaTime, input);
            }
        } else {
            isSpraying = false;
        }

        wasGrounded = grounded;

        gooSound.target = (isSpraying && gooLevel > 0) ? 1 : 0;
        if (!isSpraying && input != 0 && grounded) {
            gooSound.target = Math.max(gooSound.target, 0.1f);
        }
        gooSound.update(deltaTime);
        Assets.gooSplatter.setVolume(gooSound.value);

        super.update(deltaTime);
    }

    private void handleSpray(float deltaTime, float input) {
        Vector2 pos = body.getPosition();

        sprayTimer += deltaTime;
        while (sprayTimer > SPRAY_INTERVAL) {
            sprayTimer -= 0.01f;

            if (gooLevel > 0) {
                GooGame.i.shake(0.02f);
                Goo g = new Goo(Goo.GooType.PLAYER_SPRAY);
                g.body.setTransform(pos.x + MathUtils.random(-0.4f, 0.4f), pos.y + MathUtils.random(-0.3f, 0.7f), 0);
                g.body.setLinearVelocity(input * -8, SPRAY_GOO_FORCE);
                GooGame.i.getEntities().add(g);

                float force = SPRAY_PLAYER_ACC;
                if (body.getLinearVelocity().y < 0)
                    force *= 3;

                if (body.getLinearVelocity().y < SPRAY_MAX_SPEED)
                    body.setLinearVelocity(body.getLinearVelocity().add(0, force * deltaTime));

                gooLevel -= 1;
            }
        }
    }

    private void handleJump() {
        size.velocity.set(-10, 10);

        body.setLinearVelocity(body.getLinearVelocity().x, JUMP_FORCE);
        Assets.playerJump.play(MathUtils.random(0.9f, 1), MathUtils.random(0.95f, 1.05f), MathUtils.random(-0.1f, 0.1f));
    }

    @Override
    public void collide(Entity e) {
        super.collide(e);

        if (e instanceof Enemy) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    hurt();
                }
            });
        } else if (e instanceof Goo) {
            Goo.GooType type = ((Goo) e).getType();
            if ((type == Goo.GooType.PLAYER_SPRAY || type == Goo.GooType.PLAYER_HURT) && ((Goo) e).getTime() > 0.5f) {
                e.destroy();
                gooLevel += 1;

                gooSound.velocity = 4;
            } else if (type == Goo.GooType.ENEMY_SHOOT) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        hurt();
                    }
                });
            }
        }
    }

    public void hurt() {
        Assets.playerHurt.play(MathUtils.random(0.9f, 1), MathUtils.random(0.95f, 1.05f), MathUtils.random(-0.1f, 0.1f));
        GooGame.i.shake(0.3f);
        for (int i = 0; i < 20; i++){
            Goo g = new Goo(Goo.GooType.PLAYER_HURT);

            Vector2 delta = new Vector2(1f, 0).rotate(MathUtils.random(0f, 360f));

            g.getBody().setTransform(body.getPosition().mulAdd(delta, 0.1f), 0);
            g.getBody().setLinearVelocity(delta.nor().scl(MathUtils.random(15f, 23f)));

            GooGame.i.getEntities().add(g);

            gooLevel -= 1;

            if (gooLevel <= 0) {
                gooLevel = 0;
                die();
            }
        }
    }

    public void squish() {
        die();
        size.velocity.set(30, -30);
    }

    private void die() {
        if (isDying) return;

        GooGame.i.shake(0.6f);

        for (int i = 0; i < 100; i++){
            Goo g = new Goo(Goo.GooType.DECORATIVE);

            Vector2 delta = new Vector2(1f, 0).rotate(MathUtils.random(0f, 360f));

            g.getBody().setTransform(body.getPosition().mulAdd(delta, 0.1f), 0);
            g.getBody().setLinearVelocity(delta.nor().scl(MathUtils.random(10f, 50f)));

            GooGame.i.getEntities().add(g);
        }

        Assets.playerDeath.play();

        isDying = true;
    }

    public float getGooLevel() {
        return gooLevel;
    }

    public boolean isDead() {
        return isDying;
    }
}
