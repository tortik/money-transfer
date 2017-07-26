package com.revolut.perf;

import com.revolut.core.dao.AccBalanceMapRepository;
import com.revolut.core.dao.AccountBalanceRepository;
import com.revolut.core.model.AccountBalance;
import com.revolut.core.model.TransferRequest;
import com.revolut.core.service.MoneyTransfer;
import com.revolut.core.service.lock.LockHolder;
import com.revolut.core.service.lock.MoneyTransferLockService;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.LLL_Result;
import org.openjdk.jcstress.infra.results.LL_Result;

import java.math.BigDecimal;


public class MoneyTransferLockServicePerf {

    @State
    public static class ServiceState {
        final MoneyTransfer lockImpl;

        public ServiceState() {
            AccountBalanceRepository<AccountBalance> lockRepo = new AccBalanceMapRepository();
            lockRepo.addNewBalance(new AccountBalance(123, new BigDecimal(1000), BigDecimal.ZERO));
            lockRepo.addNewBalance(new AccountBalance(321, new BigDecimal(1000), BigDecimal.ZERO));
            lockRepo.addNewBalance(new AccountBalance(111, new BigDecimal(1000), BigDecimal.ZERO));
            lockImpl = new MoneyTransferLockService(new LockHolder(), lockRepo);
        }
    }


    @JCStressTest
    @Outcome(id = "800, 1200", expect = Expect.ACCEPTABLE, desc = "Default outcome.")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Case violating atomicity.")
    public static class SendMoneyFromTheSameAccount {

        @Actor
        public void actor1(ServiceState state) {
            TransferRequest tr = new TransferRequest(123, 321, new BigDecimal(100));
            state.lockImpl.transfer(tr);
        }

        @Actor
        public void actor2(ServiceState state) {
            TransferRequest tr = new TransferRequest(123L, 321L, new BigDecimal(100));
            state.lockImpl.transfer(tr);
        }

        @Arbiter
        public void arbiter(ServiceState state, LL_Result result) {
            result.r1 = state.lockImpl.getBalance(123).getMoneyAmount().toString();
            result.r2 = state.lockImpl.getBalance(321).getMoneyAmount().toString();
        }
    }

    @JCStressTest
    @Outcome(id = "1000, 1000", expect = Expect.ACCEPTABLE, desc = "Default outcome.")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Case violating atomicity.")
    public static class SendMoneyFromOpositeAccounts {

        @Actor
        public void actor1(ServiceState state) {
            TransferRequest tr = new TransferRequest(123, 321, new BigDecimal(100));
            state.lockImpl.transfer(tr);
        }

        @Actor
        public void actor2(ServiceState state) {
            TransferRequest tr = new TransferRequest(321, 123, new BigDecimal(100));
            state.lockImpl.transfer(tr);
        }

        @Arbiter
        public void arbiter(ServiceState state, LL_Result result) {
            result.r1 = state.lockImpl.getBalance(123).getMoneyAmount().toString();
            result.r2 = state.lockImpl.getBalance(321).getMoneyAmount().toString();
        }
    }

    @JCStressTest
    @Outcome(id = "1000, 1000, 1000", expect = Expect.ACCEPTABLE, desc = "Default outcome.")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Case violating atomicity.")
    public static class SendMoneyFromTransitiveAccounts {

        @Actor
        public void actor1(ServiceState state) {
            TransferRequest tr = new TransferRequest(123, 321, new BigDecimal(100));
            state.lockImpl.transfer(tr);
        }

        @Actor
        public void actor2(ServiceState state) {
            TransferRequest tr = new TransferRequest(321, 111, new BigDecimal(100));
            state.lockImpl.transfer(tr);
        }

        @Actor
        public void actor3(ServiceState state) {
            TransferRequest tr = new TransferRequest(111, 123, new BigDecimal(100));
            state.lockImpl.transfer(tr);
        }

        @Arbiter
        public void arbiter(ServiceState state, LLL_Result result) {
            result.r1 = state.lockImpl.getBalance(123).getMoneyAmount().toString();
            result.r2 = state.lockImpl.getBalance(321).getMoneyAmount().toString();
            result.r3 = state.lockImpl.getBalance(111).getMoneyAmount().toString();
        }
    }
    @JCStressTest
    @Outcome(id = "900, 1100", expect = Expect.ACCEPTABLE, desc = "Default outcome.")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Case violating atomicity.")
    public static class SendMoneyInsufficientAmountOnAccount {

        @Actor
        public void actor1(ServiceState state) {
            TransferRequest tr = new TransferRequest(123, 321, new BigDecimal(2000));
            try {
                state.lockImpl.transfer(tr);
            }catch (Exception ex){}
        }

        @Actor
        public void actor2(ServiceState state) {
            TransferRequest tr = new TransferRequest(123, 321, new BigDecimal(100));
            state.lockImpl.transfer(tr);
        }

        @Arbiter
        public void arbiter(ServiceState state, LL_Result result) {
            result.r1 = state.lockImpl.getBalance(123).getMoneyAmount().toString();
            result.r2 = state.lockImpl.getBalance(321).getMoneyAmount().toString();
        }
    }

}