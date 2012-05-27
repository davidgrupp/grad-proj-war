package uc.ap.war.core.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

import uc.ap.war.core.ex.NoPlayerIdException;
import uc.ap.war.core.ex.PlayerDataNotFoundException;

public class WarModelManager {
    private static final Logger log = Logger.getLogger(WarModelManager.class);
    private static final String DEFAULT_ID = "default";

    public static void storePlayer() throws IOException, NoPlayerIdException {
        synchronized (WarPlayer.INS) {
            FileOutputStream fOut = new FileOutputStream(
                    dataFile(WarPlayer.INS.getId()));
            ObjectOutputStream out = new ObjectOutputStream(fOut);
            out.writeObject(WarPlayer.INS);
            out.close();
            fOut.close();
        }
    }

    public static void loadPlayer(final String playerId)
            throws PlayerDataNotFoundException {
        try {
            FileInputStream fIn = new FileInputStream(dataFile(playerId));
            ObjectInputStream in = new ObjectInputStream(fIn);
            WarPlayer.INS = (WarPlayer) in.readObject();
            in.close();
            fIn.close();
        } catch (IOException | ClassNotFoundException e) {
            log.error(e);
            throw new PlayerDataNotFoundException();
        }
    }

    public static void loadPlayer() throws PlayerDataNotFoundException {
        loadPlayer(DEFAULT_ID);
    }

    private static String dataFile(final String playerId) {
        return "player_" + playerId + ".dat";
    }

}
