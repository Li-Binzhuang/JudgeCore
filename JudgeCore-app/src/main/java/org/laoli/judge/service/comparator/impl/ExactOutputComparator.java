package org.laoli.judge.service.comparator.impl;

import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.enums.SimpleResult;
import org.laoli.judge.service.comparator.OutputComparator;
import org.laoli.judge.util.ProcessUtils;

public class ExactOutputComparator implements OutputComparator {

    @Override
    public CaseResult compare(CaseResult result) {
        String actual = ProcessUtils.normalizeOutput(result.actualOutput());
        String expected = ProcessUtils.normalizeOutput(result.expectedOutput());

        if (actual.equals(expected)) {
            return CaseResult.builder()
                    .status(SimpleResult.ACCEPTED)
                    .executionTime(result.executionTime())
                    .memoryUsed(result.memoryUsed())
                    .actualOutput(actual)
                    .expectedOutput(expected)
                    .input(result.input())
                    .build();
        }

        return CaseResult.builder()
                .status(SimpleResult.WRONG_ANSWER)
                .executionTime(result.executionTime())
                .memoryUsed(result.memoryUsed())
                .actualOutput(actual)
                .expectedOutput(expected)
                .input(result.input())
                .build();
    }
}
