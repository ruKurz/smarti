package io.redlink.smarti.repo;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

import io.redlink.smarti.model.config.Configuration;

public interface ConfigurationRepo extends CrudRepository<Configuration, ObjectId> {


    boolean existsByClient(ObjectId client);
    
    Configuration findByClient(ObjectId client);
    
}
