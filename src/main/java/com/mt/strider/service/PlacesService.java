package com.mt.strider.service;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.PlaceDetails;
import com.mt.strider.model.Place;
import com.mt.strider.repository.PlaceRepository;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class PlacesService {

    private final GeoApiContext geoApiContext;
    private final PlaceRepository placeRepository;

    private final Logger logger = Logger.getLogger(PlacesService.class);

    PlacesService(GeoApiContext geoApiContext, PlaceRepository placeRepository) {
        this.geoApiContext = geoApiContext;
        this.placeRepository = placeRepository;
    }

    public Place getPlaceById(String placeId) {

        Optional<Place> optionalPlace = placeRepository.findByGooglePlaceId(placeId);
        if (optionalPlace.isPresent()) {
//            logger.debug("Found place with id: " + placeId + " in database, returning...");
            return optionalPlace.get();
        } else {
            logger.debug("No place with id: " + placeId + " in database, fetching...");
            PlaceDetails placeDetails;

            try {
                placeDetails = PlacesApi.placeDetails(geoApiContext, placeId).await();
            } catch (ApiException | InterruptedException | IOException e) {
                logger.debug("Error getting place with id: " + placeId + " " + e.getMessage());
                return null;
            }

            String city = "";

            AddressComponent[] addressComponents = placeDetails.addressComponents;
            for (AddressComponent addressComponent : addressComponents) {
                if (Arrays.asList(addressComponent.types).contains(AddressComponentType.LOCALITY)) {
                    city = addressComponent.longName;
                }
            }

            Place place = Place.builder()
                    .name(placeDetails.name)
                    .googlePlaceId(placeId)
                    .openingHours(placeDetails.openingHours)
                    .location(placeDetails.geometry.location)
                    .city(city)
                    .build();

            placeRepository.save(place);
            logger.debug("Adding place with id: " + placeId + " to database...");

            return place;
        }
    }

    public List<Place> getAllPlaces() {
        return this.placeRepository.findAll();
    }
}
