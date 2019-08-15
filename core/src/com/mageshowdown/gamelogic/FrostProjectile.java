package com.mageshowdown.gamelogic;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mageshowdown.gameclient.ClientAssetLoader;
import com.mageshowdown.utils.PrefsKeys;

import static com.mageshowdown.gameclient.ClientAssetLoader.prefs;

public class FrostProjectile extends Spell implements AnimatedActor {

    public FrostProjectile(Stage stage, Vector2 position, float rotation, Vector2 direction, int id, int ownerId, boolean isClient) {
        super(stage, position, new Vector2(46, 31), new Vector2(1f, 1f), new Vector2(24, 12), rotation, id, ownerId, 4, isClient);
        createBody(getRotation(), new Vector2(bodySize.x / 2, bodySize.y / 2), BodyDef.BodyType.DynamicBody,
                () -> {
                    makeBodySensor();
                    //the velocity is set to the direction of the projectile multiplied by a speed constant
                    body.setLinearVelocity( new Vector2(3.5f * direction.x, 3.5f * direction.y));
                    return null;
                }
        );


        if (CLIENT_ACTOR) {
            addAnimation(9, 1, 1f, "idle", ClientAssetLoader.freezeProjectileSpritesheet);
            addAnimation(1, 8, 1f, "impact", ClientAssetLoader.freezeProjectileImpactSpritesheet);
        }
    }


    @Override
    public void act(float delta) {
        super.act(delta);
        if (collided && passedTime >= 1f)
            destroyable = true;

        if (CLIENT_ACTOR)
            pickFrame();
    }

    @Override
    public void pickFrame() {
        if (!collided && animations.containsKey("idle")) {
            currFrame = animations.get("idle").getKeyFrame(passedTime, true);
        } else if (collided && animations.containsKey("impact")) {
            currFrame = animations.get("impact").getKeyFrame(passedTime, false);
        }
    }

    @Override
    public void setCollided(boolean collided) {
        super.setCollided(collided);
        //when the projectile collides with something we reset the internal passed time and use it for the impact animation
        passedTime = 0f;
        if (collided) {
            body.setLinearVelocity(new Vector2(0, 0));
            setScale(1.5f);
        }
        if (CLIENT_ACTOR)
            hasJustCollided();
    }

    private void hasJustCollided() {
        ClientAssetLoader.frozenEffect.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME) / 3);
    }
}
