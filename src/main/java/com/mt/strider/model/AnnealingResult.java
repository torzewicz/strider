package com.mt.strider.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnealingResult {

    private Integer numberOfDays;
    private Double amount;
    private List<RoadResponse> roads;
    private Integer time;
    private List<ShortPlaceInfo> placesOrder;

}
