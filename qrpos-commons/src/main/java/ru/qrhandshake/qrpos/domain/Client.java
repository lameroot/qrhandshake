package ru.qrhandshake.qrpos.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;

/**
 * Created by lameroot on 18.05.16.
 */
@Entity
@Table(name = "client")
public class Client implements UserDetails {

    public final static String IP_PARAM = "ip";
    public final static String PHONE_PARAM = "phone";
    public final static String EMAIL_PARAM = "email";


    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clientSequence")
    @SequenceGenerator(name = "clientSequence", sequenceName = "seq_client", allocationSize = 1)
    private Long id;
    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;
    @Column(name = "username", nullable = false, unique = true)
    private String username;//todo: возмоно сделать по телефону
    @Column(name = "password", nullable = true)
    private String password;
    @Column(name = "is_enabled")
    private boolean isEnabled;
    @Column(name = "is_expired")
    private boolean isExpired;
    @Column(name = "is_locked")
    private boolean isLocked;
    @Column(name = "confirm_code")
    private String confirmCode;
    private String name;
    private String phone;
    private String email;
    private String address;
    private Double lat;
    private Double lon;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !isExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !isExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public void setExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public String getConfirmCode() {
        return confirmCode;
    }

    public void setConfirmCode(String confirmCode) {
        this.confirmCode = confirmCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Client{");
        sb.append("id=").append(id);
        sb.append(", username='").append(username).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
