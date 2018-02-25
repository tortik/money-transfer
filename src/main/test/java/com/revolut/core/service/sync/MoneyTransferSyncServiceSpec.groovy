package com.revolut.core.service.sync

import com.revolut.core.dao.AccBalanceMapRepository
import com.revolut.core.dao.AccountBalanceRepository
import com.revolut.core.model.AccountBalance
import com.revolut.core.model.TransferRequest
import spock.lang.Specification

import static java.math.BigDecimal.ONE
import static java.math.BigDecimal.ZERO


class MoneyTransferSyncServiceSpec extends Specification {


    def "basic transfer from first to second account"() {
        given: "request for money transfer from 1 acc to 2 acc for amount = 1 "
            def dao = new AccBalanceMapRepository()
            dao.addNewBalance(new AccountBalance(1, ONE, ZERO))
            dao.addNewBalance(new AccountBalance(2, ONE, ZERO))
            def lockHolder = new SyncMonitorHolder();
            def service = new MoneyTransferSyncService(lockHolder, dao)
            def transferReq = new TransferRequest(1, 2, ONE)
        when: "transferring money"
            service.transfer(transferReq)
        then: "first account amount balance was decreased on 1 "
            service.getBalance(1).moneyAmount == ZERO
        and: "second account amount balance was increased on 1"
            service.getBalance(2).moneyAmount == new BigDecimal(2)
    }


    def "get balance for exist account"() {
        given: "existing account"
            def stubAcc = new AccountBalance(1, ONE, ONE);
            def dao = Mock(AccountBalanceRepository)
            def lockHolder = new SyncMonitorHolder();
            def service = new MoneyTransferSyncService(lockHolder, dao)
        when: "retrieving balance"
            def result = service.getBalance(1)
        then: "dao layer was called"
            1 * dao.getBalance(1) >> stubAcc
        and: "returned account is the same as expected"
            result == stubAcc
    }

    def "get balance for not exist account"() {
        given: "not exist account"
            def dao = Mock(AccountBalanceRepository)
            def lockHolder = new SyncMonitorHolder();
            def service = new MoneyTransferSyncService(lockHolder, dao)
        when: "retrieving balance"
            def result = service.getBalance(1)
        then: "dao layer was called"
            1 * dao.getBalance(1)
        and:"returned balance is null"
            result == null
    }

    def "add balance"() {
        given:
            def dao = Mock(AccountBalanceRepository)
            def lockHolder = new SyncMonitorHolder();
            def service = new MoneyTransferSyncService(lockHolder, dao)
            def newAccBalance = new AccountBalance(1, ONE, ONE)
        when:
            service.addBalance(newAccBalance)
        then:
        1 * dao.addNewBalance(newAccBalance)
    }

    def where(){
        def dao = new AccBalanceMapRepository()
        def lockHolder = new SyncMonitorHolder()
        def service = new MoneyTransferSyncService(lockHolder, dao)
    }
}
