package com.rijksmuseum.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class ConfigReader {
    private static Properties properties = new Properties();

    static {
        try {
            FileInputStream stream = new FileInputStream("src/test/resources/config.properties");
            properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

}
