package com.revolut.perf;

import com.revolut.core.dao.AccountBalanceCASRepository;
import com.revolut.core.dao.AccountBalanceRepository;
import com.revolut.core.model.AccountBalance;
import com.revolut.core.model.TransferRequest;
import com.revolut.core.service.MoneyTransfer;
import com.revolut.core.service.cas.MoneyTransferCASService;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.LLL_Result;
import org.openjdk.jcstress.infra.results.LL_Result;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;


public class MoneyTransferCASServicePerf {

    @State
    public static class ServiceState {
        final MoneyTransfer casImpl;

        public ServiceState() {
            AccountBalanceRepository<AtomicReference<AccountBalance>> casRepo = new AccountBalanceCASRepository();
            casRepo.addNewBalance(new AtomicReference<>(new AccountBalance(123, new BigDecimal(1000), BigDecimal.ZERO)));
            casRepo.addNewBalance(new AtomicReference<>(new AccountBalance(321, new BigDecimal(1000), BigDecimal.ZERO)));
            casRepo.addNewBalance(new AtomicReference<>(new AccountBalance(111, new BigDecimal(1000), BigDecimal.ZERO)));
            casImpl = new MoneyTransferCASService("100", casRepo);
        }
    }


    @JCStressTest
    @Outcome(id = "800, 1200", expect = Expect.ACCEPTABLE, desc = "Default outcome.")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Case violating atomicity.")
    public static class SendMoneyFromTheSameAccount {

        @Actor
        public void actor1(ServiceState state) {
            TransferRequest tr = new TransferRequest(123, 321, new BigDecimal(100));
            state.casImpl.transfer(tr);
        }

        @Actor
        public void actor2(ServiceState state) {
            TransferRequest tr = new TransferRequest(123L, 321L, new BigDecimal(100));
            state.casImpl.transfer(tr);
        }

        @Arbiter
        public void arbiter(ServiceState state, LL_Result result) {
            result.r1 = state.casImpl.getBalance(123).getMoneyAmount().toString();
            result.r2 = state.casImpl.getBalance(321).getMoneyAmount().toString();
        }
    }

    @JCStressTest
    @Outcome(id = "1000, 1000", expect = Expect.ACCEPTABLE, desc = "Default outcome.")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Case violating atomicity.")
    public static class SendMoneyFromOpositeAccounts {

        @Actor
        public void actor1(ServiceState state) {
            TransferRequest tr = new TransferRequest(123, 321, new BigDecimal(100));
            state.casImpl.transfer(tr);
        }

        @Actor
        public void actor2(ServiceState state) {
            TransferRequest tr = new TransferRequest(321, 123, new BigDecimal(100));
            state.casImpl.transfer(tr);
        }

        @Arbiter
        public void arbiter(ServiceState state, LL_Result result) {
            result.r1 = state.casImpl.getBalance(123).getMoneyAmount().toString();
            result.r2 = state.casImpl.getBalance(321).getMoneyAmount().toString();
        }
    }

    @JCStressTest
    @Outcome(id = "1000, 1000, 1000", expect = Expect.ACCEPTABLE, desc = "Default outcome.")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Case violating atomicity.")
    public static class SendMoneyFromTransitiveAccounts {

        @Actor
        public void actor1(ServiceState state) {
            TransferRequest tr = new TransferRequest(123, 321, new BigDecimal(100));
            state.casImpl.transfer(tr);
        }

        @Actor
        public void actor2(ServiceState state) {
            TransferRequest tr = new TransferRequest(321, 111, new BigDecimal(100));
            state.casImpl.transfer(tr);
        }

        @Actor
        public void actor3(ServiceState state) {
            TransferRequest tr = new TransferRequest(111, 123, new BigDecimal(100));
            state.casImpl.transfer(tr);
        }

        @Arbiter
        public void arbiter(ServiceState state, LLL_Result result) {
            result.r1 = state.casImpl.getBalance(123).getMoneyAmount().toString();
            result.r2 = state.casImpl.getBalance(321).getMoneyAmount().toString();
            result.r3 = state.casImpl.getBalance(111).getMoneyAmount().toString();
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
                state.casImpl.transfer(tr);
            }catch (Exception ex){}
        }

        @Actor
        public void actor2(ServiceState state) {
            TransferRequest tr = new TransferRequest(123, 321, new BigDecimal(100));
            state.casImpl.transfer(tr);
        }

        @Arbiter
        public void arbiter(ServiceState state, LL_Result result) {
            result.r1 = state.casImpl.getBalance(123).getMoneyAmount().toString();
            result.r2 = state.casImpl.getBalance(321).getMoneyAmount().toString();
        }
    }

}