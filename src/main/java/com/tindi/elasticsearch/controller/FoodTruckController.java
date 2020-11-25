package com.tindi.elasticsearch.controller;

import com.alibaba.fastjson.JSON;
import com.tindi.elasticsearch.bean.FoodTruck;
import com.tindi.elasticsearch.bean.FoodTrucks;
import com.tindi.elasticsearch.dao.ElasticSearchClient;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.search.SearchHit;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/foodTrucks")
public class FoodTruckController {

    private ElasticSearchClient elasticSearchClient;

    private String indexName = "food_trucks_data";


    public FoodTruckController(ElasticSearchClient elasticSearchClient) {
        this.elasticSearchClient = elasticSearchClient;
    }

    @PostMapping("/{id}")
    public String insertFoodTruck(@RequestBody FoodTruck foodTruck) throws Exception {
        return elasticSearchClient.addDoc(indexName, foodTruck.getId(), foodTruck).toString();
    }

    @GetMapping("/{id}")
    public com.tindi.elasticsearch.controller.SearchResponse getFoodTruckById(@PathVariable String id) throws IOException {
        SearchResponse searchResponse = elasticSearchClient.getDocById(indexName, id);
        return getResults(searchResponse);
    }

    @PutMapping("/{id}")
    public GetResult updateFoodTruckById(@RequestBody FoodTruck foodTruck, @PathVariable String id) {
        try {
            return elasticSearchClient.updateDoc(indexName, foodTruck.getId(), foodTruck.toString()).getGetResult();
        } catch (IOException io) {
            return null;
        }

    }

    @DeleteMapping("/{id}")
    public String deleteFoodTruckById(@PathVariable String id) throws IOException {
        return elasticSearchClient.deleteDoc(indexName, id).toString();
    }

    @RequestMapping(value = "/search/{queryTerm}", method = RequestMethod.GET)
    public com.tindi.elasticsearch.controller.SearchResponse searchFoodTruckTermSearch(@PathVariable String queryTerm) throws IOException {

        SearchResponse searchResponse = elasticSearchClient.multiFieldSearch(indexName, queryTerm);

        return this.getResults(searchResponse);
    }

    @PostMapping(value = "/create/index")
    public boolean createFoodTruckIndex(){
        try {
            CreateIndexResponse createIndexResponse = elasticSearchClient.createIndex();
            return createIndexResponse.isAcknowledged();
        } catch (IOException e) {
            return false;
        }
    }

    private com.tindi.elasticsearch.controller.SearchResponse getResults(SearchResponse searchResponse) {

        Integer total = Math.toIntExact(searchResponse.getHits().getTotalHits().value);

        List<FoodTruck> foodTruckList = new ArrayList<>();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            foodTruckList.add(JSON.parseObject(searchHit.getSourceAsString(), FoodTruck.class));
        }

        return new com.tindi.elasticsearch.controller.SearchResponse(total, foodTruckList, HttpStatus.OK.value());
    }

    @RequestMapping(value = "/bulkload", method = RequestMethod.POST)
    public String bulkLoadIndex(@RequestBody FoodTrucks foodTrucks) {
        try {
            BulkResponse bulkResponse = elasticSearchClient.importAll(indexName, false, foodTrucks);
            if (bulkResponse.hasFailures()) {
                return bulkResponse.buildFailureMessage();
            }
            return "Successful bulk loading";
        } catch (Exception e) {
            return "Something went wrong with bulk loading to elastic search";
        }

    }
}
