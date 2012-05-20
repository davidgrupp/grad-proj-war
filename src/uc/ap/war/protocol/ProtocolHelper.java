package uc.ap.war.protocol;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class ProtocolHelper {
    private static final Logger log = Logger.getLogger(ProtocolHelper.class);
    private static final Pattern PAT_CRACK_STATUC = Pattern.compile("^"
            + ProtoKw.CMD_CRACK_STATUS + "\\s+(\\w+)\\s+(\\w+)\\s+(.*)");
    private static final Pattern PAT_RESOURCE = Pattern
            .compile("(\\w+)\\s+(\\d+)\\s*");

    public static String[] parseGameIds(final MsgGroup mg) {
        if (mg == null) {
            return null;
        }
        return mg.getResultStr().split("\\s+");
    }

    public static String[] parseCrackHp(final MsgGroup mg) {
        if (mg == null) {
            return null;
        }
        return mg.getResultStr().split("\\s+");
    }

    public static HashMap<String, Integer> parsePlayerStatResources(
            final MsgGroup mg) {
        final HashMap<String, Integer> hmap = new HashMap<String, Integer>();
        if (mg == null) {
            return hmap;
        }
        populateResources(mg.getResultStr(), hmap);
        return hmap;
    }

    private static void populateResources(final String resourceString,
            final HashMap<String, Integer> hmap) {
        final Matcher mat = PAT_RESOURCE.matcher(resourceString);
        while (mat.find()) {
            final String resName = mat.group(1);
            // we don't need to capture parsing exception here as it is
            // guaranteed by the regular expression used to capture this group
            final int resCount = Integer.parseInt(mat.group(2));
            log.debug("parsed resource: " + resName + " " + resCount);
            hmap.put(resName, resCount);
        }
    }

    public static HashMap<String, Integer> parseTruceResources(final MsgGroup mg) {
        final HashMap<String, Integer> hmap = new HashMap<String, Integer>();
        if (mg == null) {
            return hmap;
        }
        if (!mg.getResultArg().equals(ProtoKw.CMD_WAR_TRUCE_OFFER)) {
            return hmap;
        }
        final int expectedCount = 4;
        final String[] tokens = mg.getResultStr().split("\\s+", expectedCount);
        if (tokens.length == expectedCount) {
            populateResources(tokens[expectedCount - 1], hmap);
        }
        return hmap;
    }

    public static String parseCrackCookie(final MsgGroup mg) {
        // TODO: redesign the model for other players' information, and return a
        // player object with parsed information here
        if (mg == null) {
            return "";
        }
        if (mg.getResultArg().equals(ProtoKw.CMD_CRACK_COOKIE)) {
            final int expectedCount = 3;
            final String[] tokens = mg.getResultStr().split("\\s+",
                    expectedCount);
            if (tokens.length == expectedCount
                    && tokens[1].equals(ProtoKw.CMD_ARG_SUCCEEDED)) {
                return tokens[expectedCount - 1];
            }
        }
        return "";
    }

    public static HashMap<String, Integer> parseCrackStatus(MsgGroup mg) {
        // TODO: redesign the model for other players' information, and return a
        // player object with parsed information here
        final HashMap<String, Integer> hmap = new HashMap<String, Integer>();
        if (mg == null) {
            return hmap;
        }
        if (!mg.getResultArg().equals(ProtoKw.CMD_CRACK_STATUS)) {
            return hmap;
        }
        final int expectedCount = 3;
        final String[] tokens = mg.getResultStr().split("\\s+", expectedCount);
        if (tokens.length == expectedCount
                && tokens[1].equals(ProtoKw.CMD_ARG_SUCCEEDED)) {
            populateResources(tokens[expectedCount - 1], hmap);
        }
        return hmap;
    }

}
