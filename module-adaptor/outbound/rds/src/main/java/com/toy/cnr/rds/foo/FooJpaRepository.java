package com.toy.cnr.rds.foo;

import com.toy.cnr.rds.foo.entity.FooEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FooJpaRepository extends JpaRepository<FooEntity, Long> {
}
