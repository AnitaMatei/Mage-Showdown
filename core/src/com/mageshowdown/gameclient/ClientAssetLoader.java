package com.mageshowdown.gameclient;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.*;

public class ClientAssetLoader {

    public static Texture solidBlack;
    public static Skin uiSkin;
    public static Skin hudSkin;
    public static Texture menuBackground;
    //fonts
    public static BitmapFont hugeSizeFont;
    public static BitmapFont bigSizeFont;
    //player animations
    public static Texture friendlyIdleSpritesheet;
    public static Texture frozenFriendlyIdleSpritesheet;
    public static Texture enemyIdleSpritesheet;
    public static Texture frozenEnemyIdleSpritesheet;
    public static Texture friendlyJumpingSpritesheet;
    public static Texture frozenFriendlyJumpingSpritesheet;
    public static Texture enemyJumpingSpritesheet;
    public static Texture frozenEnemyJumpingSpritesheet;
    public static Texture friendlyRunningSpritesheet;
    public static Texture frozenFriendlyRunningSpritesheet;
    public static Texture enemyRunningSpritesheet;
    public static Texture frozenEnemyRunningSpritesheet;
    public static Texture crystalSpritesheet;
    public static Texture freezeProjectileSpritesheet;
    public static Texture freezeProjectileImpactSpritesheet;
    public static Texture freezeBombSpritesheet;
    public static Texture armFreezeBombSpritesheet;
    public static Texture armFireBombSpritesheet;
    public static Texture fireBombSpritesheet;
    public static Texture flameSpritesheet;
    public static Texture fireLaserSpritesheet;
    public static Texture burningSpritesheet;
    public static Texture energyShieldSpritesheet;
    //sounds & music
    public static Sound btnClickSound;
    public static Sound fireShot1;
    public static Sound fireShot2;
    public static Sound frozenEffect;
    public static Sound fireBombExplosion;
    public static Sound frostBombExplosion;
    public static Sound frostShot;
    public static Music menuMusic;
    public static Music gameplayMusic;

    //preferences files
    public static Preferences prefs;
    //maps
    public static TiledMap map1;
    public static TiledMap dungeonMap;
    public static TiledMap purpleMap;
    //credits file

    public AssetManager manager;

    private static ClientAssetLoader ourInstance = new ClientAssetLoader();

    private ClientAssetLoader() {
        manager = new AssetManager();
    }

    public static ClientAssetLoader getInstance() {
        return ourInstance;
    }

    public void loadLoadingScreen() {
        manager.load("UIAssets/golden-ui-skin.json", Skin.class);
        generateFonts();
    }

    public void setLoadingAssets() {
        hudSkin = manager.get("UIAssets/golden-ui-skin.json", Skin.class);
        bigSizeFont = manager.get("joystixBig.ttf", BitmapFont.class);
    }

    public void load() {
        //Friendly player spritesheets
        manager.load("Player Animations/Friendly player/idleAnimationSpritesheet.png", Texture.class);
        manager.load("Player Animations/Friendly player/jumpingAnimationSpritesheet.png", Texture.class);
        manager.load("Player Animations/Friendly player/runningAnimationSpritesheet.png", Texture.class);
        manager.load("Player Animations/Friendly player/frozenIdleAnimationSpritesheet.png", Texture.class);
        manager.load("Player Animations/Friendly player/frozenJumpingAnimationSpritesheet.png", Texture.class);
        manager.load("Player Animations/Friendly player/frozenRunningAnimationSpritesheet.png", Texture.class);

        //Enemy player spritesheets
        manager.load("Player Animations/Enemy player/enemyIdleAnimationSpritesheet.png", Texture.class);
        manager.load("Player Animations/Enemy player/enemyJumpingAnimationSpritesheet.png", Texture.class);
        manager.load("Player Animations/Enemy player/enemyRunningAnimationSpritesheet.png", Texture.class);
        manager.load("Player Animations/Enemy player/frozenEnemyIdleAnimationSpritesheet.png", Texture.class);
        manager.load("Player Animations/Enemy player/frozenEnemyJumpingAnimationSpritesheet.png", Texture.class);
        manager.load("Player Animations/Enemy player/frozenEnemyRunningAnimationSpritesheet.png", Texture.class);

        //Freeze projectiles
        manager.load("Player Animations/Orb/CrystalSpritesheet.png", Texture.class);
        manager.load("Player Animations/Spell/freezeProjectileSpritesheet.png", Texture.class);
        manager.load("Player Animations/Spell/freezeProjectileImpactSpritesheet.png", Texture.class);
        manager.load("Player Animations/Effect/armFreezeBombSpritesheet.png", Texture.class);
        manager.load("Player Animations/Effect/freezeBombSpritesheet.png", Texture.class);

        //Flame laser
        manager.load("Player Animations/Orb/flameSpritesheet.png", Texture.class);
        manager.load("Player Animations/Spell/fireLaserSpritesheet.png", Texture.class);
        manager.load("Player Animations/Effect/burningSpritesheet.png", Texture.class);
        manager.load("Player Animations/Effect/armFireBombSpritesheet.png", Texture.class);
        manager.load("Player Animations/Effect/fireBombSpritesheet.png", Texture.class);

        manager.load("Player Animations/Effect/energyShieldSpriteSheet.png", Texture.class);
        manager.load("Player Animations/Effect/frozenSpritesheet.png", Texture.class);

        manager.load("Sounds/click.ogg", Sound.class);
        manager.load("Sounds/fireShot1.mp3", Sound.class);
        manager.load("Sounds/fireShot2.mp3", Sound.class);
        manager.load("Sounds/IceBlastImpact.wav", Sound.class);
        manager.load("Sounds/fireBomb.wav", Sound.class);
        manager.load("Sounds/frostBomb.wav", Sound.class);
        manager.load("Sounds/IceCast.wav", Sound.class);
        manager.load("Music/Action Break.mp3", Music.class);
        manager.load("Music/Night Pulse.mp3", Music.class);

        manager.load("UIAssets/menuBackground.png", Texture.class);
        manager.load("UIAssets/Black_1080p.png", Texture.class);

        manager.load("UIAssets/uiskin.json", Skin.class);

        manager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        manager.load("Maps/level1.tmx", TiledMap.class);
        manager.load("Maps/level2.tmx", TiledMap.class);
        manager.load("Maps/level3.tmx", TiledMap.class);
    }

    private void generateFonts() {
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        FreetypeFontLoader.FreeTypeFontLoaderParameter hugeParameter = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        hugeParameter.fontFileName = "UIAssets/joystix monospace.ttf";
        hugeParameter.fontParameters.size = 48;
        manager.load("joystixHuge.ttf", BitmapFont.class, hugeParameter);

        FreetypeFontLoader.FreeTypeFontLoaderParameter bigParameter = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        bigParameter.fontFileName = "UIAssets/joystix monospace.ttf";
        bigParameter.fontParameters.size = 24;
        bigParameter.fontParameters.shadowColor = Color.DARK_GRAY;
        bigParameter.fontParameters.shadowOffsetX = 3;
        bigParameter.fontParameters.shadowOffsetY = 3;
        manager.load("joystixBig.ttf", BitmapFont.class, bigParameter);
    }

    public void setAssets() {
        friendlyIdleSpritesheet = manager.get("Player Animations/Friendly player/idleAnimationSpritesheet.png", Texture.class);
        friendlyJumpingSpritesheet = manager.get("Player Animations/Friendly player/jumpingAnimationSpritesheet.png", Texture.class);
        friendlyRunningSpritesheet = manager.get("Player Animations/Friendly player/runningAnimationSpritesheet.png", Texture.class);
        frozenFriendlyIdleSpritesheet = manager.get("Player Animations/Friendly player/frozenIdleAnimationSpritesheet.png", Texture.class);
        frozenFriendlyJumpingSpritesheet = manager.get("Player Animations/Friendly player/frozenJumpingAnimationSpritesheet.png", Texture.class);
        frozenFriendlyRunningSpritesheet = manager.get("Player Animations/Friendly player/frozenRunningAnimationSpritesheet.png", Texture.class);

        enemyIdleSpritesheet = manager.get("Player Animations/Enemy player/enemyIdleAnimationSpritesheet.png", Texture.class);
        enemyJumpingSpritesheet = manager.get("Player Animations/Enemy player/enemyJumpingAnimationSpritesheet.png", Texture.class);
        enemyRunningSpritesheet = manager.get("Player Animations/Enemy player/enemyRunningAnimationSpritesheet.png", Texture.class);
        frozenEnemyIdleSpritesheet = manager.get("Player Animations/Enemy player/frozenEnemyIdleAnimationSpritesheet.png", Texture.class);
        frozenEnemyJumpingSpritesheet = manager.get("Player Animations/Enemy player/frozenEnemyJumpingAnimationSpritesheet.png", Texture.class);
        frozenEnemyRunningSpritesheet = manager.get("Player Animations/Enemy player/frozenEnemyRunningAnimationSpritesheet.png", Texture.class);

        crystalSpritesheet = manager.get("Player Animations/Orb/CrystalSpritesheet.png", Texture.class);
        freezeProjectileSpritesheet = manager.get("Player Animations/Spell/freezeProjectileSpritesheet.png", Texture.class);
        freezeProjectileImpactSpritesheet = manager.get("Player Animations/Spell/freezeProjectileImpactSpritesheet.png", Texture.class);
        armFreezeBombSpritesheet = manager.get("Player Animations/Effect/armFreezeBombSpritesheet.png", Texture.class);
        freezeBombSpritesheet = manager.get("Player Animations/Effect/freezeBombSpritesheet.png", Texture.class);

        flameSpritesheet = manager.get("Player Animations/Orb/flameSpritesheet.png", Texture.class);
        fireLaserSpritesheet = manager.get("Player Animations/Spell/fireLaserSpritesheet.png", Texture.class);
        burningSpritesheet = manager.get("Player Animations/Effect/burningSpritesheet.png", Texture.class);
        armFireBombSpritesheet = manager.get("Player Animations/Effect/armFireBombSpritesheet.png", Texture.class);
        fireBombSpritesheet = manager.get("Player Animations/Effect/fireBombSpritesheet.png", Texture.class);

        energyShieldSpritesheet = manager.get("Player Animations/Effect/energyShieldSpriteSheet.png", Texture.class);

        btnClickSound = manager.get("Sounds/click.ogg", Sound.class);
        fireShot1 = manager.get("Sounds/fireShot1.mp3", Sound.class);
        fireShot2 = manager.get("Sounds/fireShot2.mp3", Sound.class);
        frozenEffect = manager.get("Sounds/IceBlastImpact.wav", Sound.class);
        fireBombExplosion = manager.get("Sounds/fireBomb.wav", Sound.class);
        frostBombExplosion = manager.get("Sounds/frostBomb.wav", Sound.class);
        frostShot = manager.get("Sounds/IceCast.wav", Sound.class);
        menuMusic = manager.get("Music/Action Break.mp3", Music.class);
        gameplayMusic = manager.get("Music/Night Pulse.mp3", Music.class);

        menuBackground = manager.get("UIAssets/menuBackground.png", Texture.class);
        solidBlack = manager.get("UIAssets/Black_1080p.png", Texture.class);

        uiSkin = manager.get("UIAssets/uiskin.json", Skin.class);


        hugeSizeFont = manager.get("joystixHuge.ttf", BitmapFont.class);

        uiSkin.get("default", TextButton.TextButtonStyle.class).font = bigSizeFont;
        uiSkin.get("default", TextField.TextFieldStyle.class).font = bigSizeFont;
        uiSkin.get("default", SelectBox.SelectBoxStyle.class).font =
                uiSkin.get("default", SelectBox.SelectBoxStyle.class).listStyle.font = bigSizeFont;
        uiSkin.add("menu-label", new Label.LabelStyle(bigSizeFont, Color.WHITE), Label.LabelStyle.class);

        ScrollPane.ScrollPaneStyle transpSpStyle =
                new ScrollPane.ScrollPaneStyle(uiSkin.get("default", ScrollPane.ScrollPaneStyle.class));
        transpSpStyle.background = null;
        uiSkin.add("transp-scrollpane", transpSpStyle);

        map1 = manager.get("Maps/level1.tmx", TiledMap.class);
        dungeonMap = manager.get("Maps/level2.tmx", TiledMap.class);
        purpleMap = manager.get("Maps/level3.tmx", TiledMap.class);
    }

    public void dispose() {
        manager.dispose();
    }
}
