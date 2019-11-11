package com.mt.strider.model;

import com.google.maps.model.TravelMode;
import com.mt.strider.model.other.Bicycling;
import com.mt.strider.model.other.Driving;
import com.mt.strider.model.other.Transit;
import com.mt.strider.model.other.Walking;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "roads")
public class Road {

    @Id
    private String id;

    private String originPlaceId;
    private String destinationPlaceId;

    private TravelMode mode;
    private Walking walking;
    private Driving driving;
    private Transit transit;
    private Bicycling bicycling;

    public Road() {
        this.mode = TravelMode.DRIVING;
        this.walking = new Walking();
        this.driving = new Driving();
        this.transit = new Transit();
        this.bicycling = new Bicycling();
    }

    public Road(Road road) {
        this.originPlaceId = road.destinationPlaceId;
        this.destinationPlaceId = road.originPlaceId;
        this.walking = road.walking;
        this.driving = road.driving;
        this.transit = road.transit;
        this.bicycling = road.bicycling;

    }

    public Road mode(TravelMode travelMode) {
        this.mode = travelMode;
        return this;
    }

    public Long getDistance() {
        if (mode.equals(TravelMode.WALKING)) {
            return this.walking.getDistance();
        } else if (mode.equals(TravelMode.TRANSIT)) {
            return this.transit.getDistance();
        } else if (mode.equals(TravelMode.BICYCLING)) {
            return this.bicycling.getDistance();
        } else return this.driving.getDistance();

    }

    public void setDistance(Long distance) {
        if (mode.equals(TravelMode.WALKING)) {
            this.walking.setDistance(distance);
        } else if (mode.equals(TravelMode.TRANSIT)) {
            this.transit.setDistance(distance);
        } else if (mode.equals(TravelMode.BICYCLING)) {
            this.bicycling.setDistance(distance);
        } else this.driving.setDistance(distance);
    }

    public Long getDuration() {
        if (mode.equals(TravelMode.WALKING)) {
            return this.walking.getDuration();
        } else if (mode.equals(TravelMode.TRANSIT)) {
            return this.transit.getDuration();
        } else if (mode.equals(TravelMode.BICYCLING)) {
            return this.bicycling.getDuration();
        } else return this.driving.getDuration();
    }

    public void setDuration(Long duration) {
        if (mode.equals(TravelMode.WALKING)) {
            this.walking.setDuration(duration);
        } else if (mode.equals(TravelMode.TRANSIT)) {
            this.transit.setDuration(duration);
        } else if (mode.equals(TravelMode.BICYCLING)) {
            this.bicycling.setDuration(duration);
        } else this.driving.setDuration(duration);
    }

}
