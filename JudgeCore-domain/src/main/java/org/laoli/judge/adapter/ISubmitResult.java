package org.laoli.judge.adapter;
/*
 *@description 提交结果适配器
 *@author laoli
 *@create 2025/4/19 13:47
 */
import org.laoli.judge.model.aggregate.JudgeResult;

public interface ISubmitResult {
    void submitResult(JudgeResult judgeResult);
}
