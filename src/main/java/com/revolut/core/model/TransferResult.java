/*
package com.revolut.core.model;

import com.revolut.service.pipeline.step.StepType;


public class TransferResult {

    private TransferRequest request;
    private Status status;

    private TransferResult(TransferRequest request, Status status, StepType stage) {
        this.request = request;
        this.status = status;
        this.stage = stage;
    }

    public static TransferResult failed(TransferRequest request, StepType stage) {
        return new TransferResult(request, Status.FAILED, stage);
    }

    public static TransferResult complete(TransferRequest request) {
        return new TransferResult(request, Status.COMPLETED, null);
    }

    public TransferRequest getRequest() {
        return request;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        COMPLETED, FAILED
    }
}
*/
