package uc.ap.war.core.protocol;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class DirectiveHelper {
    private static final Logger log = Logger.getLogger(DirectiveHelper.class);
    private static final Pattern PAT_HOST_PORT = Pattern
            .compile("(\\w+)\\s+(\\w+)\\s+(\\d+)\\s*");
    private static final Pattern PAT_RESOURCE = Pattern
            .compile("(\\w+)\\s+(\\d+)\\s*");

    public static String parseCrackCookie(final String result) {
        if (result == null) {
            return "";
        }
        final int expectedCount = 3;
        final String[] tokens = result.split("\\s+", expectedCount);
        if (tokens.length == expectedCount
                && tokens[1].equals(ProtoKw.CMD_ARG_SUCCEEDED)) {
            return tokens[expectedCount - 1];
        } else {
            return "";
        }
    }

    public static String[] parseCrackHp(final String result) {
        if (result == null) {
            return null;
        }
        final Matcher mat = PAT_HOST_PORT.matcher(result);
        if (mat.matches()) {
            return new String[] { mat.group(1), mat.group(2), mat.group(3) };
        } else {
            return null;
        }
    }

    public static HashMap<String, Integer> parseCrackStatus(final String result) {
        final HashMap<String, Integer> hmap = new HashMap<String, Integer>();
        if (result == null) {
            return hmap;
        }
        final int expectedCount = 3;
        final String[] tokens = result.split("\\s+", expectedCount);
        if (tokens.length == expectedCount
                && tokens[1].equals(ProtoKw.CMD_ARG_SUCCEEDED)) {
            populateResources(tokens[expectedCount - 1], hmap);
        }
        return hmap;
    }

    public static String[] parseGameIds(final String result) {
        if (result == null) {
            return null;
        }
        return result.split("\\s+");
    }

    public static HashMap<String, Integer> parsePlayerStatResources(
            final String result) {
        final HashMap<String, Integer> hmap = new HashMap<String, Integer>();
        if (result == null) {
            return hmap;
        }
        populateResources(result, hmap);
        return hmap;
    }

    public static HashMap<String, Integer> parseTruceResources(
            final String result) {
        final HashMap<String, Integer> hmap = new HashMap<String, Integer>();
        if (result == null) {
            return hmap;
        }
        final int expectedCount = 4;
        final String[] tokens = result.split("\\s+", expectedCount);
        if (tokens.length == expectedCount) {
            populateResources(tokens[expectedCount - 1], hmap);
        }
        return hmap;
    }

    private static void populateResources(final String resourceStr,
            final HashMap<String, Integer> hmap) {
        // null pointer check is done by calling methods, so we don't need to
        // check here
        final Matcher mat = PAT_RESOURCE.matcher(resourceStr);
        while (mat.find()) {
            final String resName = mat.group(1);
            // we don't need to capture parsing exception here as it is
            // guaranteed by the regular expression used to capture this group
            final int resCount = Integer.parseInt(mat.group(2));
            log.debug("parsed resource: " + resName + " " + resCount);
            hmap.put(resName, resCount);
        }
    }

}
