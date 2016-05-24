package ru.qrhandshake.qrpos.dto;

import ru.qrhandshake.qrpos.domain.Client;

/**
 * Created by lameroot on 18.05.16.
 */
public class ClientDto extends AuthRequest {

    private String name;
    private String phone;
    private String email;
    private String address;
    private String ip;
    private Location location;

    public ClientDto() {}
    public ClientDto(Client client) {
        if ( null != client ) {
            this.name = client.getName();
            this.phone = client.getPhone();
            this.email = client.getEmail();
            this.address = client.getAddress();
            this.location = new Location(client.getLat(), client.getLon());
            this.ip = client.getIp();
        }
    }

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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
