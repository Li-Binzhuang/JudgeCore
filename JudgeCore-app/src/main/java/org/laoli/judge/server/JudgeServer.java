package org.laoli.judge.server;

import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import net.devh.boot.grpc.server.service.GrpcService;
import org.laoli.api.JudgeCore;
import org.laoli.api.JudgeServiceGrpc;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.service.IJudgeService;
import org.laoli.judge.model.entity.TestCase;

/**
 * @Description rpc判题服务实现
 * @Author laoli
 * @Date 2025/4/21 17:47
 */

@GrpcService
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

        String actualOutput = "";
        String expectedOutput = "";

        if(judgeResult.caseResults()!=null){
            actualOutput = judgeResult.caseResults().actualOutput()==null?"":judgeResult.caseResults().actualOutput();
            expectedOutput = judgeResult.caseResults().expectedOutput()==null?"":judgeResult.caseResults().expectedOutput();
        }

        responseObserver.onNext(JudgeCore.Response.newBuilder()
                .setCaseInfo(JudgeCore.ReturnCaseInfo.newBuilder()
                        .setActualOutput(actualOutput)
                        .setInput(expectedOutput)
                        .build())
                .setExecutionTime(judgeResult.executionTime())
                .setMessage(judgeResult.message()==null?"":judgeResult.message())
                .setStatus(judgeResult.status().toString())
                .setMemoryUsed(judgeResult.memoryUsed())
                .build());
        responseObserver.onCompleted();
    }
}