package com.revolut.core.service.sync;

import com.google.inject.Inject;
import com.revolut.core.dao.AccountBalanceRepository;
import com.revolut.core.model.AccountBalance;
import com.revolut.core.model.TransferRequest;
import com.revolut.core.service.MoneyTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Implementation based on synchronized.
 */
public class MoneyTransferSyncService implements MoneyTransfer {
    private static final Logger LOG = LoggerFactory.getLogger(MoneyTransferSyncService.class);

    private AccountBalanceRepository<AccountBalance> repository;
    private SyncMonitorHolder monitorHolder;

    @Inject
    public MoneyTransferSyncService(SyncMonitorHolder monitorHolder,
                                    AccountBalanceRepository repository) {
        this.monitorHolder = monitorHolder;
        this.repository = repository;
    }

    @Override
    public void transfer(TransferRequest request) {
        Long fromAcc = request.getFromAcc();
        Long toAcc = request.getToAcc();

        SyncMonitorHolder.MonitorMetaData firstMetaData = monitorHolder.getMonitor(fromAcc, toAcc, Math::min);
        synchronized (firstMetaData.getMonitor()) {
            SyncMonitorHolder.MonitorMetaData secondMetaData = monitorHolder.getMonitor(fromAcc, toAcc, Math::max);
            synchronized (secondMetaData.getMonitor()) {
                AccountBalance newSenderBalance = getNewSenderAccountBalance(request, fromAcc);
                AccountBalance newRecipientBalance = getNewRecipientAccountBalance(request, toAcc);

                repository.setBalance(newSenderBalance);
                repository.setBalance(newRecipientBalance);
            }
            monitorHolder.removeMonitor(secondMetaData);
        }
        monitorHolder.removeMonitor(firstMetaData);
    }

    @Override
    public AccountBalance getBalance(long accountId) {
        return repository.getBalance(accountId);
    }

    @Override
    public void addBalance(AccountBalance accountBalance) {
        repository.addNewBalance(accountBalance);
    }

    private AccountBalance getNewRecipientAccountBalance(TransferRequest request, Long toAcc) {
        AccountBalance recipientBalance = repository.getBalance(toAcc);
        BigDecimal increasedAmount = recipientBalance.getMoneyAmount().add(request.getAmount());
        return getNewBalance(recipientBalance, increasedAmount);
    }

    private AccountBalance getNewSenderAccountBalance(TransferRequest request, Long fromAcc) {
        AccountBalance senderBalance = repository.getBalance(fromAcc);
        validateBalance(senderBalance, request.getAmount());
        BigDecimal remainAmount = senderBalance.getMoneyAmount().subtract(request.getAmount());
        return getNewBalance(senderBalance, remainAmount);
    }

    private AccountBalance getNewBalance(AccountBalance oldBalance, BigDecimal newAmount) {
        return new AccountBalance(oldBalance.getAccNumber(), newAmount, oldBalance.getBlockedAmount());
    }
}
