package com.mt.strider.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortPlaceInfo {

    private String name;
    private String googlePlaceId;

    public static ShortPlaceInfo getShortPlaceFromCasual(Place place) {
        return ShortPlaceInfo.builder()
                .name(place.getName())
                .googlePlaceId(place.getGooglePlaceId())
                .build();
    }
}
