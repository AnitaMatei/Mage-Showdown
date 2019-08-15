package com.mageshowdown.gamelogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mageshowdown.gameclient.ClientAssetLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;


public class Orb extends GameActor implements AnimatedActor {

    public enum SpellType {
        FROST,
        FIRE
    }

    private SpellType spellType;
    private ArrayList<Spell> spells;
    private static final HashMap<String, Float> spellCosts;
    //we need the stage as an attribute to re-add the orb to it when we equip it
    private Stage gameStage;

    /*
     * orbs will work like an energy shield; their mana will constantly regenerate
     * casting with the orb interrupts that process, after which there is a small cooldown until the regeneration starts again
     */
    private final float COOLDOWN_TIME;
    private final float MAXIMUM_MANA;
    private final float RECHARGE_RATE = 10f;   //recharge rate per second
    private float currentMana;
    private float rechargeTimer = 0f;
    private boolean recharge = false;

    static {
        spellCosts = new HashMap<>();
        spellCosts.put("freeze projectile", 4f);
        spellCosts.put("laser", 3f);
        spellCosts.put("freeze bomb", 8f);
        spellCosts.put("fire bomb", 6f);
    }

    public Orb(Stage stage, SpellType spellType, float cd, int capacity, boolean isClient) {
        super(stage, new Vector2(0, 0), new Vector2(64, 66), new Vector2(0,0), 0f, isClient);
        this.gameStage = stage;
        COOLDOWN_TIME = cd;
        MAXIMUM_MANA = capacity;
        currentMana = MAXIMUM_MANA;
        this.spellType = spellType;
        spells = new ArrayList<Spell>();


        if (CLIENT_ACTOR)
            switch (spellType) {
                case FROST:
                    addAnimation(6, 1, 1.5f, "idle", ClientAssetLoader.crystalSpritesheet);
                    break;
                case FIRE:
                    addAnimation(7, 1, 1f, "idle", ClientAssetLoader.flameSpritesheet);
                    break;
            }
    }

    public void updatePosition(Vector2 position) {
        Vector2 offset = new Vector2();

        switch (spellType) {
            case FROST:
                offset = new Vector2(-15f, 35f);
                break;
            case FIRE:
                offset = new Vector2(-15f, 50f);
                break;
        }
        setPosition(position.x + offset.x, position.y + offset.y);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        rechargeOrb();

        if (CLIENT_ACTOR)
            pickFrame();
    }

    public void destroyEliminatedSpells() {
        /*
         * if it has collided or out of the screen remove the spell from the arraylist and the stage so theres no reference to it left
         */
        ListIterator<Spell> iter = spells.listIterator();
        while (iter.hasNext()) {
            Spell x = iter.next();
            if (x.isOutOfBounds() || x.canDestroy() || x.isExpired()) {
                x.remove();
                iter.remove();
            }
        }
    }

    public boolean castSpellProjectile(Vector2 direction, float rotation, int ownerId) {
        switch (spellType) {
            case FROST:
                if (currentMana > spellCosts.get("freeze projectile")) {
                    spells.add(new FrostProjectile(getStage(), getStartPosition(rotation), rotation, direction, spells.size(), ownerId, CLIENT_ACTOR));
                    currentMana -= spellCosts.get("freeze projectile");
                    recharge = false;
                    return true;
                }
                break;
            case FIRE:
                if (currentMana > spellCosts.get("laser")) {
                    spells.add(new Laser(getStage(), getStartPosition(rotation), rotation, spells.size(), ownerId, CLIENT_ACTOR));
                    currentMana -= spellCosts.get("laser");
                    recharge = false;
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean castBomb(Vector2 position, int ownerId) {
        switch (spellType) {
            case FROST:
                if (currentMana > spellCosts.get("freeze bomb")) {
                    spells.add(new Bomb(getStage(), position, 0, spells.size(), ownerId, spellType, CLIENT_ACTOR));
                    currentMana -= spellCosts.get("freeze bomb");
                    recharge = false;
                    return true;
                }
                break;
            case FIRE:
                if (currentMana > spellCosts.get("fire bomb")) {
                    spells.add(new Bomb(getStage(), position, 0, spells.size(), ownerId, spellType,CLIENT_ACTOR));
                    currentMana -= spellCosts.get("fire bomb");
                    recharge = false;
                    return true;
                }
                break;
        }
        return false;
    }

    private void rechargeOrb() {
        if (recharge) {
            if (currentMana < MAXIMUM_MANA) {
                currentMana += RECHARGE_RATE * Gdx.graphics.getDeltaTime();
            }
        } else {
            rechargeTimer += Gdx.graphics.getDeltaTime();
            if (rechargeTimer >= COOLDOWN_TIME) {
                recharge = true;
                rechargeTimer = 0f;
            }
        }
    }

    public Vector2 getStartPosition(float angle) {
        return new Vector2((getX()+getOriginX()-5)+(getOriginX()-15)*(float)Math.cos(angle*Math.PI/180)
                , (getY()+getOriginY()-15)+(getOriginY()-15)*(float)Math.sin(angle*Math.PI/180));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (currFrame != null)
            batch.draw(currFrame, getX(), getY(), getWidth() * getScaleX(), getHeight() * getScaleY());
    }

    @Override
    public void pickFrame() {
        if (animations.get("idle") != null)
            currFrame = animations.get("idle").getKeyFrame(passedTime, true);
    }

    @Override
    public void clearQueue() {
        for (Spell x : spells) {
            x.clearQueue();
        }
    }


    public void equipOrb() {
        gameStage.addActor(this);
    }

    //when we unequip the orb we want the spells from the previous one to remain
    //so we only remove the orb itself from the stage
    public void unequipOrb() {
        super.remove();
    }


    @Override
    public boolean remove() {
        removeCastSpells();
        return super.remove();
    }

    public void removeCastSpells(){
        ListIterator<Spell> iterator = spells.listIterator();
        while (iterator.hasNext()) {
            Spell x = iterator.next();
            x.remove();
            iterator.remove();
        }
    }

    public float getMaxCapacity() {
        return MAXIMUM_MANA;
    }

    public float getCurrentMana() {
        return currentMana;
    }

    public SpellType getSpellType() {
        return spellType;
    }
}
