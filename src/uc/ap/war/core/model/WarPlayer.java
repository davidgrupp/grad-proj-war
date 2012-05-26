package uc.ap.war.core.model;

import java.util.HashMap;

import org.apache.log4j.Logger;

import uc.ap.war.core.exp.PlayerIdException;
import uc.ap.war.core.protocol.ProtoKw;

public final class WarPlayer implements java.io.Serializable {
    private static final long serialVersionUID = 4044667669615364344L;
    static WarPlayer INS = new WarPlayer();
    static Logger log = Logger.getLogger(WarPlayer.class);

    public static WarPlayer ins() {
        return INS;
    }

    private String pwd = "default_pw";
    private String newPwd = "";
    private String cookie = "";
    private String id = "";
    private String host = "";
    private Integer port = 0;

    private HashMap<String, Integer> resources;

    private WarPlayer() {
        // prevent instantiation by others
        resources = new HashMap<String, Integer>();
        for (String resName : ProtoKw.RES_NAMES) {
            resources.put(resName, 0);
        }
    }

    public void genNewPw() {
        // todo: generate random new pwd
    }

    public int getComputers() {
        return resources.get(ProtoKw.RES_COMPUTERS);
    }

    public String getCookie() {
        return cookie;
    }

    public int getCopper() {
        return resources.get(ProtoKw.RES_COPPER);
    }

    public int getGlass() {
        return resources.get(ProtoKw.RES_GLASS);
    }

    public String getHost() {
        return host;
    }

    public String getId() throws PlayerIdException {
        if (id.equals("")) {
            throw new PlayerIdException();
        }
        return id;
    }

    public String getNewPw() {
        return newPwd;
    }

    public int getOil() {
        return resources.get(ProtoKw.RES_OIL);
    }

    public int getPlastic() {
        return resources.get(ProtoKw.RES_PLASTIC);
    }

    public int getPort() {
        return port;
    }

    public String getPw() {
        return pwd;
    }

    public int getRubber() {
        return resources.get(ProtoKw.RES_RUBBER);
    }

    public int getRupyulars() {
        return resources.get(ProtoKw.RES_RUPYULARS);
    }

    public int getSteel() {
        return resources.get(ProtoKw.RES_STEEL);
    }

    public int getVehicles() {
        return resources.get(ProtoKw.RES_VEHICLES);
    }

    public int getWeapons() {
        return resources.get(ProtoKw.RES_WEAPONS);
    }

    public void setCookie(final String c) {
        synchronized (cookie) {
            cookie = c;
        }
        log.debug("cookie updated: " + c);
    }

    public void setHost(final String host) {
        synchronized (this.host) {
            this.host = host;
        }
        log.debug("host updated: " + host);
    }

    public void setId(final String id) {
        synchronized (this.id) {
            this.id = id;
        }
        log.debug("id updated: " + id);
    }

    public void setPort(final int port) {
        synchronized (this.port) {
            this.port = port;
        }
        log.debug("port updated: " + port);
    }

    public void setPwd(final String pw) {
        synchronized (pwd) {
            pwd = pw;
        }
        log.debug("password updated: " + pw);
    }

    public void updateResources(final HashMap<String, Integer> newResources) {
        synchronized (resources) {
            resources.putAll(newResources);
        }
    }
}
