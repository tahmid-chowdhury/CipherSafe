package com.example.ciphersafe;

import java.io.Serializable;
import java.util.UUID;

public class Credential implements Serializable {
    private String id;
    private String serviceName;
    private String username;
    private String password;
    private long lastModified;
    private String userId; // Add userId to associate credentials with specific users

    // Default constructor for new credentials
    public Credential() {
        this.id = UUID.randomUUID().toString();
        this.lastModified = System.currentTimeMillis();
    }

    // Constructor with all fields
    public Credential(String serviceName, String username, String password, String userId) {
        this.id = UUID.randomUUID().toString();
        this.serviceName = serviceName;
        this.username = username;
        this.password = password;
        this.userId = userId;
        this.lastModified = System.currentTimeMillis();
    }

    // Constructor without userId for backward compatibility
    public Credential(String serviceName, String username, String password) {
        this(serviceName, username, password, null);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
        this.lastModified = System.currentTimeMillis();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.lastModified = System.currentTimeMillis();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.lastModified = System.currentTimeMillis();
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Credential that = (Credential) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}