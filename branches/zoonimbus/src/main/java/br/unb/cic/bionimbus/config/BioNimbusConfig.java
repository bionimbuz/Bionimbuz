package br.unb.cic.bionimbus.config;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.common.base.Objects;
import com.google.inject.Singleton;

import br.unb.cic.bionimbus.p2p.Host;

@Singleton
public class BioNimbusConfig {
	
	private @JsonIgnore String id;
	private @JsonIgnore String infra;
	private @JsonIgnore Set<Host> seeds = new HashSet<Host>();
	private Host host;
	private boolean client = false;	
	private String serverPath = "";
    
    private String proxyHost = "localhost";
    private int proxyPort = 8080;

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public boolean isClient() {
		return client;
	}
	
	public void setInfra(String infra) {
		this.infra = infra;
	}	
	
	public String getInfra() {
		return infra;
	}
	
	public void setHost(Host host) {
		this.host = host;
	}
	
	public Host getHost() {
		return host;
	}

	public void setClient(boolean client) {
		this.client = client;
	}

	public void setSeeds(Set<Host> seeds) {
		this.seeds = seeds;
	}

	public Set<Host> getSeeds() {
		return seeds;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	public String getServerPath() {
		return serverPath;
	}

	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
	                  .add("id", id)	              
	                  .add("client", client)
	                  .add("host", host)
	                  .add("seeds", seeds)
	                  .add(serverPath, serverPath)
		              .toString();
	}


}
