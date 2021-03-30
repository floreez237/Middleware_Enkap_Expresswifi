package com.maviance.middleware_enkap_expresswifi.repositories;

import com.maviance.middleware_enkap_expresswifi.model.request.MiddleWareRequestEntity;
import org.springframework.data.repository.CrudRepository;

public interface RequestRepository extends CrudRepository<MiddleWareRequestEntity, String> {

}
