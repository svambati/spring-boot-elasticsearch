package com.tindi.elasticsearch.controller;

import com.tindi.elasticsearch.bean.FoodTruck;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchResponse {
    private Integer searchResultsCount;
    private List<FoodTruck> foodTrucks;
    private Integer code;

}
