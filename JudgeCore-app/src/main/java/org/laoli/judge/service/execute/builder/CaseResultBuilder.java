package org.laoli.judge.service.execute.builder;

import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.SimpleResult;
import org.laoli.judge.util.ProcessUtils;

public class CaseResultBuilder {
    private SimpleResult status;
    private String message;
    private long executionTime;
    private Long memoryUsed;
    private String actualOutput;
    private String expectedOutput;
    private String input;

    private CaseResultBuilder() {
    }

    public static CaseResultBuilder builder() {
        return new CaseResultBuilder();
    }

    public CaseResultBuilder status(SimpleResult status) {
        this.status = status;
        return this;
    }

    public CaseResultBuilder message(String message) {
        this.message = message;
        return this;
    }

    public CaseResultBuilder executionTime(long executionTime) {
        this.executionTime = executionTime;
        return this;
    }

    public CaseResultBuilder memoryUsed(Long memoryUsed) {
        this.memoryUsed = memoryUsed;
        return this;
    }

    public CaseResultBuilder actualOutput(String actualOutput) {
        this.actualOutput = ProcessUtils.normalizeOutput(actualOutput);
        return this;
    }

    public CaseResultBuilder expectedOutput(String expectedOutput) {
        this.expectedOutput = ProcessUtils.normalizeOutput(expectedOutput);
        return this;
    }

    public CaseResultBuilder input(String input) {
        this.input = input;
        return this;
    }

    public CaseResultBuilder fromTestCase(TestCase testCase) {
        this.input = testCase.input();
        this.expectedOutput = testCase.expectedOutput();
        return this;
    }

    public CaseResult build() {
        return CaseResult.builder()
                .status(status)
                .message(message)
                .executionTime(executionTime)
                .memoryUsed(memoryUsed)
                .actualOutput(actualOutput)
                .expectedOutput(expectedOutput)
                .input(input)
                .build();
    }
}
