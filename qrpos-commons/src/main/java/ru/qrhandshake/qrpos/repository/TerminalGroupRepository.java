package ru.qrhandshake.qrpos.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.TerminalGroup;

@Repository
public interface TerminalGroupRepository extends CrudRepository<TerminalGroup,Long>{


}
