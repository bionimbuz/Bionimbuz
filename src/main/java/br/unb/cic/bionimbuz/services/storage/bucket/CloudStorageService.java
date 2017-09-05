/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.storage.bucket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.model.FileInfo;
import br.unb.cic.bionimbuz.plugin.PluginFile;
import br.unb.cic.bionimbuz.services.AbstractBioService;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbuz.services.storage.bucket.CloudStorageMethods.StorageProvider;
import br.unb.cic.bionimbuz.services.storage.bucket.methods.CloudMethodsAmazonGoogle;
import br.unb.cic.bionimbuz.toSort.Listeners;

/**
 *
 * @author Lucas
 */

@Singleton
public class CloudStorageService extends AbstractBioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudStorageService.class);

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("CloudStorageService-%d").build());

    private static String bucketsFolder;
    static private List<BioBucket> bucketList = new ArrayList<>();
    static private CloudStorageMethods methodsInstance;

    @Inject
    public CloudStorageService(final CloudMessageService cms) {

        Preconditions.checkNotNull(cms);

        this.cms = cms;
    }

    @Override
    public void run() {

        LOGGER.info("[CloudStorageService] Checking files on zookeeper");
        this.checkFiles();
        LOGGER.info("[CloudStorageService] Cleaning files on zookeeper");
        this.cleanFiles();
        LOGGER.info("[CloudStorageService] Checking for new files");
        this.checkNewFiles();
    }

    @Override
    public void start(List<Listeners> listeners) {
        this.listeners = listeners;
        if (listeners != null) {
            listeners.add(this);
        }

        // Criando pastas zookeeper para o módulo de armazenamento
        if (!this.cms.getZNodeExist(Path.BUCKETS.getFullPath(), null)) {
            this.cms.createZNode(CreateMode.PERSISTENT, Path.BUCKETS.getFullPath(), null);
        }

        LOGGER.info("[CloudStorageService] Starting");

        // Instance methods object
        methodsInstance = new CloudMethodsAmazonGoogle();

        // Getting parameters from config file
        bucketsFolder = BioNimbusConfig.get().getBucketsFolder();
        CloudStorageMethods.setKeyGoogle(BioNimbusConfig.get().getKeyGoogle());
        CloudStorageMethods.setKeyAmazon(BioNimbusConfig.get().getKeyAmazon());
        CloudStorageMethods.setGcloudFolder(BioNimbusConfig.get().getGcloudFolder());
        CloudStorageMethods.setMyId(BioNimbusConfig.get().getId());

        // Instance all buckets
        LOGGER.info("[CloudStorageService] Instancing Buckets");
        this.InstanceBuckets();

        // Cleaning possible mounted buckets from last execution
        for (final BioBucket aux : bucketList) {
            try {
                methodsInstance.StorageUmount(aux);
            } catch (final Throwable t) {
                // Ignore
            }
        }

        try {
            // Authenticate providers
            LOGGER.info("[CloudStorageService] Authenticating Providers");
            for (final StorageProvider aux : StorageProvider.values()) {
                methodsInstance.StorageAuth(aux);
            }

            // Mount all buckets
            LOGGER.info("[CloudStorageService] Mounting Buckets");
            for (final BioBucket aux : bucketList) {
                methodsInstance.StorageMount(aux);
            }
        } catch (final Throwable t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
            t.printStackTrace();
        }

        final BandwithCheckerBucket checker = new BandwithCheckerBucket();
        checker.start();
        this.executorService.scheduleAtFixedRate(this, 0, 5, TimeUnit.MINUTES);
    }

    @Override
    public void shutdown() {

        try {
            for (final BioBucket aux : bucketList) {
                methodsInstance.StorageUmount(aux);
            }
        } catch (final Throwable t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
        }

        this.listeners.remove(this);
        this.executorService.shutdownNow();
    }

    @Override
    public void getStatus() {
        // TODO Auto-generated method stub
    }

    @Override
    public void verifyPlugins() {

    }

    @Override
    public void event(WatchedEvent eventType) {

    }

    private void InstanceBuckets() {

        BioBucket aux;

        // AMAZON
        // Brazil
        aux = new BioBucket(StorageProvider.AMAZON, "bionimbuz-a-br2", (bucketsFolder + "bionimbuz-a-br2"));
        // Para buckets da AMAZON criados em regiões que não sejam US (Estados Unidos), deve ser setado um endpoint
        // para o Bucket referente a região utilizada
        aux.setEndPoint("s3-sa-east-1.amazonaws.com");
        bucketList.add(aux);
        if (!this.cms.getZNodeExist(Path.NODE_BUCKET.getFullPath(aux.getName()), null)) {
            this.cms.createZNode(CreateMode.PERSISTENT, Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
            // cms.setData(Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
            this.cms.createZNode(CreateMode.PERSISTENT, Path.BUCKET_FILES.getFullPath(aux.getName()), null);
        }
        // //US
        // aux = new BioBucket(StorageProvider.AMAZON, "bionimbuz-a-us", (bucketsFolder + "bionimbuz-a-us"));
        // bucketList.add(aux);
        // if (!cms.getZNodeExist(Path.NODE_BUCKET.getFullPath(aux.getName()), null)) {
        // cms.createZNode(CreateMode.PERSISTENT, Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
        // //cms.setData(Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
        // cms.createZNode(CreateMode.PERSISTENT, Path.BUCKET_FILES.getFullPath(aux.getName()), null);
        // }
        // //EU
        // aux = new BioBucket(StorageProvider.AMAZON, "bionimbuz-a-eu", (bucketsFolder + "bionimbuz-a-eu"));
        // aux.setEndPoint("s3.eu-central-1.amazonaws.com");
        // bucketList.add(aux);
        // if (!cms.getZNodeExist(Path.NODE_BUCKET.getFullPath(aux.getName()), null)) {
        // cms.createZNode(CreateMode.PERSISTENT, Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
        // //cms.setData(Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
        // cms.createZNode(CreateMode.PERSISTENT, Path.BUCKET_FILES.getFullPath(aux.getName()), null);
        // }

        // GOOGLE
        // US
        aux = new BioBucket(StorageProvider.GOOGLE, "bustling-cosmos-151913.appspot.com", (bucketsFolder + "bustling-cosmos-151913.appspot.com"));
        bucketList.add(aux);
        if (!this.cms.getZNodeExist(Path.NODE_BUCKET.getFullPath(aux.getName()), null)) {
            this.cms.createZNode(CreateMode.PERSISTENT, Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
            // cms.setData(Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
            this.cms.createZNode(CreateMode.PERSISTENT, Path.BUCKET_FILES.getFullPath(aux.getName()), null);
        }
        // //EU
        // aux = new BioBucket(StorageProvider.GOOGLE, "bionimbuz-g-eu", (bucketsFolder + "bionimbuz-g-eu"));
        // bucketList.add(aux);
        // if (!cms.getZNodeExist(Path.NODE_BUCKET.getFullPath(aux.getName()), null)) {
        // cms.createZNode(CreateMode.PERSISTENT, Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
        // //cms.setData(Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
        // cms.createZNode(CreateMode.PERSISTENT, Path.BUCKET_FILES.getFullPath(aux.getName()), null);
        // }
    }

    public static BioBucket getBucket(String name) {
        for (final BioBucket aux : bucketList) {
            if (aux.getName().equals(name)) {
                return aux;
            }
        }
        return null;
    }

    public BioBucket findFile(FileInfo file) {

        LOGGER.debug("[CloudStorageService] Looking for file: " + file.getName());

        final List<BioBucket> buckets = new ArrayList<>();

        try {

            final List<String> bucketsPaths = this.cms.getChildren(Path.BUCKETS.getFullPath(), null);

            for (final String bucket_aux : bucketsPaths) {

                LOGGER.debug("[CloudStorageService] Looking file on bucket: " + bucket_aux);
                final ObjectMapper mapper = new ObjectMapper();
                // BioBucket bucket = mapper.readValue(cms.getData(Path.NODE_BUCKET.getFullPath(bucket_aux), null), BioBucket.class);

                final List<String> filesList = this.cms.getChildren(Path.BUCKET_FILES.getFullPath(bucket_aux), null);

                for (final String file_aux : filesList) {

                    LOGGER.debug("[CloudStorageService] File (" + file_aux + ") on Bucket (" + bucket_aux + ")");

                    // FileInfo bucket_file = mapper.readValue(cms.getData(Path.NODE_BUCKET_FILE(bucket_aux, file_aux), null), FileInfo.class);

                    LOGGER.debug("[CloudStorageService] Comparing file (" + file.getName() + ") and file_aux (" + file_aux + ")");
                    if (file_aux.equals(file.getName())) {

                        LOGGER.debug("[CloudStorageService] Match");

                        buckets.add(getBucket(bucket_aux));
                    }
                }
            }
        } catch (final Throwable t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
        }

        if (buckets.isEmpty()) {
            LOGGER.info("[CloudStorageService] File not found on Buckets: " + file.getName());
            return null;
        }

        return getBestBucket(buckets);
    }

    public synchronized void checkFiles() {

        try {

            for (final BioBucket bucket : bucketList) {
                final File dataFolder = new File(bucket.getMountPoint() + "/" + BioNimbusConfig.get().getDataFolder());

                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                } else {

                    if (dataFolder.listFiles() == null) {
                        LOGGER.debug("[CloudStorageService] listfiles() for bucket " + bucket.getName() + " is null");
                    } else {
                        for (final File file : dataFolder.listFiles()) {
                            final PluginFile pluginFile = new PluginFile();
                            pluginFile.setId(file.getName());
                            pluginFile.setName(file.getName());
                            pluginFile.setPath(file.getPath());

                            final List<String> bucketFiles = this.cms.getChildren(Path.BUCKET_FILES.getFullPath(bucket.getName()), null);

                            int hits = 0;
                            for (final String stringFile : bucketFiles) {

                                final ObjectMapper mapper = new ObjectMapper();
                                final PluginFile auxFile = mapper.readValue(this.cms.getData(Path.NODE_BUCKET_FILE.getFullPath(bucket.getName(), stringFile), null), PluginFile.class);

                                if (auxFile.equals(pluginFile)) {
                                    hits += 1;
                                }
                            }

                            if (hits == 0) {
                                this.cms.createZNode(CreateMode.PERSISTENT, Path.NODE_BUCKET_FILE.getFullPath(bucket.getName(), pluginFile.getName()), pluginFile.toString());
                            }
                        }
                    }
                }
            }

        } catch (final Throwable t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
            t.printStackTrace();
        }
    }

    public synchronized void checkNewFiles() {

        final File dataFolder = new File(BioNimbusConfig.get().getDataFolder());

        try {
            for (final File file : dataFolder.listFiles()) {
                if (!fileExistsBuckets(file.getName())) {
                    final int pos = file.getName().lastIndexOf('.');
                    final String toIgnore = ".gstmp";
                    if (pos != -1 && toIgnore.equals(file.getName().substring(pos))) {
                        continue;
                    }

                    final PluginFile pluginFile = new PluginFile();
                    pluginFile.setId(file.getName());
                    pluginFile.setName(file.getName());
                    pluginFile.setPath(file.getPath());

                    final BioBucket dest = getBestBucket(bucketList);

                    LOGGER.info("[CloudStorageService] New file! Uploading " + file.getPath() + " to Bucket " + dest.getName());
                    // TODO: Metodo que deve ser revisto pois faz o upload e para ter o arquivo na pasta data-folder, faz o download dela para a pasta
                    methodsInstance.StorageUploadFile(dest, "/" + BioNimbusConfig.get().getDataFolder(), BioNimbusConfig.get().getDataFolder(), file.getName());
                    methodsInstance.StorageDownloadFile(dest, "/" + BioNimbusConfig.get().getDataFolder(), BioNimbusConfig.get().getDataFolder(), file.getName());
                    this.cms.createZNode(CreateMode.PERSISTENT, Path.NODE_BUCKET_FILE.getFullPath(dest.getName(), pluginFile.getName()), pluginFile.toString());
                }
            }

        } catch (final Exception t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
        }
    }

    public synchronized void cleanFiles() {

        try {

            for (final BioBucket bucket : bucketList) {

                final File dataFolder = new File(bucket.getMountPoint() + "/" + BioNimbusConfig.get().getDataFolder());
                final List<String> bucketFiles = this.cms.getChildren(Path.BUCKET_FILES.getFullPath(bucket.getName()), null);

                for (final String stringFile : bucketFiles) {

                    final ObjectMapper mapper = new ObjectMapper();
                    final PluginFile auxFile = mapper.readValue(this.cms.getData(Path.NODE_BUCKET_FILE.getFullPath(bucket.getName(), stringFile), null), PluginFile.class);

                    int hits = 0;
                    for (final File file : dataFolder.listFiles()) {
                        final PluginFile pluginFile = new PluginFile();
                        pluginFile.setId(file.getName());
                        pluginFile.setName(file.getName());
                        pluginFile.setPath(file.getPath());

                        if (pluginFile.equals(auxFile)) {
                            hits += 1;
                        }
                    }

                    if (hits == 0) {
                        this.cms.delete(Path.NODE_BUCKET_FILE.getFullPath(bucket.getName(), auxFile.getName()));
                    }
                }

            }

        } catch (final IOException t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
        }
    }

    public static boolean checkMode(BioBucket bucket) {

        // TODO testar valores de treshold
        if (bucket.getLatency() < 1) {
            if (bucket.getDlBandwith() > (4 * 1024 * 1024)) { // tresshold = 4 MB
                return true;
            }
        }

        return false;
    }

    public boolean fileExistsZookeeper(String filename) {

        try {

            for (final BioBucket bucket : bucketList) {
                final List<String> bucketFiles = this.cms.getChildren(Path.BUCKET_FILES.getFullPath(bucket.getName()), null);
                for (final String stringFile : bucketFiles) {
                    final ObjectMapper mapper = new ObjectMapper();
                    final PluginFile auxFile = mapper.readValue(this.cms.getData(Path.NODE_BUCKET_FILE.getFullPath(bucket.getName(), stringFile), null), PluginFile.class);
                    if (filename.equals(auxFile.getName())) {
                        return true;
                    }
                }
            }
        } catch (final IOException t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
        }

        return false;
    }

    public static boolean fileExistsBuckets(String filename) {

        try {
            for (final BioBucket bucket : bucketList) {
                final File dataFolder = new File(bucket.getMountPoint() + "/" + BioNimbusConfig.get().getDataFolder());
                for (final File file : dataFolder.listFiles()) {
                    if (filename.equals(file.getName())) {
                        return true;
                    }
                }
            }
        } catch (final Throwable t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
        }

        return false;
    }

    public static List<BioBucket> getBucketList() {
        return bucketList;
    }

    public static BioBucket getBestBucket(List<BioBucket> buckets) {
        BioBucket best = null;

        for (final BioBucket aux : buckets) {
            if (best == null || (aux.getAvgBandwith() > best.getAvgBandwith())) {
                best = aux;
            }
        }

        return best;
    }

    public static void main(String[] args) {
        System.out.println("Iniciando Teste.\n\n");

        System.out.println("\n\nTeste Realizado.");
    }
}
