package com.alopez.store.users.repositories;

import com.alopez.store.users.entities.Profile;
import org.springframework.data.repository.CrudRepository;

public interface ProfileRepository extends CrudRepository<Profile, Long> {
}