package uc.ap.war.protocol;

import java.util.HashMap;

import org.apache.log4j.Logger;

import uc.ap.war.protocol.exp.PlayerIdException;

public final class WarPlayer implements java.io.Serializable {
    private static final long serialVersionUID = 4044667669615364344L;
    static WarPlayer INS = new WarPlayer();
    static Logger log = Logger.getLogger(WarPlayer.class);
    private String pwd = "default_pw";
    private String newPwd = "";
    private String cookie = "";
    private String id = "";
    private String host = "";
    private Integer port = 0;
    private HashMap<String, Integer> resources;

    // private Integer rupyulars = 0;
    // private HashMap<String, Integer> products;

    private WarPlayer() {
        // prevent instantiation by others
        resources = new HashMap<String, Integer>();
        for (String resName: ProtoKw.RES_NAMES) {
            resources.put(resName, 0);
        }
        // resources.put(ProtoKw.RES_RUPYULARS, 0);
        // resources.put(ProtoKw.RES_STEEL, 0);
        // resources.put(ProtoKw.RES_COPPER, 0);
        // resources.put(ProtoKw.RES_OIL, 0);
        // resources.put(ProtoKw.RES_GLASS, 0);
        // resources.put(ProtoKw.RES_RUBBER, 0);
        // resources.put(ProtoKw.RES_PLASTIC, 0);
        // resources.put(ProtoKw.RES_WEAPONS, 0);
        // resources.put(ProtoKw.RES_VEHICLES, 0);
        // resources.put(ProtoKw.RES_COMPUTERS, 0);
        // products = new HashMap<String, Integer>();
        // products.put("weapons", 0);
        // products.put("vehicles", 0);
        // products.put("computers", 0);
    }

    public void updateResources(final HashMap<String, Integer> newResources) {
        synchronized (resources) {
            resources.putAll(newResources);
        }
    }

    public int getRupyulars() {
        return resources.get(ProtoKw.RES_RUPYULARS);
    }

    public int getSteel() {
        return resources.get(ProtoKw.RES_STEEL);
    }

    public int getCopper() {
        return resources.get(ProtoKw.RES_COPPER);
    }

    public int getOil() {
        return resources.get(ProtoKw.RES_OIL);
    }

    public int getGlass() {
        return resources.get(ProtoKw.RES_GLASS);
    }

    public int getRubber() {
        return resources.get(ProtoKw.RES_RUBBER);
    }

    public int getPlastic() {
        return resources.get(ProtoKw.RES_PLASTIC);
    }

    public int getComputers() {
        return resources.get(ProtoKw.RES_COMPUTERS);
    }

    public int getWeapons() {
        return resources.get(ProtoKw.RES_WEAPONS);
    }

    public int getVehicles() {
        return resources.get(ProtoKw.RES_VEHICLES);
    }

    public static WarPlayer ins() {
        return INS;
    }

    public void setPwd(final String pw) {
        synchronized (pwd) {
            pwd = pw;
        }
        log.debug("password updated: " + pw);
    }

    public void genNewPw() {
        // todo: generate random new pwd
    }

    public String getNewPw() {
        return newPwd;
    }

    public String getPw() {
        return pwd;
    }

    public void setCookie(final String c) {
        synchronized (cookie) {
            cookie = c;
        }
        log.debug("cookie updated: " + c);
    }

    public String getCookie() {
        return cookie;
    }

    public void setId(final String id) {
        synchronized (this.id) {
            this.id = id;
        }
        log.debug("id updated: " + id);
    }

    public String getId() throws PlayerIdException {
        if (id.equals("")) {
            throw new PlayerIdException();
        }
        return id;
    }

    public void setHost(final String host) {
        synchronized (this.host) {
            this.host = host;
        }
        log.debug("host updated: " + host);
    }

    public String getHost() {
        return host;
    }

    public void setPort(final int port) {
        synchronized (this.port) {
            this.port = port;
        }
        log.debug("port updated: " + port);
    }

    public int getPort() {
        return port;
    }
}
