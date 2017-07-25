package com.revolut.core.service.cas;


import com.google.inject.Inject;
import com.revolut.core.model.AccountBalance;
import com.revolut.core.model.TransferRequest;
import com.revolut.core.service.MoneyTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;

/**
 * Here we sacrifice atomicity for performance.
 * This example is not full.
 * We should store transaction with status.
 * This flow more like international transfer
 */
public class MoneyTransferCASService implements MoneyTransfer {
    private static final Logger LOG = LoggerFactory.getLogger(MoneyTransferCASService.class);
    @Inject
    private AccountBalanceCASRepository repository;
    private int maxRetries;


    @Override
    public void transfer(TransferRequest request) {
        Long senderAcc = request.getFromAcc();
        Long receiverAcc = request.getToAcc();
        BigDecimal amount = request.getAmount();
        //TODO: could be rewritten with pipeline, and rollback func for every stage
        if (blockAmount(senderAcc, amount)) {
            if (increaseReceiverAmount(receiverAcc, amount)) {
                //we can't do rollback at this point,
                //cause we send money to receiver, and he can see them
                decreaseSenderBlockedAmount(senderAcc, amount);
            } else {
                //TODO: we should handle if some error happens here
                unblockSenderAmount(senderAcc, amount);
            }
        }

    }

    private boolean blockAmount(Long senderAcc, BigDecimal amount) {
        AtomicReference<AccountBalance> balanceRef = repository.getBalance(senderAcc);
        for (int i = 0; i < maxRetries; i++) {
            AccountBalance blockedBalance = Optional.ofNullable(balanceRef).
                    map(AtomicReference::get).
                    filter(it -> it.getMoneyAmount().subtract(amount).compareTo(BigDecimal.ZERO) > 0).
                    map(it -> getBlockedAccountBalance(it, amount)).
                    orElseThrow(() -> new RuntimeException(format("Can't block amount %s for account balance %s", amount, balanceRef.get())));

            if (balanceRef.compareAndSet(balanceRef.get(), blockedBalance)) {
                LOG.info("Successfully block amount {} for account {}", amount, senderAcc);
                return true;
            }
        }
        LOG.warn("Can't block sender money {} for account {} max retries exceeded - {}", amount, senderAcc, maxRetries);
        return false;
    }

    private boolean increaseReceiverAmount(Long receiverAcc, BigDecimal amount) {
        AtomicReference<AccountBalance> balanceRef = repository.getBalance(receiverAcc);
        for (int i = 0; i < maxRetries; i++) {
            AccountBalance increasedBalance = Optional.ofNullable(balanceRef).
                    map(AtomicReference::get).
                    map(it -> increaseAmount(it, amount)).
                    orElseThrow(() -> new RuntimeException(format("Can't put money amount %s to account balance %s", amount, balanceRef.get())));
            if (balanceRef.compareAndSet(balanceRef.get(), increasedBalance)) {
                LOG.info("Successfully subtract amount {} for account {}", amount, receiverAcc);
                return true;
            }
        }
        LOG.warn("Can't send money to receiver {} for account {} max retries exceeded - {}", amount, receiverAcc, maxRetries);
        return false;
    }

    private void decreaseSenderBlockedAmount(Long senderAcc, BigDecimal amount) {
        AtomicReference<AccountBalance> balanceRef = repository.getBalance(senderAcc);
        AccountBalance decreasedBalance;
        do {
            decreasedBalance = Optional.ofNullable(balanceRef).map(AtomicReference::get).
                    map(it -> decreaseBlockedAccountBalance(it, amount)).
                    orElseThrow(() -> new RuntimeException(format("Can't put money amount %s to account balance %s", amount, balanceRef.get())));
        } while (balanceRef.compareAndSet(balanceRef.get(), decreasedBalance));
    }

    private boolean unblockSenderAmount(Long senderAcc, BigDecimal amount) {
        AtomicReference<AccountBalance> balanceRef = repository.getBalance(senderAcc);

        for (int i = 0; i < maxRetries; i++) {
            AccountBalance blockedBalance = Optional.of(balanceRef.get()).
                    filter(it -> it.getBlockedAmount().subtract(amount).compareTo(BigDecimal.ZERO) > 0).
                    map(it -> getUnblockedAccountBalance(it, amount)).
                    orElseThrow(() -> new RuntimeException(format("Can't unblock amount %s for account balance %s", amount, balanceRef.get())));

            if (balanceRef.compareAndSet(balanceRef.get(), blockedBalance)) {
                LOG.info(format("Successfully unblock amount %s for account %d", amount, blockedBalance));
                return true;
            }
        }
        return false;
    }


    private AccountBalance getBlockedAccountBalance(AccountBalance oldBalance, BigDecimal amount) {
        AccountBalance newBalance = new AccountBalance(oldBalance.getAccNumber(),
                oldBalance.getMoneyAmount().subtract(amount), oldBalance.getBlockedAmount().add(amount));
        LOG.debug(format("Map balance %s to new balance %d for amount %s", oldBalance, newBalance, amount));
        return newBalance;
    }

    private AccountBalance increaseAmount(AccountBalance oldBalance, BigDecimal amount) {
        AccountBalance newBalance = new AccountBalance(oldBalance.getAccNumber(), oldBalance.getMoneyAmount().add(amount),
                oldBalance.getBlockedAmount());
        LOG.debug(format("Map balance %s to new balance %d for amount %s", oldBalance, newBalance, amount));
        return newBalance;
    }

    private AccountBalance decreaseBlockedAccountBalance(AccountBalance oldBalance, BigDecimal amount) {
        AccountBalance newBalance = new AccountBalance(oldBalance.getAccNumber(), oldBalance.getMoneyAmount(),
                oldBalance.getBlockedAmount().subtract(amount));
        LOG.debug(format("Map balance %s to new balance %d for amount %s", oldBalance, newBalance, amount));
        return newBalance;
    }

    private AccountBalance getUnblockedAccountBalance(AccountBalance oldBalance, BigDecimal amount) {
        AccountBalance newBalance = new AccountBalance(oldBalance.getAccNumber(),
                oldBalance.getMoneyAmount().add(amount), oldBalance.getBlockedAmount().subtract(amount));

        LOG.debug(format("Map balance %s to new balance %d for amount %s", oldBalance, newBalance, amount));
        return newBalance;
    }
}
