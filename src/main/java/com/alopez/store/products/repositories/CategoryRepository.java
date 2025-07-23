package com.alopez.store.products.repositories;

import com.alopez.store.products.dtos.Category;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Byte> {
}