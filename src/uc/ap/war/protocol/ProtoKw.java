package uc.ap.war.protocol;

public final class ProtoKw {
    // directives
    public static final String DIR_WAIT = "WAITING";
    public static final String DIR_REQ = "REQUIRE";
    public static final String DIR_RESULT = "RESULT";
    public static final String DIR_CMD_ERR = "COMMAND_ERROR";
    public static final String DIR_QUIT = "QUIT";

    // command arguments
    public static final String CMD_ARG_ACCEPT = "ACCEPT";
    public static final String CMD_ARG_DECLINE = "DECLINE";
    public static final Object CMD_ARG_SUCCEEDED = "SUCCEEDED";
    public static final Object CMD_ARG_FAILED = "FAILED";

    // commands
    public static final String CMD_ID = "IDENT";
    public static final String CMD_PWD = "PASSWORD";
    public static final String CMD_HP = "HOST_PORT";
    public static final String CMD_ALIVE = "ALIVE";
    public static final String CMD_QUIT = "QUIT";
    public static final String CMD_SIGN_OFF = "SIGN_OFF";
    public static final String CMD_PSTAT = "PLAYER_STATUS";
    public static final String CMD_SYNTH = "SYNTHESIZE";
    public static final String CMD_GAME_IDS = "GET_GAME_IDENTS";
    public static final String CMD_RANDOM_PLAYER_HP = "RANDOM_PLAYER_HOST_PORT";
    public static final String CMD_CRACK_HP = "PLAYER_HOST_PORT";
    public static final String CMD_CRACK_COOKIE = "PLAYER_MONITOR_PASSWORD_CRACK";
    public static final String CMD_CRACK_STATUS = "PLAYER_STATUS_CRACK";
    public static final String CMD_TRADE_REQ = "TRADE_REQUEST";
    public static final String CMD_TRADE_REQ_FOR = "for";
    public static final String CMD_TRADE_RESP = "TRADE_RESPONSE";
    public static final String CMD_WAR_DECLARE = "WAR_DECLARE";
    public static final String CMD_WAR_DEFEND = "WAR_DEFEND";
    public static final String CMD_WAR_STATUS = "WAR_STATUS";
    public static final String CMD_WAR_TRUCE_OFFER = "WAR_TRUCE_OFFER";
    public static final String CMD_WAR_TRUCE_RESP = "WAR_TRUCE_RESPONSE";

    // resources
    public static final String RES_COMPUTERS = "computers";
    public static final String RES_VEHICLES = "vehicles";
    public static final String RES_WEAPONS = "weapons";
    public static final String RES_PLASTIC = "plastic";
    public static final String RES_RUBBER = "rubber";
    public static final String RES_GLASS = "glass";
    public static final String RES_OIL = "oil";
    public static final String RES_COPPER = "copper";
    public static final String RES_STEEL = "steel";
    public static final String RES_RUPYULARS = "rupyulars";
    public static final String[] COMP_RES_NAMES = { RES_COMPUTERS,
            RES_VEHICLES, RES_WEAPONS };
    public static final String[] RES_NAMES = { RES_COMPUTERS, RES_VEHICLES,
            RES_WEAPONS, RES_PLASTIC, RES_RUBBER, RES_GLASS, RES_OIL,
            RES_COPPER, RES_STEEL, RES_RUPYULARS };

}
