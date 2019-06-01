package com.mageshowdown.gamelogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.Stage;


public abstract class PlayerCharacter extends DynamicGameActor {
    protected static final float MAXIMUM_ENERGY_SHIELD = 5f;
    protected static final float MAXIMUM_HEALTH = 15f;
    protected static final float FREEZE_DURATION = 2f;

    protected Stage gameStage;

    protected Orb frostOrb;
    protected Orb fireOrb;
    protected Orb currentOrb;

    protected float energyShield = MAXIMUM_ENERGY_SHIELD;
    protected float health = MAXIMUM_HEALTH;

    protected boolean dmgImmune = false;
    protected boolean frozen = false;
    protected float frozenTimer = 0f;

    protected int score = 0;
    protected int kills = 0;

    protected PlayerCharacter(Stage stage, Vector2 position, int orbEquipped, boolean loadOrbAnimation) {
        super(stage, position, new Vector2(22, 32),new Vector2(22,32), 0f, new Vector2(1.5f, 1.5f));

        createBody(new Vector2(getOriginX(),getOriginY()),BodyDef.BodyType.DynamicBody);
        gameStage = stage;

        frostOrb = new Orb(stage, loadOrbAnimation, Orb.SpellType.FROST, 1.5f, 25);
        fireOrb = new Orb(stage, loadOrbAnimation, Orb.SpellType.FIRE, 1.5f, 25);

        frostOrb.remove();
        fireOrb.remove();

        if (orbEquipped == 1)
            currentOrb = frostOrb;
        else currentOrb = fireOrb;

        gameStage.addActor(currentOrb);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        updateSpellState();
        setBodyFixedRotation();
    }

    protected void setBodyFixedRotation(){
        if(body!=null && !body.isFixedRotation())
            body.setFixedRotation(true);
    }

    @Override
    public boolean remove() {
        destroyOrbs();
        return super.remove();
    }


    protected void updateOrbPosition() {
        if (currentOrb != null)
            currentOrb.updatePosition(new Vector2(getX(), getY()));
    }

    protected void updateSpellState(){
        if(frostOrb !=null)
            frostOrb.destroyEliminatedSpells();
        if(fireOrb !=null)
            fireOrb.destroyEliminatedSpells();
    }

    protected void updateFrozenState() {
        if (frozen) {
            frozenTimer += Gdx.graphics.getDeltaTime();
            if (frozenTimer > FREEZE_DURATION) {
                frozen = false;
                frozenTimer = 0f;
            }
        }
    }

    protected void destroyOrbs() {
        frostOrb.remove();
        fireOrb.remove();
    }

    public void switchMyOrbs() {
        currentOrb.unequipOrb();

        if (currentOrb.equals(frostOrb)) {
            currentOrb = fireOrb;
        } else {
            currentOrb = frostOrb;
        }
        currentOrb.equipOrb();
    }

    public Orb getCurrentOrb() {
        return currentOrb;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public static float getMaxShield() {
        return MAXIMUM_ENERGY_SHIELD;
    }

    public static float getMaxHealth() {
        return MAXIMUM_HEALTH;
    }

    public float getEnergyShield() {
        return energyShield;
    }

    public void setEnergyShield(float energyShield) {
        this.energyShield = energyShield;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public boolean isDmgImmune() {
        return dmgImmune;
    }

    public void setDmgImmune(boolean dmgImmune) {
        this.dmgImmune = dmgImmune;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getEquippedOrb() {
        if (currentOrb.equals(frostOrb))
            return 1;
        else return 2;
    }
}
