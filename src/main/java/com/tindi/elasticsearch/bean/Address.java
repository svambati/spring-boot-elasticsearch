package com.tindi.elasticsearch.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties
public class Address {
    String address;
    String city;
    String state;
    String zipCode;
}
