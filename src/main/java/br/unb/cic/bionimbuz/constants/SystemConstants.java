package br.unb.cic.bionimbuz.constants;

public class SystemConstants {
    public static final String FOLDER_DATA = "data/";
    public static final String FOLDER_DATA_SYSTEM = FOLDER_DATA + "system/";
    public static final String FOLDER_CONF = "conf/";
    public static final String FOLDER_LOGS = "logs/";
    public static final String FOLDER_RESOURCES = FOLDER_CONF + "resources/";
    public static final String FOLDER_CREDENTIALS = FOLDER_RESOURCES + "credentials/";
    public static final String FOLDER_INSTANCES = FOLDER_RESOURCES + "instances/";
    public static final String FOLDER_STORAGES = FOLDER_RESOURCES + "storages/";
    public static final String FOLDER_SERVICE = FOLDER_CONF + "services/";
    
    public static final String FILE_NODE = FOLDER_CONF + "node.yaml";
    public static final String FILE_DATABASE = FOLDER_CONF + "database.yaml";  
    
    public static final String FILE_INSTANCES_AMAZON = FOLDER_INSTANCES + "amazon.json";
    public static final String FILE_CREDENTIALS_AMAZON = FOLDER_CREDENTIALS + "amazon.properties";
    
    public static final String FILE_INSTANCES_GOOGLE = FOLDER_INSTANCES + "google.json";
    public static final String FILE_STORAGES_GOOGLE = FOLDER_STORAGES + "google.json";
    public static final String FILE_CREDENTIALS_GOOGLE = FOLDER_CREDENTIALS + "google.json";
    
    public static final String FILE_BANDWITH_TEST = FOLDER_DATA_SYSTEM + "4MBfile";       
}
