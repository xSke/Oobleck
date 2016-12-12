package space.ske.goo;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import space.ske.goo.entity.Entity;

public class CollisionTracker implements ContactListener {
    private ObjectMap<Fixture, Array<Fixture>> fixtureContacts = new ObjectMap<Fixture, Array<Fixture>>();

    @Override
    public void beginContact(Contact contact) {
        //if ((contact.getFixtureA().getFilterData().maskBits & contact.getFixtureB().getFilterData().categoryBits) == 0) return;
        //if ((contact.getFixtureB().getFilterData().maskBits & contact.getFixtureA().getFilterData().categoryBits) == 0) return;

        Body ba = contact.getFixtureA().getBody();
        Body bb = contact.getFixtureB().getBody();

        if (ba.getUserData() instanceof Entity) {
            ((Entity) ba.getUserData()).collide(contact.getFixtureB());
            if (bb.getUserData() instanceof Entity) {
                ((Entity) ba.getUserData()).collide((Entity) bb.getUserData());
            }
        }

        if (bb.getUserData() instanceof Entity) {
            ((Entity) bb.getUserData()).collide(contact.getFixtureA());
            if (ba.getUserData() instanceof Entity) {
                ((Entity) bb.getUserData()).collide((Entity) ba.getUserData());
            }
        }

        if (!fixtureContacts.containsKey(contact.getFixtureA())) fixtureContacts.put(contact.getFixtureA(), new Array<Fixture>());
        if (!fixtureContacts.containsKey(contact.getFixtureB())) fixtureContacts.put(contact.getFixtureB(), new Array<Fixture>());
        fixtureContacts.get(contact.getFixtureA()).add(contact.getFixtureB());
        fixtureContacts.get(contact.getFixtureB()).add(contact.getFixtureA());
    }

    @Override
    public void endContact(Contact contact) {
        //if ((contact.getFixtureA().getFilterData().maskBits & contact.getFixtureB().getFilterData().categoryBits) == 0) return;
        //if ((contact.getFixtureB().getFilterData().maskBits & contact.getFixtureA().getFilterData().categoryBits) == 0) return;

        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();
        fixtureContacts.get(fa).removeValue(fb, true);
        fixtureContacts.get(fb).removeValue(fa, true);
        if (fixtureContacts.get(fa).size == 0) fixtureContacts.remove(fa);
        if (fixtureContacts.get(fb).size == 0) fixtureContacts.remove(fb);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    public ObjectMap<Fixture, Array<Fixture>> getFixtureContacts() {
        return fixtureContacts;
    }
}
