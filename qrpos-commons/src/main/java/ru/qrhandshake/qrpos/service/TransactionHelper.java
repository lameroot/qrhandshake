package ru.qrhandshake.qrpos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import java.util.concurrent.Callable;
import static org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRED;

@Service
public class TransactionHelper {

    private static final Logger logger = LoggerFactory.getLogger(TransactionHelper.class);

    @Resource
    private PlatformTransactionManager transactionManager;

    /**
     * Propagation = REQUIRED, isolation = READ_COMMITTED, readOnly = false
     */
    public <R> R execute(Callable<R> callable) {
        TransactionDefinition definition = createDefinition(PROPAGATION_REQUIRED, ISOLATION_READ_COMMITTED, false);
        return executeWithDefinition(callable, definition);
    }

    /**
     * Propagation = REQUIRED, isolation = READ_COMMITTED, readOnly = true
     */
    public <R> R executeReadOnly(Callable<R> callable) {
        TransactionDefinition definition = createDefinition(PROPAGATION_REQUIRED, ISOLATION_READ_COMMITTED, true);
        return executeWithDefinition(callable, definition);
    }

    private static TransactionDefinition createDefinition(int propagation, int isolation, boolean readOnly) {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(propagation);
        definition.setIsolationLevel(isolation);
        definition.setReadOnly(readOnly);
        return definition;
    }

    /**
     * Execute code block in transaction defined by specified TransactionDefinition
     */
    private <R> R executeWithDefinition(Callable<R> callable, TransactionDefinition definition) {
        TransactionStatus status = transactionManager.getTransaction(definition);
        try {
            R result = callable.call();
            transactionManager.commit(status);
            return result;
        } catch (Throwable t) {
            safeRollback(status);
            throw new RuntimeException(t);
        }
    }

    /**
     * Used for hiding possible rollback exception and throw original exception in caller method
     */
    private void safeRollback(TransactionStatus status) {
        try {
            transactionManager.rollback(status);
        } catch (Exception re) {
            logger.error("Rollback exception", re);
        }
    }

}
