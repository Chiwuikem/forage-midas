package com.jpmc.midascore.kafka;

import com.jpmc.midascore.component.TransactionService;
import com.jpmc.midascore.foundation.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TransactionListener {

    private static final Log log =
            LogFactory.getLog(TransactionListener.class);

    private final List<Transaction> receivedTransactions = new ArrayList<>();
    private final TransactionService transactionService;

    // ✅ constructor injection
    public TransactionListener(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(
            topics = "${general.kafka-topic}",
            groupId = "midas-core-consumer-group"
    )
    public void onTransaction(Transaction transaction) {
        log.info("Received transaction from Kafka: " + transaction);

        synchronized (receivedTransactions) {
            receivedTransactions.add(transaction);
        }

        // ✅ THIS is what Task 3 requires
        transactionService.processIncoming(transaction);
    }

    public List<Transaction> getReceivedTransactions() {
        synchronized (receivedTransactions) {
            return Collections.unmodifiableList(
                    new ArrayList<>(receivedTransactions)
            );
        }
    }
}
