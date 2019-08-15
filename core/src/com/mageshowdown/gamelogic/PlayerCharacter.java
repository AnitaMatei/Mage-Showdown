package com.mageshowdown.gamelogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.Stage;


public abstract class PlayerCharacter extends DynamicGameActor {
    protected static final float MAXIMUM_ENERGY_SHIELD = 5f;
    protected static final float MAXIMUM_HEALTH = 15f;
    protected static final float FREEZE_DURATION = 5f;

    protected boolean moveLeft = false;
    protected boolean moveRight = false;
    protected boolean jump = false;

    //number of fixtures the foot fixture is collided with; if the number is 0, the player isn't grounded
    protected int nrFeetContacts = 0;
    protected boolean jumped = false;
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

    protected PlayerCharacter(Stage stage, Vector2 position, Orb.SpellType orbEquipped, boolean isClient) {
        super(stage, position, new Vector2(22, 32), new Vector2(33, 48), 0f, new Vector2(1.5f, 1.5f), isClient);

        createBody(new Vector2(bodySize.x / 2, bodySize.y / 2), BodyDef.BodyType.DynamicBody,
                () -> {
                    setBodyFixedRotation();
                    Fixture feetFixture = body.createFixture(
                            CreateBodies.createPolygonShape(
                                    GameWorld.convertPixelsToWorld(new Vector2(33, 15)),
                                    GameWorld.convertPixelsToWorld(new Vector2(bodySize.x / 2, bodySize.y / 2))), .6f);
                    feetFixture.setSensor(true);
                    feetFixture.setUserData("feet");
                    return null;
                });
        gameStage = stage;

        frostOrb = new Orb(stage, Orb.SpellType.FROST, 1.5f, 32, isClient);
        fireOrb = new Orb(stage, Orb.SpellType.FIRE, 1.5f, 32, isClient);

        frostOrb.remove();
        fireOrb.remove();

        if (orbEquipped == Orb.SpellType.FROST)
            currentOrb = frostOrb;
        else currentOrb = fireOrb;

        gameStage.addActor(currentOrb);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (dmgImmune)
            batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b, .5f);
        super.draw(batch, parentAlpha);
        if (dmgImmune)
            batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b, 1f);
    }

    @Override
    public void act(float delta) {
        updateSpellState();
        handleMovement();
        super.act(delta);
        checkFeetState();
    }

    protected void handleMovement() {
        if (body == null)
            return;

        if (moveLeft)
            body.setLinearVelocity(new Vector2(-2f, body.getLinearVelocity().y));
        else if (moveRight)
            body.setLinearVelocity(new Vector2(2f, body.getLinearVelocity().y));
        else
            body.setLinearVelocity(new Vector2(0f, body.getLinearVelocity().y));

        if (jump && !jumped) {
            body.applyLinearImpulse(new Vector2(0, .85f), body.getPosition(), true);
            jumped = true;
            jump = false;
        }
    }

    public void startMoving(int keycode){
        switch (keycode) {
            case Input.Keys.A:
                moveLeft=true;
                break;
            case Input.Keys.D:
                moveRight=true;
                break;
            case Input.Keys.W:
                if(!jumped)
                    jump=true;
                break;
        }
    }

    public void stopMoving(int keycode) {
        switch (keycode) {
            case Input.Keys.A:
                moveLeft=false;
                break;
            case Input.Keys.D:
                moveRight=false;
                break;
        }
    }

    private void resetJumpFlag() {
        setVerticalState(VerticalState.GROUNDED);
        jumped = false;
    }

    protected void setBodyFixedRotation() {
        if (body != null && !body.isFixedRotation())
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

    protected void updateSpellState() {
        if (frostOrb != null)
            frostOrb.destroyEliminatedSpells();
        if (fireOrb != null)
            fireOrb.destroyEliminatedSpells();
    }

    protected void checkFeetState() {
        if (nrFeetContacts == 0) {
            setVerticalState(VerticalState.FLYING);
        } else {
            resetJumpFlag();
        }
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

    public void removeCastSpells() {
        frostOrb.removeCastSpells();
        fireOrb.removeCastSpells();
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


    public synchronized void increaseNrFeetContacts() {
        nrFeetContacts++;
    }

    public synchronized void decreaseNrFeetContacts() {
        nrFeetContacts--;
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

}
