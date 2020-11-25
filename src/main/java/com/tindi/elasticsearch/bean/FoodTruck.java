package com.tindi.elasticsearch.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties
@Data
@NoArgsConstructor
public class FoodTruck
{
    private String id;
    private String productName;
    private Address address;
    private Location location;
    private String categoryParentType;
    private String CategoryCodeType;

}
