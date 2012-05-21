package uc.ap.war.crypto;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class CertMgrAdapter {
    private static final Logger log = Logger.getLogger(CertMgrAdapter.class);
    private Class certMgrC;
    private Object certMgr;

    public CertMgrAdapter() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        // initiate the certificate related logic
        certMgrC = Class.forName("CertMgr");
        certMgr = certMgrC.newInstance();
    }

    public String getMyHalf() throws ClassNotFoundException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, InstantiationException,
            NoSuchMethodException, SecurityException {
        Method getMyHalf = certMgrC.getMethod("getMyHalf", null);
        String myHalf = (String) getMyHalf.invoke(certMgr);
        return myHalf;
    }

    public static void main(String[] args) throws ClassNotFoundException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, InstantiationException,
            NoSuchMethodException, SecurityException {

        PropertyConfigurator.configure("log4j.properties");
        final CertMgrAdapter adp = new CertMgrAdapter();
        log.debug("My half is " + adp.getMyHalf());
    }

    public String getMyPublicKeyExp() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getMyPublicKeyMod() {
        // TODO Auto-generated method stub
        return null;
    }
}
