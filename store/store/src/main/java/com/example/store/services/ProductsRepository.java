package com.example.store.services;

import org.springframework.data.jpa.repository.*;
import com.example.store.models.*;

public interface ProductsRepository extends JpaRepository<product, Integer>{

}
