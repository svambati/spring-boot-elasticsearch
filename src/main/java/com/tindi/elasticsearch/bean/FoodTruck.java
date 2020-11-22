package com.tindi.elasticsearch.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties
@Data
public class FoodTruck
{
    private String id;
    private String productName;
    private Address address;
    private Location location;
    private String categoryParentType;
    private String CategoryCodeType;

}
