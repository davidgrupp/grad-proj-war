package uc.ap.war.core.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class WarModelManager {
	private static final String DEFAULT_ID = "default";

	public static void storePlayer(final String playerId) throws IOException {
		FileOutputStream fOut = new FileOutputStream(dataFile(playerId));
		ObjectOutputStream out = new ObjectOutputStream(fOut);
		out.writeObject(WarPlayer.INS);
		out.close();
		fOut.close();
	}

	public static void storePlayer() throws IOException {
		storePlayer(DEFAULT_ID);
	}

	public static void loadPlayer(final String playerId) throws IOException,
			ClassNotFoundException {
		FileInputStream fIn = new FileInputStream(dataFile(playerId));
		ObjectInputStream in = new ObjectInputStream(fIn);
		WarPlayer.INS = (WarPlayer) in.readObject();
		in.close();
		fIn.close();
	}

	public static void loadPlayer() throws IOException, ClassNotFoundException {
		loadPlayer(DEFAULT_ID);
	}

	private static String dataFile(final String playerId) {
		return "player_" + playerId + ".dat";
	}

}
