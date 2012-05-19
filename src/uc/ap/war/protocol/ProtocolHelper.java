package uc.ap.war.protocol;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class ProtocolHelper {
    private static final Logger log = Logger.getLogger(ProtocolHelper.class);
    private static final Pattern regex = Pattern
            .compile("(\\w+)\\s+(\\d+)\\s*");

    public static String[] parseGameIds(final MsgGroup mg) {
        if (mg == null) {
            return null;
        }
        return mg.getResultStr().split("\\s+");
    }

    public static String[] parseRandomPlayerHp(final MsgGroup mg) {
        if (mg == null) {
            return null;
        }
        return mg.getResultStr().split("\\s+");
    }

    public static HashMap<String, Integer> parseResources(final MsgGroup mg) {
        final HashMap<String, Integer> hmap = new HashMap<String, Integer>();
        if (mg == null) {
            return hmap;
        }
        final Matcher mat = regex.matcher(mg.getResultStr());
        while (mat.find()) {
            final String resName = mat.group(1);
            // we don't need to capture parsing exception here as it is
            // guaranteed by the regular expression used to capture this group
            final int resCount = Integer.parseInt(mat.group(2));
            log.debug("parsed resource: " + resName + " " + resCount);
            hmap.put(resName, resCount);
        }
        return hmap;
    }
}
