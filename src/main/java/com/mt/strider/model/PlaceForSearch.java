package com.mt.strider.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceForSearch {

    private String googlePlaceId;
    private Integer priority;
    private Long duration;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean startPlace = false;
}
