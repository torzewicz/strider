package com.mt.strider.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class Bound {

    private Boolean inBound;
    private LocalTime originalClosing;
    private Integer day;
    private Boolean isStartingPoint;
}
