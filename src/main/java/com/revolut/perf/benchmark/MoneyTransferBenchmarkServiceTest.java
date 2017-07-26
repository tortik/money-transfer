package com.revolut.perf.benchmark;

import com.revolut.core.dao.AccBalanceMapRepository;
import com.revolut.core.dao.AccountBalanceCASRepository;
import com.revolut.core.dao.AccountBalanceRepository;
import com.revolut.core.model.AccountBalance;
import com.revolut.core.model.TransferRequest;
import com.revolut.core.service.MoneyTransfer;
import com.revolut.core.service.cas.MoneyTransferCASService;
import com.revolut.core.service.lock.LockHolder;
import com.revolut.core.service.lock.MoneyTransferLockService;
import com.revolut.core.service.sync.MoneyTransferSyncService;
import com.revolut.core.service.sync.SyncMonitorHolder;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class MoneyTransferBenchmarkServiceTest {

    public static final long FIRST_ACCOUNT = 123;
    public static final long SECOND_ACCOUNT = 321;
    public static final long THIRD_ACCOUNT = 111;
    public static final List<Long> ACCOUNT_NUMBERS = Stream.of(FIRST_ACCOUNT, SECOND_ACCOUNT, THIRD_ACCOUNT).
            collect(Collectors.toList());

    private MoneyTransfer targetService;

    @Param({"sync", "lock", "cas"})
    private String type;

    @Param({"1", "10"})
    private Integer writersNum;

    @Param({"1", "10"})
    private Integer readersNum;


    private List<TransferRequest> requests;
    private Random random = new Random();

    @Setup
    public void init() {
        requests = Stream.of(new TransferRequest(FIRST_ACCOUNT, SECOND_ACCOUNT, new BigDecimal(10)),
                new TransferRequest(FIRST_ACCOUNT, THIRD_ACCOUNT, new BigDecimal(10)),
                new TransferRequest(SECOND_ACCOUNT, FIRST_ACCOUNT, new BigDecimal(10)),
                new TransferRequest(SECOND_ACCOUNT, THIRD_ACCOUNT, new BigDecimal(10)),
                new TransferRequest(THIRD_ACCOUNT, FIRST_ACCOUNT, new BigDecimal(10)),
                new TransferRequest(THIRD_ACCOUNT, SECOND_ACCOUNT, new BigDecimal(10))).
                collect(Collectors.toList());

        switch (type) {
            case "sync":
                AccountBalanceRepository<AccountBalance> syncRepo = new AccBalanceMapRepository();
                syncRepo.addNewBalance(new AccountBalance(FIRST_ACCOUNT, new BigDecimal(1000000), BigDecimal.ZERO));
                syncRepo.addNewBalance(new AccountBalance(SECOND_ACCOUNT, new BigDecimal(1000000), BigDecimal.ZERO));
                syncRepo.addNewBalance(new AccountBalance(THIRD_ACCOUNT, new BigDecimal(1000000), BigDecimal.ZERO));
                targetService = new MoneyTransferSyncService(new SyncMonitorHolder(), syncRepo);
                break;
            case "lock":
                AccountBalanceRepository<AccountBalance> lockRepo = new AccBalanceMapRepository();
                lockRepo.addNewBalance(new AccountBalance(FIRST_ACCOUNT, new BigDecimal(1000000), BigDecimal.ZERO));
                lockRepo.addNewBalance(new AccountBalance(SECOND_ACCOUNT, new BigDecimal(1000000), BigDecimal.ZERO));
                lockRepo.addNewBalance(new AccountBalance(THIRD_ACCOUNT, new BigDecimal(1000000), BigDecimal.ZERO));
                targetService = new MoneyTransferLockService(new LockHolder(), lockRepo);
                break;
            case "cas":
                AccountBalanceRepository<AtomicReference<AccountBalance>> casRepo = new AccountBalanceCASRepository();
                casRepo.addNewBalance(new AtomicReference<>(new AccountBalance(FIRST_ACCOUNT, new BigDecimal(1000000), BigDecimal.ZERO)));
                casRepo.addNewBalance(new AtomicReference<>(new AccountBalance(SECOND_ACCOUNT, new BigDecimal(1000000), BigDecimal.ZERO)));
                casRepo.addNewBalance(new AtomicReference<>(new AccountBalance(THIRD_ACCOUNT, new BigDecimal(1000000), BigDecimal.ZERO)));
                targetService = new MoneyTransferCASService("100", casRepo);
                break;
        }

    }


    @Benchmark
    public void test(Blackhole bh) throws Exception {

        Stream<CompletableFuture> writeStream = IntStream.range(0, writersNum).
                mapToObj(i -> CompletableFuture.runAsync(() -> {
                    targetService.transfer(requests.get(random.nextInt(6)));
                }));
        Stream<CompletableFuture> readStream = IntStream.range(0, readersNum).
                mapToObj(i -> CompletableFuture.runAsync(() -> {
                    bh.consume(targetService.getBalance(ACCOUNT_NUMBERS.get(random.nextInt(3))));
                }));
        CompletableFuture[] futures = Stream.concat(writeStream, readStream).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).get();
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MoneyTransferBenchmarkServiceTest.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}