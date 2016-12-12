package space.ske.goo.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import space.ske.goo.Assets;
import space.ske.goo.GooGame;
import space.ske.goo.util.SpringingContext1D;
import space.ske.goo.util.SpringingContext2D;


public class Boss1 extends Boss {
    private final Fixture bottomSensor;
    private final Fixture mainBody;
    private BossState state = BossState.ASLEEP;
    private float timer;
    private int   counter;
    private int hp = 3;

    private SpringingContext2D size     = new SpringingContext2D(0.3f, 2);
    private SpringingContext2D movement = new SpringingContext2D(0.5f, 1);
    private SpringingContext1D rotation = new SpringingContext1D(0.9f, 8);

    public Boss1() {
        bottomSensor = createRect(2.9f, 0.1f, 0, -1.5f);
        Filter bottomFilter = new Filter();
        bottomFilter.categoryBits = 0x08;
        bottomFilter.maskBits = 0x05;
        bottomSensor.setFilterData(bottomFilter);
        bottomSensor.setSensor(true);

        mainBody = createRect(3, 3);
        Filter bodyFilter = new Filter();
        bodyFilter.categoryBits = 0x08;
        bodyFilter.maskBits = 0x07;
        mainBody.setFilterData(bodyFilter);

        z = 1;

        body.setFixedRotation(true);

        size.target.set(size.value.set(3, 3));
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        GooGame.i.getBossMusic().target = (state == BossState.ASLEEP || state == BossState.DEAD) ? 0 : 0.6f;

        movement.frequency = 2;
        if (state == BossState.ASLEEP) {
            hp = 3;
            counter = 0;
            movement.frequency = 0.6f;
            movement.damping = 0.7f;
            movement.value.set(body.getPosition());
            movement.target.set(15.5f, 2.5f);
            movement.update(deltaTime);
            body.setLinearVelocity(movement.velocity);

        } else if (state == BossState.RISE) {
            timer += deltaTime;

            getBody().setLinearVelocity(0, 5);

            GooGame.i.shake(timer * 0.05f);

            if (timer > 1.5) {
                state = BossState.FOLLOW;
                getBody().setLinearVelocity(0, 0);
                timer = 0;
            }
        } else if (state == BossState.FOLLOW) {
            movement.frequency = 1f;
            movement.damping = 0.5f;
            movement.value.set(getBody().getPosition().x, getBody().getPosition().y);
            movement.target.set(GooGame.i.getPlayer().body.getPosition().x, 10);
            movement.update(deltaTime);

            getBody().setLinearVelocity(movement.velocity);

            timer += deltaTime;
            if (timer > 1.5f) {
                float lastTarget = rotation.target;
                rotation.target = 0.5f * ((timer % 0.2 < 0.1) ? -1 : 1);

                if (rotation.target != lastTarget && timer < 2f)
                    Assets.goo3.play(MathUtils.random(0.7f, 0.9f), MathUtils.random(0.97f, 1.03f), MathUtils.random(-0.1f, 0.1f));
            }
            if (timer > 2f) {
                rotation.target = 0;
            }
            if (timer > 2.2f) {
                state = BossState.SMASH;
                size.velocity.set(-30, 30);
            }

            rotation.update(deltaTime);
        } else if (state == BossState.SMASH) {
            getBody().setLinearVelocity(0, -80);

            if (anythingTouches(bottomSensor)) {
                Array<Fixture> hits = GooGame.i.getCollisionTracker().getFixtureContacts().get(bottomSensor);
                counter++;
                Assets.goo5.play();
                GooGame.i.shake(0.4f);

                for (Fixture hit : hits){
                    if (hit.getBody().getUserData() instanceof Player) {
                        ((Player) hit.getBody().getUserData()).squish();
                    } else {
                        if (counter >= 3) {
                            state = BossState.WEAK;
                        } else {
                            state = BossState.RISE;
                        }

                        size.velocity.set(30, -30);

                        timer = 0;
                    }
                }
            }
        } else if (state == BossState.WEAK) {
            timer += deltaTime;

            if (timer % 0.3f < (timer - deltaTime) % 0.3f) {
                size.velocity.set(10, 10);
                Assets.goo3.play(0.7f);
            }

            if (timer > 2) {
                state = BossState.SWISH;
                timer = 0;
            }
        } else if (state == BossState.SWISH) {
            counter = 0;
            timer += deltaTime;

            if (timer > 1) {
                movement.frequency = 0.7f;
                movement.damping = 0.3f;
                movement.target.set((timer % 2 < 1) ? 5 : 25, 3.8f);
                movement.value.set(getBody().getPosition());
                movement.update(deltaTime);
                body.setLinearVelocity(movement.velocity);
            }

            if (timer > 7.5f) {
                state = BossState.RISE;
            }
        } else if (state == BossState.DEAD) {
            timer += deltaTime;

            size.target.set(5, 5);
            if (timer >= 0.5f) {
                size.target.set(0, 0);
            }

            size.frequency = 0.5f;
            size.damping = 0.7f;

            if (size.value.len() < 0.1) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        spray(1, 200, 40);
                    }
                });
                destroy();
                Assets.enemyDeath.play();
                Assets.playerHurt.play();
                Assets.yay.play();
                GooGame.i.shake(0.7f);
            }
        }

        if (GooGame.i.getPlayer().isDead()) state = BossState.ASLEEP;

        size.update(deltaTime);
    }

    @Override
    public void collide(final Entity e) {
        super.collide(e);

        if (e instanceof Goo && ((Goo) e).getType().isCanHurtEnemy()) {
            if (state == BossState.ASLEEP && !GooGame.i.getPlayer().isDead()) {
                Assets.playerHurt.play();
                state = BossState.RISE;
                size.velocity.set(60, -60);
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        spray(1, 70, 50);
                    }
                });
            } else if (state == BossState.WEAK) {
                Assets.playerHurt.play();
                state = BossState.SWISH;
                size.velocity.set(40, -40);
                timer = 0;
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        spray(1, 70, 50);
                    }
                });
                hp -= 1;
                GooGame.i.shake(0.4f);
                if (hp == 0) {
                    state = BossState.DEAD;
                    timer = 0;
                    Assets.playerDeath.play(1.2f);
                }
            }
        }

        if (e instanceof Player) {
            if (state == BossState.SWISH) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        ((Player) e).squish();
                    }
                });
            }
        }
    }

    @Override
    public void draw(ShapeRenderer sr) {
        sr.setColor(Color.WHITE);
        sr.translate(body.getPosition().x, body.getPosition().y, 0);

        Vector2 s = size.value;
        sr.rotate(0, 0, 1, rotation.value * MathUtils.radDeg);
        sr.triangle(-s.x / 2f, -s.y / 2f, s.x / 2f, -s.y / 2f, -s.x / 2f * 1.5f, s.y / 2f);
        sr.triangle(s.x / 2f, -s.y / 2f, -s.x / 2f * 1.5f, s.y / 2f, s.x / 2f * 1.5f, s.y / 2f);

        sr.identity();
    }

    public enum BossState {
        RISE,
        FOLLOW,
        SMASH,
        WEAK,
        ASLEEP,
        SWISH,
        DEAD
    }
}
