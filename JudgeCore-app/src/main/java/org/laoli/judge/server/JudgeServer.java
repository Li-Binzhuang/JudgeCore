package org.laoli.judge.server;

import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import org.laoli.api.JudgeCore;
import org.laoli.api.JudgeServiceGrpc;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.service.IJudgeService;
import org.laoli.judge.model.entity.TestCase;

/**
 * @Description TODO
 * @Author laoli
 * @Date 2025/4/21 17:47
 */

public class JudgeServer extends JudgeServiceGrpc.JudgeServiceImplBase {
    @Resource
    private IJudgeService judgeService;
    @Override
    public void judge(JudgeCore.Request request, StreamObserver<JudgeCore.Response> responseObserver) {
        JudgeResult judgeResult = judgeService.judge(request.getCasesList().stream().map(testCase -> TestCase.builder()
                        .input(testCase.getInput())
                        .expectedOutput(testCase.getExpectedOutput())
                        .build()).toList(),
                request.getCode(),
                Language.valueOf(request.getLanguage().toUpperCase()),
                request.getTimeLimit(), request.getMemoryLimit());

        super.judge(request, responseObserver);
    }
}