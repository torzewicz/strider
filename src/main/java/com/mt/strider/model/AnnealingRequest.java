package com.mt.strider.model;

import com.google.maps.model.TravelMode;
import com.mt.strider.enums.Method;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnealingRequest {

    private Integer numberOfDays;
    private Float amount;
    private ZonedDateTime date;
    private List<PlaceForSearch> places;
    private List<TravelMode> availableTravelModes;
    private Method method;
    private Double timeWeight;
    private Double amountWeight;


}
