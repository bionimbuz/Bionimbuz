package br.unb.cic.bionimbus.services.tarifation.Amazon;

import br.unb.cic.bionimbus.services.tarifation.Utils.RestfulGetter;
import br.unb.cic.bionimbus.services.tarifation.Utils.RestfulGetterBehaviors.PricingGet;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe AmazonDataGet, relacionada à atualização e obtenção de preços da
 * Amazon.
 *
 * @author Gabriel Fritz Sluzala
 */
public class AmazonDataGet {

    private final RestfulGetter getter = new PricingGet();
    private final Map<String, String> config;

    /**
     * Contrutor da classe
     *
     * Server: "info.awsstream.com" AddressOD: "/instances.json?"
     * AddressStorage: "/storage.json?" AddressDataTransfer: "/transfer.json?"
     */
    public AmazonDataGet() {

        this.config = new HashMap<>();
        this.config.put("Server", "info.awsstream.com");
        this.config.put("AddressOD", "/instances.json?");
        this.config.put("FilenameOD", "AmazonInstancesOD.txt");
        this.config.put("AddressStorage", "/storage.json?");
        this.config.put("FilenameStorage", "AmazonStorage.txt");
        this.config.put("AddressDataTransfer", "/transfer.json?");
        this.config.put("FilenameDataTransfer", "AmazonDataTransfer.txt");
        System.out.println("Getting Amazon prices On Demand...");
        String arrayODString = this.getter.get(this.config.get("Server"), this.config.get("AddressOD"));
        System.out.println("Getting Amazon Storage prices...");
        String arrayStorageString = this.getter.get(this.config.get("Server"), this.config.get("AddressStorage"));
        System.out.println("Getting Amazon Data Transfer prices...");
        String arrayDataTransferString = this.getter.get(this.config.get("Server"), this.config.get("AddressDataTransfer"));
        System.out.println("Completed.");
        if (arrayODString != null) {
            System.out.println("Saving Amazon prices On Demand...");
            getter.saveGet(arrayODString, this.config.get("FilenameOD"));
            System.out.println("Saved.");
        }

        if (arrayStorageString != null) {
            System.out.println("Saving Amazon Storage prices...");
            getter.saveGet(arrayStorageString, this.config.get("FilenameStorage"));
            System.out.println("Saved.");
        }

        if (arrayDataTransferString != null) {
            System.out.println("Saving Amazon Data Transfer prices...");
            getter.saveGet(arrayStorageString, this.config.get("FilenameDataTransfer"));
            System.out.println("Saved.");
        }

        System.out.println("GET process completed.");
    }
}
