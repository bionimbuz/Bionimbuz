package br.unb.cic.bionimbuz.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Models an User entity
 *
 * @author Vinicius
 */
@Entity
@Table(name = "tb_users")
public class User implements Serializable {
    
    private static final long serialVersionUID = 940213058561091084L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    @Column(unique = true)
    private String login;
    
    private String password;
    
    private String nome;
    
    private String cpf;
    
    private String email;
    
    private String celphone;
    
    private String securityToken;
    
    private Long storageUsage;
    
    @Transient
    private List<Instance> instances;
    
    @Transient
    private List<FileInfo> files;
    
    @Transient
    private List<Workflow> workflows;
    
    public User() {
        super();
    }
    public User(String login) {
        super();
        this.login = login;
    }
    
    public long getId() {
        return this.id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getLogin() {
        return this.login;
    }
    
    public void setLogin(String login) {
        this.login = login;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getNome() {
        return this.nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getCpf() {
        return this.cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCelphone() {
        return this.celphone;
    }
    
    public void setCelphone(String celphone) {
        this.celphone = celphone;
    }
    
    public String getSecurityToken() {
        return this.securityToken;
    }
    
    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }
    
    public Long getStorageUsage() {
        return this.storageUsage;
    }
    
    public void setStorageUsage(Long storageUsage) {
        this.storageUsage = storageUsage;
    }
    
    public void addStorageUsage(Long usage) {
        this.storageUsage += usage;
    }
    
    public void subtractStorageUsage(Long usage) {
        this.storageUsage -= usage;
    }
    
    public List<FileInfo> getFiles() {
        return this.files;
    }
    
    public void setFiles(List<FileInfo> files) {
        this.files = files;
    }
    
    public List<Workflow> getWorkflows() {
        return this.workflows;
    }
    
    public void setWorkflows(List<Workflow> workflows) {
        this.workflows = workflows;
    }
    
    public List<Instance> getInstances() {
        return this.instances;
    }
    
    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }
    
    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (final IOException ex) {
            Logger.getLogger(Workflow.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.login == null ? 0 : this.login.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        if (this.login == null) {
            if (other.login != null) {
                return false;
            }
        } else if (!this.login.equals(other.login)) {
            return false;
        }
        return true;
    }
    
}
