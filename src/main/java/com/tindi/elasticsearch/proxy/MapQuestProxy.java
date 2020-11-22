package com.tindi.elasticsearch.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.tindi.elasticsearch.bean.Address;
import com.tindi.elasticsearch.bean.Location;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;


@Component
public class MapQuestProxy {

    private final RestTemplate restTemplate;
    @Value("http://www.mapquestapi.com/geocoding/v1/address?key=XQ1GdpQ6mMy9eOroJzhJwtdRjXlgbbEu&location=")
    private String mapQuestUrlByLocation;

    public MapQuestProxy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public Location getGeoCoordinates(Address address1) {
        Location location = null;
        if (address1.getAddress() == null || address1.getAddress().isEmpty())
            return null;
        try {
            String address = new StringBuffer(address1.getAddress())
                    .append(", ")
                    .append(address1.getCity())
                    .append(" ")
                    .append(address1.getState())
                    .append(" ")
                    .append(address1.getZipCode() != null ? address1.getZipCode() : "")
                    .toString().trim();

            String url = mapQuestUrlByLocation + address;

            HttpEntity entity = new HttpEntity<>(null, new HttpHeaders());
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            JsonNode responseBody = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && null != responseBody) {
                System.out.println(responseBody.path("results").get(0).path("locations"));
                System.out.println(responseBody.path("results").get(0).path("locations").get(0));
                double lat = responseBody.path("results").get(0).path("locations").get(0).path("latLng").path("lat").asDouble();
                double lon = responseBody.path("results").get(0).path("locations").get(0).path("latLng").path("lng").asDouble();
                location = new Location(lat, lon);
            }
            return location;
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

}
