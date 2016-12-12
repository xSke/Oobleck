package space.ske.goo.entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import space.ske.goo.GooGame;
import space.ske.goo.util.SpringingContext1D;
import space.ske.goo.util.SpringingContext2D;

public class BoxShapedEntity extends Entity {
    private final Fixture mainBody;
    final         Fixture bottomSensor;
    final         Fixture leftSensor;
    final         Fixture rightSensor;
    private final float   w;
    private final float   h;

    SpringingContext2D size  = new SpringingContext2D(0.2f, 4);
    SpringingContext1D shear = new SpringingContext1D(0.2f, 4);

    public BoxShapedEntity(float w, float h, int category, int mask) {
        this.w = w;
        this.h = h;
        mainBody = createRect(w, h);
        mainBody.setFriction(0);

        bottomSensor = createRect(w - 0.1f, 0.1f, 0, -h / 2);
        leftSensor = createRect(0.1f, h - 0.1f, -w / 2, 0);
        rightSensor = createRect(0.1f, h - 0.1f, w / 2, 0);

        size.target.set(size.value.set(w, h));

        Filter mainBodyFlags = new Filter();
        mainBodyFlags.categoryBits = (short) category;
        mainBodyFlags.maskBits = (short) mask;

        Filter collisionSensorFlags = new Filter();
        collisionSensorFlags.categoryBits = (short) category;
        collisionSensorFlags.maskBits = 0x01;

        mainBody.setFilterData(mainBodyFlags);
        bottomSensor.setFilterData(collisionSensorFlags);
        leftSensor.setFilterData(collisionSensorFlags);
        rightSensor.setFilterData(collisionSensorFlags);

        bottomSensor.setSensor(true);
        leftSensor.setSensor(true);
        rightSensor.setSensor(true);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        size.update(deltaTime);
        shear.update(deltaTime);
    }

    @Override
    public void draw(ShapeRenderer sr) {
        super.draw(sr);
        Vector2 pos = body.getPosition();
        sr.setColor(1, 1, 1, 1);

        float bottomY = pos.y - h / 2;

        Affine2 aff = new Affine2();
        aff.translate(pos.x, bottomY);
        aff.shear(shear.value, 0);

        sr.getTransformMatrix().set(aff);
        sr.updateMatrices();

        sr.rect(-size.value.x / 2f * w, 0, size.value.x * w, size.value.y * h);

        sr.identity();
    }
}
