package com.tindi.elasticsearch.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties
@NoArgsConstructor
public class Address {
    String address;
    String city;
    String state;
    String zipCode;
}
