package com.mt.strider.utils;

import com.google.maps.model.OpeningHours;
import com.google.maps.model.OpeningHours.Period;
import com.google.maps.model.TravelMode;
import com.mt.strider.model.*;
import com.mt.strider.service.PlacesService;
import com.mt.strider.service.RoadService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AnnealingUtilsComponent {

    private final RoadService roadService;
    private final PlacesService placesService;
    private Random random;

    public AnnealingUtilsComponent(RoadService roadService, PlacesService placesService) {
        this.roadService = roadService;
        this.placesService = placesService;
        this.random = new Random();
    }

    private Road getRoadForPlaces(PlaceForSearch firstPlace, PlaceForSearch secondPlace) {
        return roadService.getDirectionByOriginAndDestinationPlaceIds(firstPlace.getGooglePlaceId(), secondPlace.getGooglePlaceId());
    }

    public static Double getValueForTime(List<Road> roads, Long timeForFunctionPlaces) {
        return roads.stream().map(Road::getDuration).mapToDouble(Long::doubleValue).reduce(0D, Double::sum) + timeForFunctionPlaces;
    }

    public static Double getValueForAmount(List<Road> roads) {
        return roads.stream().map(AnnealingUtilsComponent::getPriceForRoad).reduce(0D, Double::sum);
    }

    private static Double getPriceForRoad(Road road) {

        double averageFuelConsumption = 8D;
        double priceForOneFuelLiter = 5D;

        switch (road.getMode()) {
            case DRIVING:
                return ((road.getDistance() / 1000) / 100D) * averageFuelConsumption * priceForOneFuelLiter;
            case TRANSIT:
                return 3.4;
            case WALKING:
            case BICYCLING:
            default:
                return 0D;

        }
    }

    public List<PlaceForSearch> changePlacesOrder(List<PlaceForSearch> placesForSearch, AnnealingRequest annealingRequest) {
        boolean validSwap;

        do {
            validSwap = true;
            int firstIndex = random.nextInt(placesForSearch.size() - 1) + 1;
            int secondIndex;
            do {
                secondIndex = random.nextInt(placesForSearch.size() - 1) + 1;
            } while (secondIndex == firstIndex);

            Collections.swap(placesForSearch, firstIndex, secondIndex);

            for (int i = 0; i < placesForSearch.size() - 1; i++) {
                if (placesForSearch.get(i).getGooglePlaceId().equals(placesForSearch.get(i + 1).getGooglePlaceId())) {
                    validSwap = false;
                    break;
                }
            }

            if (placesForSearch.get(placesForSearch.size() - 1).getStartPlace()) {
                validSwap = false;
            }

        } while (!validSwap);


        ZonedDateTime date = annealingRequest.getDate();

        int placesListSizeToIterate = placesForSearch.size();
        boolean boundsAreNotOk = true;
        List<Bound> bounds = null;
        Pair<String, String> latestChange = null;
        long numberOfHomesInPlacesList = placesForSearch.stream().filter(placeForSearch -> placeForSearch.getGooglePlaceId().equals(placesForSearch.get(0).getGooglePlaceId())).count();
        while (boundsAreNotOk && placesListSizeToIterate >= 0) {
            bounds = checkBoundsForPlaces(date, placesForSearch);
            List<Boolean> boundsToBooleans = bounds.stream().map(Bound::getInBound).collect(Collectors.toList());

            if (boundsToBooleans.contains(false)) {
                System.out.println("Nie zmieściesz się w boundach");
                int index = boundsToBooleans.indexOf(false);
                if (index == 1) {
                    if (annealingRequest.getNumberOfDays() == 1) {
                        placesForSearch.remove(index);
                    } else {
                        placesForSearch.add(placesForSearch.get(0));
                        placesForSearch.add(placesForSearch.get(1));
                        placesForSearch.remove(1);

                    }
                } else {
                    if (bounds.get(index - 1).getInBound() && !bounds.get(index - 1).getIsStartingPoint() && (bounds.get(index - 1).getOriginalClosing() == null || bounds.get(index - 1).getOriginalClosing().compareTo(bounds.get(index).getOriginalClosing()) >= 0)) {
                        System.out.println("Poprzedni jest później zamknęty, zamenaimny");
                        if (latestChange == null) {
                            latestChange = ImmutablePair.of(placesForSearch.get(index - 1).getGooglePlaceId(), placesForSearch.get(index).getGooglePlaceId());
                            Collections.swap(placesForSearch, index - 1, index);
                        } else {
                            if (wasPreviouslyChanged(placesForSearch, latestChange, index)) {
                                System.out.println("Zamienamy dmo");
                                int indexToSwap = placesForSearch.size() - 1;
                                Collections.swap(placesForSearch, index - 1, indexToSwap);
                                placesListSizeToIterate++;
                                System.out.println("Dodajemy dzień, ale sprwdze czy można");
                                if (annealingRequest.getNumberOfDays() > (int) numberOfHomesInPlacesList) {
                                    placesForSearch.add(placesForSearch.get(0));
                                    placesForSearch.add(placesForSearch.get(indexToSwap));
                                    placesForSearch.remove(indexToSwap);
                                } else {
                                    System.out.println("nie można dodać dnia");
                                }

                            } else {
                                Collections.swap(placesForSearch, index - 1, index);
                            }
                        }
                    } else if (index == placesForSearch.size() - 1) {
                        System.out.println("jest ostatni a poprzedni się nie da");
                        if (annealingRequest.getNumberOfDays() == 1) {
                            System.out.println("Trzeba wywalić");
                            placesForSearch.remove(index);
                        } else {
                            //sprwdz czy już dom nie jest przedostatni
                            if (placesForSearch.get(placesForSearch.size() - 2).getStartPlace()) {
                                System.out.println("WYWALAM");
                                placesForSearch.remove(placesForSearch.size() - 1);
                                placesForSearch.remove(placesForSearch.size() - 1);

                            } else {
                                placesForSearch.add(placesForSearch.get(0));
                                placesForSearch.add(placesForSearch.get(index));
                                placesForSearch.remove(index);
                            }
                        }
                    } else {
                        System.out.println("Jest np. przeedostatni m");
                        if (annealingRequest.getNumberOfDays() == (int) numberOfHomesInPlacesList) {
                            System.out.println("Tyle samo dni co domów");
                            System.out.println("Trzeba wywalić");
                            placesForSearch.remove(index);
                        } else {
                            placesForSearch.add(placesForSearch.get(0));
                            placesForSearch.add(placesForSearch.get(index));
                            placesForSearch.remove(index);
                        }
                    }

                }
            } else {
                boundsAreNotOk = false;
            }
            placesListSizeToIterate--;
        }

        System.out.println("Bounds: " + bounds);
        System.out.println("Bounds are not ok? " + boundsAreNotOk);

        return placesForSearch;

    }

    private boolean wasPreviouslyChanged(List<PlaceForSearch> placesForSearch, Pair<String, String> latestChange, int index) {
        return (latestChange.getKey().equals(placesForSearch.get(index - 1).getGooglePlaceId()) &&
                latestChange.getValue().equals(placesForSearch.get(index).getGooglePlaceId())
        ) || (
                latestChange.getKey().equals(placesForSearch.get(index).getGooglePlaceId()) &&
                        latestChange.getValue().equals(placesForSearch.get(index - 1).getGooglePlaceId()));
    }

    public List<PlaceForSearch> removePlacesIfNumberOfDaysIsNotEnough(AnnealingRequest annealingRequest) {

        List<PlaceForSearch> placesForSearch = annealingRequest.getPlaces();
        ZonedDateTime date = annealingRequest.getDate();

        if (annealingRequest.getNumberOfDays().equals(1)) {
            boolean additionalCheck;
            do {
                additionalCheck = false;
//            Long roadTime = roads.stream().map(Road::getDuration).reduce(0L, Long::sum);
                Long timeInPlaces = placesForSearch.stream().map(PlaceForSearch::getDuration).reduce(0L, Long::sum);

                LocalDateTime latestPlacesClosing = date.toLocalDateTime();
                for (PlaceForSearch placeForSearch : placesForSearch) {
                    if (!placeForSearch.getStartPlace()) {
                        Place place = placesService.getPlaceById(placeForSearch.getGooglePlaceId());
                        OpeningHours openingHours = place.getOpeningHours();
                        if (openingHours != null) {
                            for (Period period : openingHours.periods) {
                                LocalDateTime periodCloseDateTime = period.close.time.atDate(LocalDate.of(latestPlacesClosing.getYear(), latestPlacesClosing.getMonth(), latestPlacesClosing.getDayOfMonth())).atZone(date.getZone()).toLocalDateTime();
                                if (period.close.day.name().equals(date.getDayOfWeek().name()) && periodCloseDateTime.compareTo(latestPlacesClosing) >= 1) {
                                    latestPlacesClosing = periodCloseDateTime;
                                    System.out.println("Najpzniejsze zamykanie: " + period.close.time + " w miesjcu:" + place.getName());
                                }
                            }
                        } else {
                            System.out.println("No opening hours or whole time open, setting to 22");
                            latestPlacesClosing = LocalDateTime.of(latestPlacesClosing.getYear(), latestPlacesClosing.getMonth(), latestPlacesClosing.getDayOfMonth(), 22, 0);
                        }
                    }
                }

                System.out.println(latestPlacesClosing + " vs " + date.toLocalDateTime().plusSeconds(timeInPlaces));
                LocalDateTime localDateTimeTimePlusInPlaces = date.toLocalDateTime().plusSeconds(timeInPlaces);
                if (latestPlacesClosing.compareTo(localDateTimeTimePlusInPlaces) <= 0) {
                    System.out.println("O NIE, trzeba usuna miejsce o najniższym priorytecie");
                    removePlaceWithLowestPriority(placesForSearch);
                    additionalCheck = true;

                }
            } while (additionalCheck);

        }

        return placesForSearch;
    }

    private void removePlaceWithLowestPriority(List<PlaceForSearch> placeForSearches) {
        Integer minPriority = Integer.MAX_VALUE;
        int indexToDelete = 0;
        for (int i = 0; i < placeForSearches.size(); i++) {
            if (minPriority > placeForSearches.get(i).getPriority()) {
                minPriority = placeForSearches.get(i).getPriority();
                indexToDelete = i;
            }
        }

        placeForSearches.remove(indexToDelete);
    }

    @NotNull
    public List<Road> getRoadsFromPlacesForSearchList(List<PlaceForSearch> placeForSearches, AnnealingRequest
            annealingRequest) {
        List<Road> roads = new ArrayList<>();

        List<TravelMode> travelModes = new ArrayList<>();

        if (annealingRequest.getAvailableTravelModes() == null) {
            travelModes.addAll(Arrays.asList(
                    TravelMode.BICYCLING,
                    TravelMode.DRIVING,
                    TravelMode.TRANSIT,
                    TravelMode.WALKING));
        }

        for (int i = 0; i < placeForSearches.size() - 1; i++) {

            Road road = getRoadForPlaces(placeForSearches.get(i), placeForSearches.get(i + 1));
            road.setMode(travelModes.get(random.nextInt(travelModes.size())));

            roads.add(road);
        }
        return roads;
    }

    private List<Bound> checkBoundsForPlaces(ZonedDateTime zonedDateTime, List<PlaceForSearch> placeForSearches) {

        String day = zonedDateTime.getDayOfWeek().name();
        System.out.println();
        System.out.println();
        System.out.println();
        LocalDateTime localDateTime = LocalDateTime.of(zonedDateTime.getYear(), zonedDateTime.getMonth(), zonedDateTime.getDayOfMonth(), zonedDateTime.getHour(), zonedDateTime.getMinute());
        System.out.println("ANALIZYING: " + placeForSearches.stream().map(PlaceForSearch::getGooglePlaceId).collect(Collectors.joining(", ")));
        // TODO: 11/11/2019 Nie zwracaj uagi na godziny otwarcai domu
        List<Bound> responseList = new ArrayList<>();

        int dayOrder = 0;
        for (int i = 0; i < placeForSearches.size(); i++) {
            System.out.println("---------------");
            PlaceForSearch placeForSearch = placeForSearches.get(i);
            System.out.println("ANALIZUIUNG: " + placeForSearch.getGooglePlaceId());
            Long roadDurationTime;
            Long minutesInPlace = 0L;
            if (placeForSearch.getStartPlace()) {
                dayOrder++;
                System.out.println("TO jest punkt startowy nic ciekawego się nie dzieje");
                responseList.add(Bound.builder()
                        .inBound(true)
                        .originalClosing(null)
                        .day(dayOrder)
                        .isStartingPoint(true)
                        .build());
                if (dayOrder > 1) {
                    //ustaw na godzinę otwarcia pierwszeog miejsac następnego


                    Place firstPlaceNextDay = placesService.getPlaceById(placeForSearches.get(i + 1).getGooglePlaceId());
                    final String nameOfDay = day;
                    int hour = 8;
                    int minutes = 0;
                    if (firstPlaceNextDay.getOpeningHours() != null) {
                        Period nextPlaceOpens = Arrays.stream(firstPlaceNextDay.getOpeningHours().periods).filter(period -> period.open.day.name().equals(nameOfDay)).findFirst().orElse(null);
                        if (nextPlaceOpens != null) {
                            hour = nextPlaceOpens.open.time.getHour();
                            minutes = nextPlaceOpens.open.time.getMinute();
                        }
                    }
                    localDateTime = LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth(), localDateTime.getDayOfMonth(), hour, minutes).plusDays(1).plusSeconds(roadService.getDirectionByOriginAndDestinationPlaceIds(placeForSearches.get(i).getGooglePlaceId(), placeForSearches.get(i + 1).getGooglePlaceId()).getDuration());
                    day = localDateTime.getDayOfWeek().name();

                }

            } else {
                System.out.println("MAMY DZIEN: " + " " + dayOrder + " " + day);

                Place place = placesService.getPlaceById(placeForSearch.getGooglePlaceId());

                OpeningHours openingHours = place.getOpeningHours();
                if (openingHours != null) {
                    Period[] periods = openingHours.periods;
                    if (!Arrays.stream(periods).map(period -> period.open.day.name()).collect(Collectors.toList()).contains(day)) {
                        System.out.println("Place: " + place.getName() + " nie jest otwarte w ten dzień");
                        responseList.add(Bound.builder()
                                .inBound(false)
                                .originalClosing(null)
                                .day(dayOrder)
                                .isStartingPoint(false)
                                .build());
                    } else {
                        for (Period period : periods) {
                            if (period.open.day.name().equals(day)) {
                                System.out.println(place.getName() + " Opens: " + period.open.time + " Closes: " + period.close.time);
                                System.out.println("A jest: " + localDateTime);
                                minutesInPlace = placeForSearch.getDuration();
                                if (period.open.time.compareTo(localDateTime.toLocalTime()) <= 0 && period.close.time.compareTo(localDateTime.plusSeconds(minutesInPlace).toLocalTime()) >= 1) {
                                    System.out.println("TAK WTEDY JEST OKm czyli o i nawet wyjdziesz ok, wejście : " + localDateTime + " wyjście: " + localDateTime.plusSeconds(minutesInPlace));
                                    responseList.add(Bound.builder()
                                            .inBound(true)
                                            .originalClosing(period.close.time)
                                            .day(dayOrder)
                                            .isStartingPoint(false)
                                            .build());
                                } else {
                                    System.out.println("O cholibka coś nie tak, przekorczysz albo za wcześnie");
                                    System.out.println("WTEDY: " + localDateTime + " wyjście: " + localDateTime.plusSeconds(minutesInPlace));
                                    responseList.add(Bound.builder()
                                            .inBound(false)
                                            .originalClosing(period.close.time)
                                            .day(dayOrder)
                                            .isStartingPoint(true)
                                            .build());
                                }
                            }
                        }
                    }

                } else {
                    System.out.println(place.getName() + " otwarte cały czas lub nie ma godzin");
                    responseList.add(Bound.builder()
                            .inBound(true)
                            .originalClosing(null)
                            .day(dayOrder)
                            .isStartingPoint(false)
                            .build());
                }
                ;
            }

            if (i + 1 < placeForSearches.size() - 1) {
                roadDurationTime = roadService.getDirectionByOriginAndDestinationPlaceIds(placeForSearches.get(i).getGooglePlaceId(), placeForSearches.get(i + 1).getGooglePlaceId()).getDuration();
            } else {
                roadDurationTime = 0L;
            }

            localDateTime = localDateTime.plusSeconds(minutesInPlace).plusSeconds(roadDurationTime);
        }

        System.out.println(responseList);
        return responseList;
    }

    public List<Place> getPlacesFromPlacesForSearch(List<PlaceForSearch> placeForSearches) {
        List<Place> places = new ArrayList<>();
        for (PlaceForSearch placeForSearch : placeForSearches) {
            places.add(placesService.getPlaceById(placeForSearch.getGooglePlaceId()));
        }
        return places;
    }
}
