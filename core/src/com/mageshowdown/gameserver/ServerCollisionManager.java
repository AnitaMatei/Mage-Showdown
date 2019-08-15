package com.mageshowdown.gameserver;

import com.badlogic.gdx.physics.box2d.Contact;
import com.mageshowdown.gamelogic.*;
import com.mageshowdown.packets.Network;

import javax.print.attribute.standard.Severity;

public class ServerCollisionManager extends CollisionManager {
    private ServerGameStage gameStage;

    public ServerCollisionManager(ServerGameStage gameStage) {
        this.gameStage = gameStage;
    }


    @Override
    public void beginContact(Contact contact) {
        super.beginContact(contact);
        Object obj1 = contact.getFixtureA().getBody().getUserData(),
                obj2 = contact.getFixtureB().getBody().getUserData();


        if (obj1 instanceof Spell && obj2 instanceof ServerPlayerCharacter) {
            handlePlayerSpellCollision((Spell) obj1, (ServerPlayerCharacter) obj2);
        } else if (obj1 instanceof ServerPlayerCharacter && obj2 instanceof Spell) {
            handlePlayerSpellCollision((Spell) obj2, (ServerPlayerCharacter) obj1);
        }

    }



    private void handlePlayerSpellCollision(Spell spell, ServerPlayerCharacter player) {
        //a player cant damage itself so we check if the spell's owner id is the same as the player's it hit
        if (player.getId() != spell.getOwnerId() && !spell.hasCollided()) {
            Network.PlayerDead packet = new Network.PlayerDead();
            player.damageBy(spell.getDAMAGE_VALUE(), spell);
            spell.setCollided(true);

            //if the player died from the hit
            if (player.getHealth() < 0) {
                //the player might have disconnected by the time the spell hit something
                if (gameStage.getPlayerById(spell.getOwnerId()) != null)
                    gameStage.getPlayerById(spell.getOwnerId()).addKill();
                packet.id = player.getId();
                packet.respawnPos = GameServer.getInstance().generateSpawnPoint(packet.id);
                player.respawn(packet.respawnPos);
                gameStage.getPlayerById(spell.getOwnerId()).raiseScore(1);
                GameServer.getInstance().sendToAllTCP(packet);
            }
        }
    }

}
