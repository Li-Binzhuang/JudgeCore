package org.laoli.judge.service.comparator;

import org.laoli.judge.model.entity.CaseResult;

public interface OutputComparator {
    CaseResult compare(CaseResult result);
}
