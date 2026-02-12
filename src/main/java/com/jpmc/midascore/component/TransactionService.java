package com.jpmc.midascore.component;

import com.jpmc.midascore.Incentive;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


import java.util.Optional;

@Service
public class TransactionService {

    private static final Log log =
            LogFactory.getLog(TransactionService.class);

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public TransactionService(UserRepository userRepository,
                              TransactionRecordRepository transactionRecordRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.restTemplate = restTemplate;
    }

    private float getIncentive(Transaction tx) {
        Incentive incentive = restTemplate.postForObject(
                "http://localhost:8080/incentive",
                tx,
                Incentive.class
        );
        return (incentive == null) ? 0.0f : (float) incentive.getAmount();
    }


    @Transactional
    public void processIncoming(Transaction tx) {

        long senderId = tx.getSenderId();
        long recipientId = tx.getRecipientId();
        float amount = tx.getAmount();

        // Validate users exist
        UserRecord sender = userRepository.findById(senderId);
        UserRecord recipient = userRepository.findById(recipientId);

        if (sender == null || recipient == null) {
            log.info("Invalid transaction (unknown user): " + tx);
            return;
        }


        // Validate sufficient balance
        if (sender.getBalance() < amount) {
            log.info("Invalid transaction (insufficient funds): " + tx);
            return;
        }

        // Apply balance updates
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        float incentiveAmount = getIncentive(tx);
        recipient.setBalance(recipient.getBalance() + incentiveAmount);


        userRepository.save(sender);
        userRepository.save(recipient);

        // Record transaction
        TransactionRecord record = new TransactionRecord();
        record.setSender(sender);
        record.setRecipient(recipient);
        record.setAmount(amount);
        record.setIncentive(incentiveAmount);

        transactionRecordRepository.save(record);

        log.info("Processed transaction: " + record);
    }
}
