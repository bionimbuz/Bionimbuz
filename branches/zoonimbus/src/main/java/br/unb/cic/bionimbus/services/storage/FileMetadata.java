package br.unb.cic.bionimbus.services.storage;

import java.util.Date;

import javax.xml.bind.annotation.*;
 
@XmlRootElement(name = "FileMetadata")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "FileMetadata",
        propOrder = {"id", "nome", "dataCriacao", "usuario", "size", "sha1", "formato", "versao", "host"},
        namespace = "http://biofoco3.googlecode.com/svn"
)
public class FileMetadata {
	
    @XmlElement(name = "id", required = true)
    private String id;
    
    @XmlElement(name = "nome", required = true)
    private String nome;
    
    @XmlElement(name = "dataCriacao", required = true)
    private long dataCriacao;
    
    @XmlElement(name = "usuario")
    private String usuario;
    
    @XmlElement(name = "size", required = true)
    private long size;
    
    @XmlElement(name = "sha1", required = true)
    private String sha1;
    
    @XmlElement(name = "formato", required = true)
    private String formato;
    
    @XmlElement(name = "versao", required = true)
    private int versao;
    
    @XmlElement(name = "host", required = true)
    private String host;
    
    // dadosFerramentas

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public long getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(long dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getSha1() {
		return sha1;
	}

	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}

	public String getFormato() {
		return formato;
	}

	public void setFormato(String formato) {
		this.formato = formato;
	}

	public int getVersao() {
		return versao;
	}

	public void setVersao(int versao) {
		this.versao = versao;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
    
    @Override
    public String toString() {
        return "FileMetadata{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", dataCriacao='" + dataCriacao + '\'' +
                ", usuario='" + usuario + '\'' +
                ", size='" + size + '\'' +
                ", sha1='" + sha1 + '\'' +
                ", formato='" + formato + '\'' +
                ", versao='" + versao + '\'' +
                ", host='" + host + 
                '}';
    }
}