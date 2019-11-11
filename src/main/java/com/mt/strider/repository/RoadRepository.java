package com.mt.strider.repository;

import com.mt.strider.model.Road;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoadRepository extends MongoRepository<Road, String> {

    Optional<Road> findByOriginPlaceIdAndDestinationPlaceId(String originPlaceId, String destinationPlaceId);
}
