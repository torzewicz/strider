package com.mt.strider.model;

import com.google.maps.model.LatLng;
import com.google.maps.model.OpeningHours;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "places")
@AllArgsConstructor
@NoArgsConstructor
public class Place {

    @Id
    private String id;

    private String googlePlaceId;
    private String name;
    private OpeningHours openingHours;
    private LatLng location;
    private String city;

}
