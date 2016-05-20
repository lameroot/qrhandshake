package ru.qrhandshake.qrpos.domain;

import ru.qrhandshake.qrpos.dto.Location;

import javax.persistence.*;

/**
 * Created by lameroot on 18.05.16.
 */
@Entity
@Table(name = "client")
public class Client {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clientSequence")
    @SequenceGenerator(name = "clientSequence", sequenceName = "seq_client", allocationSize = 1)
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String ip;
    private double lat;
    private double lon;

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

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
