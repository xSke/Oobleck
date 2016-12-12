package space.ske.goo.entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import space.ske.goo.GooGame;

public class Entity {
    Body body;
    public int z;

    public boolean shouldDestroy;
    private Goo.GooType g;

    public Entity() {
        body = GooGame.i.getWorld().createBody(new BodyDef());
        body.setType(BodyDef.BodyType.DynamicBody);
        body.setUserData(this);
    }

    public Fixture createShape(float radius) {
        CircleShape s = new CircleShape();
        s.setRadius(radius);
        Fixture f = body.createFixture(s, 1);
        s.dispose();
        return f;
    }

    public Fixture createRect(float w, float h) {
        return createRect(w, h, 0, 0);
    }

    public Fixture createRect(float w, float h, float x, float y) {
        PolygonShape s = new PolygonShape();
        s.setAsBox(w / 2f, h / 2f, new Vector2(x, y), 0);
        Fixture f = body.createFixture(s, 1);
        s.dispose();
        return f;
    }

    public void update(float deltaTime) {

    }

    public void draw(ShapeRenderer sr) {

    }

    public void collide(Fixture f) {

    }

    public void collide(Entity e) {

    }

    public void destroy() {
        shouldDestroy = true;
    }

    public Body getBody() {
        return body;
    }

    boolean anythingTouches(Fixture fixture) {
        return GooGame.i.getCollisionTracker().getFixtureContacts().containsKey(fixture);
    }

    public void spray(int z, int amnt, float speed) {
        for (int i = 0; i < amnt; i++) {
            Goo g = new Goo(Goo.GooType.DECORATIVE);
            g.z = z;

            Vector2 delta = new Vector2(1f, 0).rotate(MathUtils.random(0f, 360f));

            g.getBody().setTransform(body.getPosition().mulAdd(delta, 0.1f), 0);
            g.getBody().setLinearVelocity(delta.nor().scl(MathUtils.random(15f, speed)));

            GooGame.i.getEntities().add(g);
        }
    }
}
