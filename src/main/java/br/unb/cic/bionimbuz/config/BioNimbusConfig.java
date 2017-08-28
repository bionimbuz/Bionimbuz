/*
 * BioNimbuZ is a federated cloud platform.
 * Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD),
 * Department of Computer Science, University of Brasilia, Brazil
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbuz.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import br.unb.cic.bionimbuz.constants.SystemConstants;
import br.unb.cic.bionimbuz.p2p.Host;
import br.unb.cic.bionimbuz.plugin.PluginService;
import br.unb.cic.bionimbuz.utils.YamlUtils;

public class BioNimbusConfig {
    
    private static BioNimbusConfig instance = null;
    static {
        try {
            instance = YamlUtils.mapToClass(SystemConstants.FILE_NODE, BioNimbusConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private BioNimbusConfig() {}
    
    public static BioNimbusConfig get() {return instance;}

    // @JsonIgnore
    private String id;

    @JsonIgnore
    private String infra;

    @JsonIgnore
    private String address;

    @JsonIgnore
    private String plugin;

    // Retirar seeds
    @JsonIgnore
    private Set<Host> seeds = new HashSet<>();

    @JsonIgnore
    private boolean client = false;

    @JsonProperty("private_cloud")
    private int privateCloud;

    @JsonProperty("rpc_protocol")
    private String rpcProtocol;

    @JsonProperty("rpc_port")
    private Integer rpcPort;

    @JsonProperty("cost_per_giga")
    private double costpergiga;

    @JsonProperty("zookeeper_hosts")
    private String zkHosts;
    // retirar Host
    private Host host;

    @JsonProperty("cost")
    private Double cost;

    private String proxyHost = "localhost";

    private int proxyPort = 8080;

    @JsonProperty("reference-folder")
    private String referenceFolder;

    @JsonProperty("output-folder")
    private String outputFolder;

    @JsonProperty("data-folder")
    private String dataFolder;

    @JsonProperty("tmp-uploaded-files")
    private String temporaryUploadedFiles;

    @JsonProperty("references")
    private ArrayList<String> references;

    @JsonProperty("supported-formats")
    private ArrayList<String> supportedFormats;

    @JsonProperty("supported-services")
    private ArrayList<PluginService> supportedServices;

    @JsonProperty("credentials-file")
    private String credentialsFile;

    @JsonProperty("buckets-folder")
    private String bucketsFolder;

    @JsonProperty("key-google")
    private String keyGoogle;

    @JsonProperty("key-amazon")
    private String keyAmazon;

    @JsonProperty("gcloud-folder")
    private String gcloudFolder;

    @JsonProperty("storage-mode")
    private String storageMode;


    public synchronized String getZkHosts() {
        return this.zkHosts;
    }

    public synchronized void setZkConnString(String zkHosts) {
        this.zkHosts = zkHosts;
    }    

    public synchronized String getAddress() {
        return this.address;
    }

    public synchronized void setAddress(String address) {
        this.address = address;
    }

    public synchronized String getId() {
        return this.id;
    }
    
    public synchronized void setId(String id) {
        this.id = id;
    }
    

    public String getRpcProtocol() {
        return this.rpcProtocol;
    }

    public Integer getRpcPort() {
        return this.rpcPort;
    }

    public String getProxyHost() {
        return this.proxyHost;
    }

    public int getProxyPort() {
        return this.proxyPort;
    }

    public String getInfra() {
        return this.infra == null ? "linux" : this.infra;
    }

    public Host getHost() {
        return this.host;
    }

    public boolean isClient() {
        return this.client;
    }
    
    public Set<Host> getSeeds() {
        return this.seeds;
    }

    public double getCostPerGiga() {
        return this.costpergiga;
    }

    public String getPlugin() {
        return this.plugin;
    }

    public int getPrivateCloud() {
        return this.privateCloud;
    }

    public Double getCost() {
        return this.cost;
    }

    public String getReferenceFolder() {
        return this.referenceFolder;
    }

    public String getOutputFolder() {
        return this.outputFolder;
    }

    public String getDataFolder() {
        return this.dataFolder;
    }

    public ArrayList<String> getReferences() {
        return this.references;
    }

    public String getTemporaryUploadedFiles() {
        return this.temporaryUploadedFiles;
    }

    public ArrayList<String> getSupportedFormats() {
        return this.supportedFormats;
    }

    public ArrayList<PluginService> getSupportedServices() {
        return this.supportedServices;
    }

    public String getCredentialsFile() {
        return this.credentialsFile;
    }

    public String getBucketsFolder() {
        return this.bucketsFolder;
    }

    public String getGcloudFolder() {
        return this.gcloudFolder;
    }

    public String getStorageMode() {
        return this.storageMode;
    }

    public String getKeyGoogle() {
        return this.keyGoogle;
    }

    public String getKeyAmazon() {
        return this.keyAmazon;
    }


    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("rpc-protocol", this.rpcProtocol)
                .add("rpc-port", this.rpcPort)
                .add("zkHosts", this.zkHosts)
                .add("client", this.client)
                .add("host", this.host)
                .add("seeds", this.seeds)
                .add("private_cloud", this.privateCloud)
                .add("cost_per_giga", this.costpergiga)
                .add("cost", this.cost)
                .add("reference-folder", this.referenceFolder)
                .add("output-folder", this.outputFolder)
                .add("data-folder", this.dataFolder)
                .add("references-size", this.references.size())
                .add("supported-formats", this.supportedFormats.size())
                .add("supported-services", this.supportedServices.size())
                .add("credentialsFile", this.credentialsFile)
                .add("tmp-uploaded-files", this.temporaryUploadedFiles).toString();
    }
}
