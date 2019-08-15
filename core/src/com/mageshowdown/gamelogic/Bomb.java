package com.mageshowdown.gamelogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mageshowdown.gameclient.ClientAssetLoader;
import com.mageshowdown.utils.PrefsKeys;

import static com.mageshowdown.gameclient.ClientAssetLoader.prefs;

public class Bomb extends Spell implements AnimatedActor {

    private final float duration;
    private Orb.SpellType spellType;
    //each bomb has an "arming time", after which it explodes and it damages whoever is in range
    private float explosionTime = 0f;
    private boolean exploded = false;

    public Bomb(Stage stage, Vector2 position, float rotation, int id, int ownerId, Orb.SpellType spellType, boolean isClient) {
        super(stage, position, new Vector2(190, 190), new Vector2(.8f, .8f), new Vector2(140, 140), rotation, id, ownerId, 9, isClient);

        this.spellType = spellType;
        switch (spellType) {
            case FROST:
                duration = 2.5f;
                if (CLIENT_ACTOR) {
                    addAnimation(5, 5, duration / 2f, "explosion", ClientAssetLoader.freezeBombSpritesheet);
                    addAnimation(5, 4, duration / 2f, "arm", ClientAssetLoader.armFreezeBombSpritesheet);
                }
                break;
            case FIRE:
                duration = 2.5f;
                if (CLIENT_ACTOR) {
                    addAnimation(5, 4, duration / 2f, "explosion", ClientAssetLoader.fireBombSpritesheet);
                    addAnimation(5, 4, duration / 2f, "arm", ClientAssetLoader.armFireBombSpritesheet);
                }
                break;
            default:
                duration = 0;
                break;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (CLIENT_ACTOR)
            pickFrame();
        if (exploded) {
            explosionTime += Gdx.graphics.getDeltaTime();
        }
        if (explosionTime > duration / 2f) {
            setExpired(true);
        }
        if (passedTime > duration / 2f && !exploded) {
            exploded = true;
            if(CLIENT_ACTOR)
                hasJustExploded();
            /*
            * we enable collision with the bomb only after it actually explodes by only then creating the actual body
            * since the size of the body differs from the one of the actor we'll have to offset the position
            * of the actor by the difference in sizes
            * this way the body doesnt end up in a slightly different place from where the bomb was cast
            */
            setPosition(getX()+25,getY()+25);
            createBody(new Vector2(bodySize.x/2, bodySize.y/2), BodyDef.BodyType.StaticBody,
                    ()->{
                makeBodySensor();
                return null;
                    });
        }
    }


    @Override
    public void pickFrame() {
        if (!exploded) {
            if (animations.containsKey("arm"))
                currFrame = animations.get("arm").getKeyFrame(passedTime, false);
        } else {
            if (animations.containsKey("explosion"))
                currFrame = animations.get("explosion").getKeyFrame(explosionTime, false);
        }
    }

    private void hasJustExploded(){
        if(spellType==Orb.SpellType.FROST)
            ClientAssetLoader.frostBombExplosion.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME) / 2);
        else if(spellType==Orb.SpellType.FIRE)
            ClientAssetLoader.fireBombExplosion.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME) / 2);
    }

    public Orb.SpellType getSpellType() {
        return spellType;
    }
}
