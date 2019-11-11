package com.mt.strider.service;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.mt.strider.model.Road;
import com.mt.strider.repository.RoadRepository;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoadService {

    private final GeoApiContext geoApiContext;
    private final RoadRepository roadRepository;

    private final Logger logger = Logger.getLogger(RoadService.class);

    public RoadService(GeoApiContext geoApiContext, RoadRepository roadRepository) {
        this.geoApiContext = geoApiContext;
        this.roadRepository = roadRepository;
    }

    public Road getDirectionByOriginAndDestinationPlaceIds(String originPlaceId, String destinationPlaceId) {

        Optional<Road> optionalRoad = roadRepository.findByOriginPlaceIdAndDestinationPlaceId(originPlaceId, destinationPlaceId);
        if (optionalRoad.isPresent()) {
//            logger.debug("Found road with origin id: " + originPlaceId + " and destination id " + destinationPlaceId + " in database, returning...");
            return optionalRoad.get().mode(TravelMode.DRIVING);

        } else {
            logger.debug("No road with origin id: " + originPlaceId + " and destination id " + destinationPlaceId + " in database, fetching...");
            DirectionsResult directionsResult;
            Road road = new Road();
            road.setOriginPlaceId(originPlaceId);
            road.setDestinationPlaceId(destinationPlaceId);

            try {

                for (TravelMode travelMode : TravelMode.values()) {
                    if (!travelMode.equals(TravelMode.UNKNOWN)) {
                        directionsResult = DirectionsApi.newRequest(geoApiContext)
                                .mode(travelMode)
                                .originPlaceId(originPlaceId)
                                .destinationPlaceId(destinationPlaceId)
                                .await();

                        road.mode(travelMode).setDistance(directionsResult.routes[0].legs[0].distance.inMeters);
                        road.mode(travelMode).setDuration(directionsResult.routes[0].legs[0].duration.inSeconds);
                    }
                }

            } catch (ApiException | InterruptedException | IOException e) {
                logger.debug("Error getting road with origin id: " + originPlaceId + " and destination id " + destinationPlaceId + " " + e.getMessage());
                return null;
            }

            roadRepository.save(road);
            logger.debug("Adding road with origin id: " + originPlaceId + " and destination id " + destinationPlaceId + " to database...");

            Road roadOtherWay = new Road(road);
            roadRepository.save(roadOtherWay);
            logger.debug("Adding road with origin id: " + roadOtherWay.getOriginPlaceId() + " and destination id " + roadOtherWay.getDestinationPlaceId() + " to database...");

            return road;

        }

    }

    public List<Road> getAllDirections() {
        return roadRepository.findAll().stream().map(i -> i.mode(TravelMode.DRIVING)).collect(Collectors.toList());
    }

}
