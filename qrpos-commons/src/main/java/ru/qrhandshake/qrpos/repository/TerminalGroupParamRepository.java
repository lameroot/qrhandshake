package ru.qrhandshake.qrpos.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.TerminalGroup;
import ru.qrhandshake.qrpos.domain.TerminalGroupParam;

@Repository
public interface TerminalGroupParamRepository extends CrudRepository<TerminalGroupParam, Long> {
}
