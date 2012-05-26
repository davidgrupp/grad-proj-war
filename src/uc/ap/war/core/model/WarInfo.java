package uc.ap.war.core.model;

import java.util.Vector;

import org.apache.log4j.Logger;

public class WarInfo implements java.io.Serializable {
    private static final long serialVersionUID = -2470409033709728773L;
    private static Logger log = Logger.getLogger(WarInfo.class);
    static WarInfo INS = new WarInfo();
    private Vector<String> otherPlayerIds;

    private WarInfo() {
        // prevent instantiation by others
        otherPlayerIds = new Vector<String>();
    }

    public static WarInfo ins() {
        return INS;
    }

    @SuppressWarnings("unchecked")
    public Vector<String> getOtherPlayerIds() {
        return (Vector<String>) otherPlayerIds.clone();
    }

    public void setOtherPlayerIds(final String[] ids) {
        if (ids == null) {
            return;
        }
        otherPlayerIds.clear();
        for (int i = 0; i < ids.length; i++) {
            otherPlayerIds.add(ids[i]);
        }
    }

}
