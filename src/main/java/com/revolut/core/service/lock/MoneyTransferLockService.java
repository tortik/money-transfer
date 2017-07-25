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
    public MoneyTransferLockService(LockHolder lockHolder, AccountBalanceRepository<AccountBalance> repository) {
        this.lockHolder = lockHolder;
        this.repository = repository;
    }

    @Override
    public void transfer(TransferRequest request) {
        Long fromAcc = request.getFromAcc();
        Long toAcc = request.getToAcc();

        LockHolder.LockMetaData metaData = lockHolder.getMonitor(fromAcc, toAcc, Math::min);
        StampedLock firstLock = metaData.getLockWrapper().getStampedLock();
        long firstLockTS = firstLock.writeLock();

        LockHolder.LockMetaData secondMetaData = lockHolder.getMonitor(fromAcc, toAcc, Math::min);
        StampedLock secondLock = secondMetaData.getLockWrapper().getStampedLock();
        long secondLockTS = secondLock.writeLock();

        try {
            AccountBalance senderBalance = repository.getBalance(fromAcc);
            validateBalance(senderBalance, request.getAmount());
            BigDecimal remainingSum = senderBalance.getMoneyAmount().subtract(request.getAmount());
            AccountBalance newSenderBalance = new AccountBalance(senderBalance.getAccNumber(), remainingSum,
                    senderBalance.getBlockedAmount());

            AccountBalance receiverBalance = repository.getBalance(fromAcc);
            AccountBalance newReceiverBalance = new AccountBalance(receiverBalance.getAccNumber(), remainingSum,
                    receiverBalance.getBlockedAmount());

            repository.setBalance(newSenderBalance);
            repository.setBalance(newReceiverBalance);
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
