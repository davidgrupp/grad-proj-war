package uc.ap.war;

import org.apache.log4j.PropertyConfigurator;

public class WarClient {

    public static void main(String[] args) {
        PropertyConfigurator.configure("log4j.properties");

        final ActiveClient ac = new ActiveClient();
        ac.pack();
        ac.setVisible(true);

    }

}
