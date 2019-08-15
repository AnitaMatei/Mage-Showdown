package com.mageshowdown.gamelogic;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

public abstract class CollisionManager implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Object obj1 = contact.getFixtureA().getBody().getUserData(),
                obj2 = contact.getFixtureB().getBody().getUserData();

        if (obj1 instanceof FrostProjectile && obj2 instanceof GameActor) {
            handleProjectileCollision((FrostProjectile) obj1, (GameActor) obj2);
        } else if (obj1 instanceof GameActor && obj2 instanceof FrostProjectile) {
            handleProjectileCollision((FrostProjectile) obj2, (GameActor) obj1);
        }
        if (contact.getFixtureA().getUserData() instanceof String && contact.getFixtureA().getUserData().equals("feet")) {
            handleLegsTouchGround((PlayerCharacter) obj1);
        } else if (contact.getFixtureB().getUserData() instanceof String && contact.getFixtureB().getUserData().equals("feet")) {
            handleLegsTouchGround((PlayerCharacter) obj2);
        }
    }

    protected void handleProjectileCollision(FrostProjectile projectile, GameActor actor) {
        if (actor instanceof MapObjectHitbox) {
            projectile.setCollided(true);
        }
    }

    private void handleLegsTouchGround(PlayerCharacter player) {
        //player.setVerticalState(DynamicGameActor.VerticalState.GROUNDED);
        player.increaseNrFeetContacts();
    }

    private void handleLegsLeaveGround(PlayerCharacter player) {
        //player.setVerticalState(DynamicGameActor.VerticalState.FLYING);
        player.decreaseNrFeetContacts();
    }

    @Override
    public void endContact(Contact contact) {
        Object obj1 = contact.getFixtureA().getBody().getUserData(),
            obj2 = contact.getFixtureB().getBody().getUserData();

        if (contact.getFixtureA().getUserData() instanceof String && contact.getFixtureA().getUserData().equals("feet")) {
            handleLegsLeaveGround((PlayerCharacter) obj1);
        } else if (contact.getFixtureB().getUserData() instanceof String && contact.getFixtureB().getUserData().equals("feet")) {
            handleLegsLeaveGround((PlayerCharacter) obj2);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Object obj1 = contact.getFixtureA().getBody().getUserData(),
                obj2 = contact.getFixtureB().getBody().getUserData();

        /*
         * if a spell wants to react to a collision with something, we disable the contact
         * this is needed because if we were to make the body's fixture a sensor,
         * we arent provided the proper contact points between the spell and something else
         */
        if(obj1 instanceof Spell || obj2 instanceof Spell)
            contact.setEnabled(false);
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
