package com.tindi.elasticsearch.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {
   String address;
   String city;
   String state;
   String zipCode;
    double latitude;
    double longitude;
}
