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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.inject.Singleton;

import br.unb.cic.bionimbuz.p2p.Host;
import br.unb.cic.bionimbuz.plugin.PluginService;

@Singleton
public class BioNimbusConfig {

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

    @JsonProperty("server-path")
    private String serverPath = "";

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

    public boolean isRemotehost() {

        final String[] localhosts = new String[] {
                "127.0.0.1",
                "0.0.0.0",
                "localhost"
        };
        final List<String> lstLocals = Arrays.asList(localhosts);
        if (!lstLocals.contains(this.getAddress()) || !lstLocals.contains(this.getAddress())) {
            return true;
        }
        return false;
    }

    public void setRpcProtocol(String rpcProtocol) {
        this.rpcProtocol = rpcProtocol;
    }

    public String getRpcProtocol() {
        return this.rpcProtocol;
    }

    public Integer getRpcPort() {
        return this.rpcPort;
    }

    public void setRpcPort(Integer rpcPort) {
        this.rpcPort = rpcPort;
    }

    public String getZkHosts() {
        return this.zkHosts;
    }

    public void setZkConnString(String zkHosts) {
        this.zkHosts = zkHosts;
    }

    public String getProxyHost() {
        return this.proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return this.proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getInfra() {
        return this.infra;
    }

    public void setInfra(String infra) {
        this.infra = infra;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public Host getHost() {
        return this.host;
    }

    public boolean isClient() {
        return this.client;
    }

    public void setClient(boolean client) {
        this.client = client;
    }

    public void setSeeds(Set<Host> seeds) {
        this.seeds = seeds;
    }

    public Set<Host> getSeeds() {
        return this.seeds;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public double getCostPerGiga() {
        return this.costpergiga;
    }

    public void setCostPerGiga(double costpergiga) {
        this.costpergiga = costpergiga;
    }

    public String getServerPath() {
        return this.serverPath;
    }

    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPlugin() {
        return this.plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public void setZkHosts(String zkHosts) {
        this.zkHosts = zkHosts;
    }

    public int getPrivateCloud() {
        return this.privateCloud;
    }

    public void setPrivateCloud(int privateCloud) {
        this.privateCloud = privateCloud;
    }

    public Double getCost() {
        return this.cost;
    }

    public void SetCost(Double cost) {
        this.cost = cost;
    }

    public String getReferenceFolder() {
        return this.getServerPath() + this.referenceFolder;
    }

    public void setReferenceFolder(String referenceFolder) {
        this.referenceFolder = referenceFolder;
    }

    public String getOutputFolder() {
        return this.getServerPath() + this.outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public String getDataFolder() {
        return this.getServerPath() + this.dataFolder;
    }

    public void setDataFolder(String dataFolder) {
        this.dataFolder = dataFolder;
    }

    public ArrayList<String> getReferences() {
        return this.references;
    }

    public void setReferences(ArrayList<String> references) {
        this.references = references;
    }

    public String getTemporaryUploadedFiles() {
        return this.getServerPath() + this.temporaryUploadedFiles;
    }

    public void setTemporaryUploadedFiles(String temporaryUploadedFiles) {
        this.temporaryUploadedFiles = temporaryUploadedFiles;
    }

    public ArrayList<String> getSupportedFormats() {
        return this.supportedFormats;
    }

    public void setSupportedFormats(ArrayList<String> supportedFormats) {
        this.supportedFormats = supportedFormats;
    }

    public ArrayList<PluginService> getSupportedServices() {
        return this.supportedServices;
    }

    public void setSupportedServices(ArrayList<PluginService> supportedServices) {
        this.supportedServices = supportedServices;
    }

    public String getCredentialsFile() {
        return this.credentialsFile;
    }

    public void setCredentialsFile(String credentialsFile) {
        this.credentialsFile = credentialsFile;
    }

    public String getBucketsFolder() {
        return this.getServerPath() + this.bucketsFolder;
    }

    public void setBucketsFolder(String bucketsFolder) {
        this.bucketsFolder = bucketsFolder;
    }

    public String getGcloudFolder() {
        return this.gcloudFolder;
    }

    public void setGcloudFolder(String gcloudFolder) {
        this.gcloudFolder = gcloudFolder;
    }

    public String getStorageMode() {
        return this.storageMode;
    }

    public void setStorageMode(String storageMode) {
        this.storageMode = storageMode;
    }

    public String getKeyGoogle() {
        return this.getServerPath() + this.keyGoogle;
    }

    public void setKeyGoogle(String keyGoogle) {
        this.keyGoogle = keyGoogle;
    }

    public String getKeyAmazon() {
        return this.getServerPath() + this.keyAmazon;
    }

    public void setKeyAmazon(String keyAmazon) {
        this.keyAmazon = keyAmazon;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("rpc-protocol", rpcProtocol).add("rpc-port", rpcPort).add("zkHosts", zkHosts).add("client", client).add("host", host).add("seeds", seeds)
                .add("private_cloud", privateCloud).add("cost_per_giga", costpergiga).add("server-path", serverPath).add("cost", cost).add("reference-folder", referenceFolder)
                .add("output-folder", outputFolder).add("data-folder", dataFolder).add("references-size", references.size()).add("supported-formats", supportedFormats.size())
                .add("supported-services", supportedServices.size()).add("credentialsFile", credentialsFile).add("tmp-uploaded-files", temporaryUploadedFiles).toString();
    }
}
