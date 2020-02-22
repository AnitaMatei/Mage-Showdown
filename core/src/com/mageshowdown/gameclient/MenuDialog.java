package com.mageshowdown.gameclient;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mageshowdown.utils.PrefsKeys;

import static com.mageshowdown.gameclient.ClientAssetLoader.*;

public class MenuDialog extends Dialog {

    public MenuDialog(String title, String message, Skin skin, String windowStyleName) {
        super("", skin, windowStyleName);
        setMovable(false);
        this.pad(25);
        getContentTable().padBottom(50);

        Label titleLable = new Label(title, new Label.LabelStyle(hugeSizeFont, Color.WHITE));
        Label msgLabel = new Label(message, new Label.LabelStyle(bigSizeFont, Color.WHITE));
        getContentTable().defaults().space(50, 0, 50, 0);
        getContentTable().add(titleLable);
        getContentTable().row();
        getContentTable().add(msgLabel);

        getButtonTable().defaults().space(0, 20, 0, 20).width(150).height(60);
    }

    @Override
    public Dialog button(String text) {
        return button(text, null);
    }

    @Override
    public Dialog button(String text, Object object) {
        if (getSkin() == null)
            throw new IllegalStateException("This method may only be used if the dialog was constructed with a Skin.");
        return button(text, object, getSkin().get(TextButton.TextButtonStyle.class));
    }

    @Override
    public Dialog button(String text, Object object, TextButton.TextButtonStyle buttonStyle) {
        return button(new TextButton(text, buttonStyle), object);
    }

    @Override
    public Dialog button(Button button) {
        return button(button, null);
    }

    @Override
    public Dialog button(Button button, Object object) {
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
            }
        });
        return super.button(button, object);
    }

    @Override
    public Dialog show(Stage stage) {
        return show(stage, Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.1f)));
    }

    @Override
    public void hide() {
        super.hide(null);
    }

    @Override
    public Dialog show(Stage stage, Action action) {
        super.show(stage, action);
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
        return this;
    }

    @Override
    @Deprecated
    public void hide(Action action) {
        super.hide(null);
    }

    @Override
    protected void result(Object object) {
        if (object != null)
            if (object instanceof Runnable)
                ((Runnable) object).run();
            else throw new IllegalArgumentException("Provided object type must be a Runnable");
    }
}
