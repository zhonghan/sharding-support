package com.karl.framework.sharding.model;

public class Order {
    private Long id;
    private String desc;
    private String address;
    private String creTime;

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", desc:'" + desc + '\'' +
                ", address:'" + address + '\'' +
                ", creTime:'" + creTime + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public Order setId(Long id) {
        this.id = id;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public Order setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Order setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getCreTime() {
        return creTime;
    }

    public Order setCreTime(String creTime) {
        this.creTime = creTime;
        return this;
    }
}
