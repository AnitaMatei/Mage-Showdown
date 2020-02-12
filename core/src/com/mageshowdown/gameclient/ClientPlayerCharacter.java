package com.mageshowdown.gameclient;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.mageshowdown.gamelogic.*;
import com.mageshowdown.packets.Network;
import com.mageshowdown.packets.Network.CastSpellProjectile;
import com.mageshowdown.packets.Network.MoveKeyDown;
import com.mageshowdown.packets.Network.SwitchOrbs;
import com.mageshowdown.utils.PrefsKeys;

import java.util.Objects;
import java.util.Random;

import static com.mageshowdown.gameclient.ClientAssetLoader.prefs;

public class ClientPlayerCharacter extends PlayerCharacter
        implements AnimatedActor, InputProcessor {

    private static Random rndSound = new Random();
    private GameClient myClient = GameClient.getInstance();
    private TextureRegion currShieldFrame;
    private TextureRegion currFrozenFrame;
    private boolean isMyPlayer;
    private boolean castSpellProjectile = false;
    private boolean castBomb = false;
    private boolean switchOrbs = false;
    private String userName;
    private int id;

    public ClientPlayerCharacter(ClientGameStage stage, Vector2 position, Orb.SpellType orbEquipped, String userName, boolean isMyPlayer) {
        super(stage, position, orbEquipped, true);
        this.isMyPlayer = isMyPlayer;

        //if its the client's player, load the ally assets for the character, otherwise load the ones for the enemy
        if (isMyPlayer) {
            addAnimation(4, 1, 1.2f, "idle", ClientAssetLoader.friendlyIdleSpritesheet);
            addAnimation(2, 1, .8f, "jumping", ClientAssetLoader.friendlyJumpingSpritesheet);
            addAnimation(8, 1, 1f, "running", ClientAssetLoader.friendlyRunningSpritesheet);
            addAnimation(4, 1, 1.2f, "frozen idle", ClientAssetLoader.frozenFriendlyIdleSpritesheet);
            addAnimation(2, 1, .8f, "frozen jumping", ClientAssetLoader.frozenFriendlyJumpingSpritesheet);
            addAnimation(8, 1, 2f, "frozen running", ClientAssetLoader.frozenFriendlyRunningSpritesheet);
        } else {
            addAnimation(4, 1, 1.2f, "idle", ClientAssetLoader.enemyIdleSpritesheet);
            addAnimation(2, 1, .8f, "jumping", ClientAssetLoader.enemyJumpingSpritesheet);
            addAnimation(8, 1, 1f, "running", ClientAssetLoader.enemyRunningSpritesheet);
            addAnimation(4, 1, 1.2f, "frozen idle", ClientAssetLoader.frozenEnemyIdleSpritesheet);
            addAnimation(2, 1, .8f, "frozen jumping", ClientAssetLoader.frozenEnemyJumpingSpritesheet);
            addAnimation(8, 1, 2f, "frozen running", ClientAssetLoader.frozenEnemyRunningSpritesheet);
        }

        addAnimation(5, 4, 2f, "energy shield", ClientAssetLoader.energyShieldSpritesheet);
        this.userName = userName;

    }

    @Override
    public void act(float delta) {
        sendInputPackets();
        pickFrame();
        updateOrbPosition();
        updateFrozenState();
        updateSpellState();
        super.act(delta);
        //the sprite flip is calculated after the horizontal state and vertical state are determined in super.act()
        calculateSpriteFlip();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (dmgImmune)
            batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b, .5f);
        if (energyShield > 0 && currShieldFrame != null)
            batch.draw(currShieldFrame, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX() + .5f, getScaleY() + .5f, getRotation());
        if (dmgImmune)
            batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b, 1f);
    }

    @Override
    public void pickFrame() {
        if (animations.containsKey("energy shield"))
            currShieldFrame = animations.get("energy shield").getKeyFrame(passedTime, true);

        if (verticalState == null || horizontalState == null)
            return;

        String verticalStateString = verticalState.toString(),
                horizontalStateString = horizontalState.toString();
        if (frozen) {
            verticalStateString = "frozen " + verticalStateString;
            horizontalStateString = "frozen " + horizontalStateString;
        }
        /*
         * if the character is grounded, we have to see if hes moving left or right or standing and change the animation accordingly
         * regardless of wether its flying or its grounded, the animation frame needs
         * to be flipped if youre going left, looking right being the "default" position
         */
        switch (verticalState) {
            case GROUNDED:
                if (animations.containsKey(horizontalStateString))
                    currFrame = animations.get(horizontalStateString).getKeyFrame(passedTime, true);
                break;
            case FLYING:
                if (animations.containsKey(verticalStateString))
                    currFrame = animations.get(verticalStateString).getKeyFrame(passedTime, false);
                break;
        }

    }


    //if keys were pressed this frame here is where we send the corresponding packets
    private void sendInputPackets() {
        //we are only going to send input packets only for every client's own player
        if (!isMyPlayer)
            return;
        MoveKeyDown keyPress = new MoveKeyDown();


        if (moveLeft) {
            keyPress.keycode = Input.Keys.A;
            myClient.sendTCP(keyPress);
        } else if (moveRight) {
            keyPress.keycode = Input.Keys.D;
            myClient.sendTCP(keyPress);
        }

        /*
         * the jump conditional is separate from the moving ones since
         * we want to be able to jump and move at the same time
         */
        if (jump) {
            keyPress.keycode = Input.Keys.W;
            myClient.sendTCP(keyPress);
        }
        if (castSpellProjectile) {
            castMySpellProjectile();
        } else if (castBomb) {
            castMyBomb();
        }
        if (switchOrbs) {
            switchMyOrbs();
            myClient.sendTCP(new SwitchOrbs());
            switchOrbs = false;
        }
    }


    //here the sprite is flipped depending on how the character is moving or where the mouse is
    private void calculateSpriteFlip() {
        if (currFrame != null) {
            switch (horizontalState) {
                case GOING_LEFT:
                    //as there is no setFlip() for TextureRegion we have to check if the texture's already flipped, libgdx pls
                    if (!currFrame.isFlipX())
                        currFrame.flip(true, false);
                    break;
                case GOING_RIGHT:
                    if (currFrame.isFlipX())
                        currFrame.flip(true, false);
                    break;
                case STANDING:
                    //we only flip the client's own character in the direction of the mouse
                    if (isMyPlayer) {
                        if (GameWorld.getMousePos().x < getX()) {
                            currFrame.flip(!currFrame.isFlipX(), false);
                        } else {
                            currFrame.flip(currFrame.isFlipX(), false);
                        }
                    } else {
                        currFrame.flip(currFrame.isFlipX(), false);
                    }
                    break;
            }
        }
    }

    //when casting a projectile we need the rotation angle from which we calculate the direction vector
    public void castMySpellProjectile() {
        CastSpellProjectile packet = new CastSpellProjectile();
        float rotation = GameWorld.getMouseVectorAngle(new Vector2(currentOrb.getX() + currentOrb.getOriginX(), currentOrb.getY() + currentOrb.getOriginY()));
        Vector2 direction = new Vector2((float) Math.cos(rotation * Math.PI / 180), (float) Math.sin(rotation * Math.PI / 180));


        packet.id = myClient.getID();
        packet.rot = rotation;
        myClient.sendTCP(packet);

        if (currentOrb.castSpellProjectile(direction, rotation, myClient.getID())) {
            if (currentOrb.getSpellType() == Orb.SpellType.FROST)
                hasJustCastFrostProjectile();
            else hasJustCastLaser();
        }

        castSpellProjectile = false;
    }

    //when casting a bomb we only want the position of the mouse
    public void castMyBomb() {
        Network.CastBomb packet = new Network.CastBomb();

        packet.id = myClient.getID();
        packet.pos = GameWorld.getMousePos(new Vector2(95, 95));
        myClient.sendTCP(packet);

        if (currentOrb.castBomb(packet.pos, myClient.getID()))
            if (currentOrb.getSpellType() == Orb.SpellType.FROST)
                hasJustCastFrostBomb();
            else hasJustCastFireBomb();

        castBomb = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientPlayerCharacter that = (ClientPlayerCharacter) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return userName + " " + kills + " " + score;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.D:
                horizontalState = HorizontalState.GOING_RIGHT;
                moveRight = true;
                break;
            case Input.Keys.A:
                horizontalState = HorizontalState.GOING_LEFT;
                moveLeft = true;
                break;
            case Input.Keys.W:
                jump = true;
                break;
            case Input.Keys.Q:
                switchOrbs = true;
                break;
            case Input.Keys.ESCAPE:
                GameScreen.getRootTable().getColor().a = 0;
                GameScreen.setState(GameScreen.State.GAME_PAUSED);
                GameScreen.getRootTable().addAction(Actions.fadeIn(0.1f));
                Gdx.input.setInputProcessor(GameScreen.getEscMenuStage());
                break;
            case Input.Keys.TAB:
                if (GameScreen.getState() == GameScreen.State.GAME_RUNNING) {
                    GameScreen.setState(GameScreen.State.SCOREBOARD);
                    GameScreen.addActionToScoreboard(Actions.fadeIn(0.1f));
                } else if (GameScreen.getState() == GameScreen.State.SCOREBOARD) {
                    GameScreen.addActionToScoreboard(Actions.sequence(Actions.fadeOut(0.1f)
                            , Actions.run(() -> GameScreen.setState(GameScreen.State.GAME_RUNNING))));
                }
                break;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        Network.MoveKeyUp ku = new Network.MoveKeyUp();
        ku.keycode = keycode;
        if (keycode == Input.Keys.A) {
            moveLeft = false;
            myClient.sendTCP(ku);
        } else if (keycode == Input.Keys.D) {
            moveRight = false;
            myClient.sendTCP(ku);
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT)
            castSpellProjectile = true;
        else if (button == Input.Buttons.RIGHT)
            castBomb = true;
        return true;
    }

    public void hasJustFrozen() {
        Gdx.app.log("game_event", "someone just froze");
    }

    public void hasJustCastFrostProjectile() {
        Gdx.app.log("game_event", "someone just cast a frost spell projectile");
        ClientAssetLoader.frostShot.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME) / 2);
    }

    public void hasJustCastLaser() {
        Gdx.app.log("game_event", "someone just cast a laser spell");
        if (rndSound.nextBoolean())
            ClientAssetLoader.fireShot1.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME) / 2);
        else
            ClientAssetLoader.fireShot2.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME) / 2);
    }

    public void hasJustCastFireBomb() {
        Gdx.app.log("game_event", "someone just cast a fire bomb");
    }

    public void hasJustCastFrostBomb() {
        Gdx.app.log("game_event", "someone just cast a frost bomb");
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public void setMyPlayer(boolean myPlayer) {
        isMyPlayer = myPlayer;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
