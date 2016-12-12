package space.ske.goo.entity;

public class BasicEnemy extends Enemy {
    private float direction = -1;

    public BasicEnemy() {
        super(1.1f, 1.1f, 0x07);
    }

    @Override
    public void update(float deltaTime) {
        if (direction == -1 && anythingTouches(leftSensor)) direction *= -1;
        if (direction == 1 && anythingTouches(rightSensor)) direction *= -1;

        body.setLinearVelocity(direction * 5, body.getLinearVelocity().y);
        shear.value = 0.2f * direction;

        super.update(deltaTime);
    }
}
