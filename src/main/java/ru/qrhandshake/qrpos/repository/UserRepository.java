package ru.qrhandshake.qrpos.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.User;

/**
 * Created by lameroot on 24.05.16.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    public User findByUsername(String username);
}
