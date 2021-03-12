package com.tindi.elasticsearch.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tindi.elasticsearch.bean.Address;
import com.tindi.elasticsearch.bean.FoodTruck;
import com.tindi.elasticsearch.bean.FoodTrucks;
import com.tindi.elasticsearch.bean.Location;
import com.tindi.elasticsearch.proxy.MapQuestProxy;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@Service
public class ElasticSearchClient {

    private final String INDEX = "food_trucks_data";
    private final String TYPE = "foodTrucks";
    private final String MAPPING = "{\n" +
            "  \"properties\": {\n" +
            "    \"location\": {\n" +
            "      \"type\": \"geo_point\"\n" +
            "    }\n" +
            "  }\n" +
            "}";
    private final String SETTING = "";

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private MapQuestProxy mapQuestProxy;

    private ObjectMapper objectMapper;

    public ElasticSearchClient(ObjectMapper objectMapper, RestHighLevelClient restHighLevelClient) {
        this.objectMapper = objectMapper;
        this.restHighLevelClient = restHighLevelClient;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Do any additional configuration here
        return builder.build();
    }

    public CreateIndexResponse createIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(INDEX);
        if (null != SETTING && !"".equals(SETTING)) {
            request.settings(SETTING, XContentType.JSON);
        }
        if (null != MAPPING && !"".equals(MAPPING)) {
            request.mapping(MAPPING, XContentType.JSON);
        }
        return this.restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
    }



    public AcknowledgedResponse deleteIndex(String... indexNames) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(indexNames);
        return this.restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
    }

    public boolean indexExists(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return this.restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    public IndexResponse addDoc(String indexName, String id, FoodTruck foodTruck) throws IOException {
        Location location = this.getGeoCoordinates(foodTruck.getAddress());

        foodTruck.setLocation(location);
        IndexRequest request = new IndexRequest(indexName);
        if (null != id) {
            request.id(id);
        }
        ObjectMapper obj = new ObjectMapper();
        String jsonStr = obj.writeValueAsString(foodTruck);
        System.out.println(jsonStr);

        request.source(jsonStr, XContentType.JSON);
        return this.restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    public UpdateResponse updateDoc(String indexName, String id, String updateJson) throws IOException {
        UpdateRequest request = new UpdateRequest(indexName, id);
        request.doc(XContentType.JSON, updateJson);
        return this.restHighLevelClient.update(request, RequestOptions.DEFAULT);
    }

    public UpdateResponse updateDoc(String indexName, String id, Map<String, Object> updateMap) throws IOException {
        UpdateRequest request = new UpdateRequest(indexName, id);
        request.doc(updateMap);
        return this.restHighLevelClient.update(request, RequestOptions.DEFAULT);
    }

    public DeleteResponse deleteDoc(String indexName, String id) throws IOException {
        DeleteRequest request = new DeleteRequest(indexName, id);
        return this.restHighLevelClient.delete(request, RequestOptions.DEFAULT);
    }

    public SearchResponse search(String field, String key, int page, int size, String... indexNames) throws IOException {
        SearchRequest request = new SearchRequest(indexNames);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(new MatchQueryBuilder(field, key))
               .from(page)
               .size(size);
        request.source(builder);
        return this.restHighLevelClient.search(request, RequestOptions.DEFAULT);
    }

    public SearchResponse termSearch(String term,
                                     int page,
                                     int size,
                                     double cust_lat,
                                     double radius,
                                     double cust_long,
                                     String... indexNames) throws IOException {
        SearchRequest request = new SearchRequest(indexNames);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        QueryBuilder matchQuery = QueryBuilders.queryStringQuery(term);
        QueryBuilder query = QueryBuilders.boolQuery().must(QueryBuilders.queryStringQuery("*" + term + "*")
                                                                         .field("productName")
                                                                         .field("categoryParentType")
                                                                         .field("categoryCodeType"));

        GeoPoint geoPoint = new GeoPoint(cust_lat, cust_long);

//        GeoDistanceQueryBuilder geoDistanceQueryBuilder = QueryBuilders.geoDistanceQuery("location").point(geoPoint).distance(radius, DistanceUnit.MILES);

        QueryBuilder query1 = QueryBuilders.matchAllQuery();


        QueryBuilder finalQuery = QueryBuilders.boolQuery().must(query);
        builder.query(finalQuery);
        //.postFilter(geoDistanceQueryBuilder);
        builder.sort(new GeoDistanceSortBuilder("location", geoPoint));
        request.source(builder.size(10));

        return this.restHighLevelClient.search(request, RequestOptions.DEFAULT);
    }


    public BulkResponse importAll(String indexName, boolean isAutoId, FoodTrucks foodTrucks) throws IOException {
        if (foodTrucks.getFoodTrucks().size() == 0) {
            throw new IOException("Empty import all request");
        }
        BulkRequest request = new BulkRequest();

        if (isAutoId) {
            for (FoodTruck foodTruck : foodTrucks.getFoodTrucks()) {
                ObjectMapper obj = new ObjectMapper();
                String foodTruckJson = obj.writeValueAsString(foodTruck);
                request.add(new IndexRequest(indexName).source(foodTruckJson, XContentType.JSON));
            }
        } else {
            for (FoodTruck foodTruck : foodTrucks.getFoodTrucks()) {
                ObjectMapper obj = new ObjectMapper();
                String foodTruckJson = obj.writeValueAsString(foodTruck);
                request.add(new IndexRequest(indexName).id(foodTruck.getId()).source(foodTruckJson, XContentType.JSON));
            }
        }
        return this.restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
    }

    public SearchResponse getDocById(String indexName, String id) throws IOException {
        SearchRequest request = new SearchRequest(indexName);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.termQuery("id", id));
        request.source(builder);
        return this.restHighLevelClient.search(request, RequestOptions.DEFAULT);
    }

    private Location getGeoCoordinates(Address address) {

        return mapQuestProxy.getGeoCoordinates(address);
    }
}
