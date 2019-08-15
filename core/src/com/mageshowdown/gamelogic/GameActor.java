package com.mageshowdown.gamelogic;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.*;
import com.mageshowdown.gameclient.GameClient;


import java.util.HashMap;
import java.util.concurrent.Callable;

public abstract class GameActor extends Actor {

    //in this boolean ill know wether the actor is created by a client or a server, for the purpose of loading assets
    protected final boolean CLIENT_ACTOR;

    protected Sprite sprite;
    protected Body body;
    protected HashMap<String, Animation<TextureRegion>> animations;
    protected float passedTime = 0f;
    protected TextureRegion currFrame;
    protected Vector2 bodySize;

    /*
     * declaring the variable where im holding the position of the actor
     * after the WORLD steps, we update its body with these values
     */
    protected Vector2 queuedPos;
    protected boolean canClearPos = false;


    protected GameActor(Stage stage, Vector2 position, Vector2 size, Vector2 bodySize, float rotation, Vector2 sizeScaling, boolean isClient) {
        setScale(sizeScaling.x, sizeScaling.y);
        setPosition(position.x, position.y);
        setSize(size.x, size.y);
        CLIENT_ACTOR=isClient;
        this.bodySize = bodySize;
        setOrigin(size.x / 2, size.y / 2);
        setRotation(rotation);
        animations = new HashMap<>();
        stage.addActor(this);
    }

    protected GameActor(Stage stage, Vector2 position, Vector2 size, Vector2 bodySize, float rotation) {
        this(stage, position, size, bodySize, rotation, new Vector2(1, 1),true);
    }

    protected GameActor(Stage stage, Vector2 position, Vector2 size, Vector2 bodySize, float rotation, Vector2 sizeScaling) {
        this(stage, position, size, bodySize, rotation, sizeScaling, true);
    }

    protected GameActor(Stage stage, Vector2 position, Vector2 size, Vector2 bodySize, float rotation, boolean isClient) {
        this(stage, position, size, bodySize, rotation, new Vector2(1, 1),isClient);
    }

    @Override
    public void act(float delta) {
        passedTime += delta;

        if (body != null) {
            updatePositionFromBody();
        }
        if (sprite != null) {
            sprite.setPosition(getX(), getY());
        }

        super.act(delta);
    }

    protected void updatePositionFromBody() {
        Vector2 convPosition = GameWorld.convertWorldToPixels(body.getPosition());
        setPosition(convPosition.x - getOriginX(), convPosition.y - getOriginY());
        setRotation(body.getAngle() * 180 / (float) Math.PI);
    }

    @Override
    protected void positionChanged() {
        super.positionChanged();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (sprite != null)
            batch.draw(sprite, getX(), getY(), sprite.getOriginX(), sprite.getOriginY(), getWidth(), getHeight(), 1f, 1f, sprite.getRotation());
        else if (currFrame != null)
            batch.draw(currFrame, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
    }

    protected void addAnimation(int frameColumns, int frameRows, float animationDuration, String animationName, Texture spriteSheet) {

        /*
         * split the spritesheet by number of columns and rows into a TextureRegion matrix,
         * put those frames into an ArrayList and then create the animation from said ArrayList
         */
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet,
                spriteSheet.getWidth() / frameColumns,
                spriteSheet.getHeight() / frameRows);
        TextureRegion[] animationFrames = new TextureRegion[frameColumns * frameRows];

        int index = 0;
        for (int i = 0; i < frameRows; i++)
            for (int j = 0; j < frameColumns; j++)
                animationFrames[index++] = tmp[i][j];

        animations.put(animationName,
                new Animation<TextureRegion>(animationDuration / (float) (frameColumns * frameRows), animationFrames));
    }


    protected void createBody(float density, float friction, float restitution, float rotation, Vector2 origin, BodyDef.BodyType bodyType) {

        body = CreateBodies.createRectangleBody(new Vector2(getX(), getY()), bodySize, origin, bodyType, density, friction, restitution, rotation);
        setTouchable(Touchable.enabled);

        //we set the body's user data to the current object in order to retrieve it later for collision handling
        body.setUserData(this);
    }

    public void clearQueue() {
        if (canClearPos) {
            body.setTransform(queuedPos, body.getAngle());
            canClearPos = false;
        }
    }

    public void setQueuedPos(Vector2 queuedPos) {
        this.queuedPos = queuedPos;
        canClearPos = true;
    }

    protected void createBody(float rotation, Vector2 origin, BodyDef.BodyType bodyType, Callable<Void> bodySettings) {
        final float localRotation = rotation;
        final BodyDef.BodyType localBodyType = bodyType;
        final Vector2 localOrigin = origin;
        GameWorld.addToBodyCreationQueue(() -> {
            createBody(.6f, 0f, 0f, localRotation, localOrigin, localBodyType);
            return null;
        },bodySettings);
    }

    protected void createBody(Vector2 origin, BodyDef.BodyType bodyType, Callable<Void> bodySettings) {
        final BodyDef.BodyType localBodyType = bodyType;
        final Vector2 localOrigin = origin;
        GameWorld.addToBodyCreationQueue(() -> {
            createBody(.6f, 0f, 0f, 0, localOrigin, localBodyType);
            return null;
        },bodySettings);
    }

    public Body getBody() {
        return body;
    }

    @Override
    public boolean remove() {
        removeBody();
        return super.remove();
    }

    protected void removeBody(){
        if (body != null)
            GameWorld.addToBodyRemovalQueue(body);

    }
}
