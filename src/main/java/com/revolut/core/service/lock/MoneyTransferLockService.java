package com.revolut.core.service.lock;


import com.google.inject.Inject;
import com.revolut.core.dao.AccountBalanceRepository;
import com.revolut.core.model.AccountBalance;
import com.revolut.core.model.TransferRequest;
import com.revolut.core.service.MoneyTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.locks.StampedLock;

public class MoneyTransferLockService implements MoneyTransfer {
    private static final Logger LOG = LoggerFactory.getLogger(MoneyTransferLockService.class);

    private AccountBalanceRepository<AccountBalance> repository;
    private LockHolder lockHolder;

    @Inject
    public MoneyTransferLockService(LockHolder lockHolder, AccountBalanceRepository repository) {
        this.lockHolder = lockHolder;
        this.repository = repository;
    }

    @Override
    public void transfer(TransferRequest request) {
        LOG.info("Handling request {} for money transfer", request);
        Long fromAcc = request.getFromAcc();
        Long toAcc = request.getToAcc();

        LockHolder.LockMetaData metaData = lockHolder.getMonitor(fromAcc, toAcc, Math::min);
        StampedLock firstLock = metaData.getLockWrapper().getStampedLock();
        LOG.debug("Getting first lock for account {}", metaData.getAccountNumber());
        long firstLockTS = firstLock.writeLock();

        LockHolder.LockMetaData secondMetaData = lockHolder.getMonitor(fromAcc, toAcc, Math::max);
        StampedLock secondLock = secondMetaData.getLockWrapper().getStampedLock();
        LOG.debug("Getting second lock for account {}", secondMetaData.getAccountNumber());
        long secondLockTS = secondLock.writeLock();

        try {
            AccountBalance senderBalance = repository.getBalance(fromAcc);
            validateBalance(senderBalance, request.getAmount());
            BigDecimal remainingSum = senderBalance.getMoneyAmount().subtract(request.getAmount());
            AccountBalance newSenderBalance = new AccountBalance(senderBalance.getAccNumber(), remainingSum,
                    senderBalance.getBlockedAmount());
            LOG.debug("New Sender balance {}", newSenderBalance);
            AccountBalance receiverBalance = repository.getBalance(toAcc);
            BigDecimal newReceiverSum = receiverBalance.getMoneyAmount().add(request.getAmount());
            AccountBalance newReceiverBalance = new AccountBalance(receiverBalance.getAccNumber(), newReceiverSum,
                    receiverBalance.getBlockedAmount());
            LOG.debug("New Receiver balance {}", newReceiverBalance);
            repository.setBalance(newSenderBalance);
            repository.setBalance(newReceiverBalance);
            LOG.info("Successfully transfer money for request {}", request);
        } finally {
            secondLock.unlockWrite(secondLockTS);
            lockHolder.removeMonitor(secondMetaData);
            firstLock.unlockWrite(firstLockTS);
            lockHolder.removeMonitor(metaData);
        }
    }

    @Override
    public AccountBalance getBalance(long accountId) {
        return repository.getBalance(accountId);
    }

    @Override
    public void addBalance(AccountBalance accountBalance) {
        repository.addNewBalance(accountBalance);
    }
}
