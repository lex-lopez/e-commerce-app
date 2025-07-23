package com.alopez.store.users.repositories;

import com.alopez.store.users.entities.Address;
import org.springframework.data.repository.CrudRepository;

public interface AddressRepository extends CrudRepository<Address, Long> {
}