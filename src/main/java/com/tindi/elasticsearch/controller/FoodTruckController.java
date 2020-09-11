package com.tindi.elasticsearch.controller;

import com.tindi.elasticsearch.bean.FoodTruck;
import com.tindi.elasticsearch.dao.FoodTruckDao;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/foodTrucks")
public class FoodTruckController {

    private FoodTruckDao foodTruckDao;

    public FoodTruckController(FoodTruckDao foodTruckDao) {
        this.foodTruckDao = foodTruckDao;
    }

    @PostMapping
    public FoodTruck insertFoodTruck(@RequestBody FoodTruck foodTruck) throws Exception{
        return foodTruckDao.insertFoodTruck(foodTruck);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getFoodTruckById(@PathVariable String id){
        return foodTruckDao.getFoodTruckById(id);
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateFoodTruckById(@RequestBody FoodTruck foodTruck, @PathVariable String id){
        return foodTruckDao.updateFoodTruckById(id, foodTruck);
    }

    @DeleteMapping("/{id}")
    public void deleteFoodTruckById(@PathVariable String id){
         foodTruckDao.deleteFoodTruckById(id);
    }
}
