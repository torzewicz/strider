package com.mt.strider.service;

import com.mt.strider.enums.Method;
import com.mt.strider.model.*;
import com.mt.strider.utils.AnnealingUtilsComponent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.mt.strider.utils.AnnealingUtilsComponent.*;

@Service
public class AnnealingService {

    private final AnnealingUtilsComponent annealingUtilsComponent;
    private final PlacesService placesService;

    private final Logger logger = Logger.getLogger(AnnealingService.class);

    public AnnealingService(AnnealingUtilsComponent annealingUtilsComponent, PlacesService placesService) {
        this.annealingUtilsComponent = annealingUtilsComponent;
        this.placesService = placesService;
    }

    public AnnealingResult performAnnealing(AnnealingRequest annealingRequest) {
        if (annealingRequest.getPlaces() == null) {
            return null;
        }


        List<PlaceForSearch> currentPlacesForSearch = annealingUtilsComponent.removePlacesIfNumberOfDaysIsNotEnough(annealingRequest);

        List<Place> places = annealingUtilsComponent.getPlacesFromPlacesForSearch(currentPlacesForSearch);

        double annealingTemperature = places.size() * 10000D;

        Pair<Double, List<Road>> initialFunctionResult = getFunctionValueForPlacesAndMethod(
                annealingUtilsComponent.getRoadsFromPlacesForSearchList(currentPlacesForSearch, annealingRequest),
                annealingRequest);

        Double functionValue = initialFunctionResult.getKey();

        List<Place> finalPlacesOrder = new ArrayList<>();
        List<Road> finalRoadsOrder = initialFunctionResult.getValue();

        List<Place> possiblePlacesOrder;

        int currentNumberOfDays = 1;

        for (int i = 0; i <= 10; i++) {
            currentPlacesForSearch = annealingUtilsComponent.changePlacesOrder(currentPlacesForSearch, annealingRequest);

            long count = currentPlacesForSearch.stream().filter(placeForSearch -> placeForSearch.getGooglePlaceId().equals(annealingRequest.getPlaces().get(0).getGooglePlaceId())).count();

//            if ((int) count > currentNumberOfDays) {
//                currentNumberOfDays = (int) count;
//            }

            possiblePlacesOrder = annealingUtilsComponent.getPlacesFromPlacesForSearch(currentPlacesForSearch);

            Pair<Double, List<Road>> functionResult = getFunctionValueForPlacesAndMethod(
                    annealingUtilsComponent.getRoadsFromPlacesForSearchList(currentPlacesForSearch, annealingRequest),
                    annealingRequest);

            Double possibleValue = functionResult.getKey();

            if (possibleValue < functionValue || count > currentNumberOfDays) {
                functionValue = possibleValue;
                finalPlacesOrder = new ArrayList<>(possiblePlacesOrder);
                finalRoadsOrder = functionResult.getValue();
                currentNumberOfDays = (int) count;
            } else if (Math.pow(Math.E, (functionValue - possibleValue) / annealingTemperature) > Math.random()) {
                logger.debug("Taking bad solution");
                functionValue = possibleValue;
                finalPlacesOrder = new ArrayList<>(possiblePlacesOrder);
                finalRoadsOrder = functionResult.getValue();

            }

            annealingTemperature = annealingTemperature * Math.random();
            logger.debug("Current places order: " + finalPlacesOrder.stream().map(Place::getName).collect(Collectors.joining(" | ")));

//            logger.debug("Temperature: " + annealingTemperature + " possible value: " + possibleValue + " value: " + value);

        }

        List<RoadResponse> roadResponses = finalRoadsOrder.stream().map(RoadResponse::getRoadResponseFromRoad).collect(Collectors.toList());

        Long spentTime = 0L;

        for (PlaceForSearch placeForSearch : annealingRequest.getPlaces()) {
            if (finalPlacesOrder.stream().map(Place::getGooglePlaceId).collect(Collectors.toList()).contains(placeForSearch.getGooglePlaceId())) {
                spentTime += placeForSearch.getDuration();
            }
        }

        Integer time = getValueForTime(finalRoadsOrder, spentTime).intValue();
        int numberOfDays = (time / 3600) % 8;

        return AnnealingResult.builder()
                .placesOrder(finalPlacesOrder.stream().map(ShortPlaceInfo::getShortPlaceFromCasual).collect(Collectors.toList()))
                .roads(roadResponses)
                .amount(getValueForAmount(finalRoadsOrder))
                .time(time)
                .numberOfDays(numberOfDays != 0 ? numberOfDays : 1)
                .build();
    }

    private Pair<Double, List<Road>> getFunctionValueForPlacesAndMethod(List<Road> roads, AnnealingRequest annealingRequest) {

        //check if this makes sense!!!
        Method method = annealingRequest.getMethod();
        Long timeForFunctionPlaces = annealingRequest.getPlaces().stream().map(PlaceForSearch::getDuration).reduce(0L, Long::sum);

        if (method.equals(Method.TIME)) {
            // TODO: 2019-10-26 Add time spent in places
            return ImmutablePair.of(getValueForTime(roads, timeForFunctionPlaces), roads);
        } else if (method.equals(Method.AMOUNT)) {
            // TODO: 2019-10-26 Add amount spent in places
            return ImmutablePair.of(getValueForAmount(roads), roads);
        } else {
            return ImmutablePair.of(
                    getValueForTime(roads, timeForFunctionPlaces) * annealingRequest.getTimeWeight() +
                            getValueForAmount(roads) * annealingRequest.getAmountWeight(),
                    roads
            );
        }
    }

}
