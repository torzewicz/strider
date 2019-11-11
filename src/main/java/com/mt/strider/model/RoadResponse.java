package com.mt.strider.model;

import com.google.maps.model.TravelMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoadResponse {

    private String originPlaceId;
    private String destinationPlaceId;
    private TravelMode mode;
    private Long distance;
    private Long duration;

    public static RoadResponse getRoadResponseFromRoad(Road road) {
        return RoadResponse.builder()
                .duration(road.getDuration())
                .distance(road.getDistance())
                .mode(road.getMode())
                .originPlaceId(road.getOriginPlaceId())
                .destinationPlaceId(road.getDestinationPlaceId())
                .build();
    }
}
