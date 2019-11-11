package com.mt.strider.repository;

import com.mt.strider.model.Place;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PlaceRepository extends MongoRepository<Place, String> {

    Optional<Place> findByGooglePlaceId(String placeId);
}
