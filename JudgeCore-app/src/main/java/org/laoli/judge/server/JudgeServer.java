package org.laoli.judge.server;

import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import net.devh.boot.grpc.server.service.GrpcService;
import org.laoli.api.JudgeCore;
import org.laoli.api.JudgeServiceGrpc;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.model.enums.SimpleResult;
import org.laoli.judge.service.IJudgeService;
import org.laoli.judge.model.entity.TestCase;

import java.util.Objects;

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
        //判空
        requsetNotNull(request, responseObserver);
        //获取测试用例
        JudgeResult judgeResult = judgeService.judge(request.getCasesList().stream().map(testCase -> TestCase.builder()
                        .input(testCase.getInput())
                        .expectedOutput(testCase.getExpectedOutput())
                        .build()).toList(),
                request.getCode(),
                Language.valueOf(request.getLanguage().toUpperCase()),
                request.getTimeLimit(), request.getMemoryLimit());

        String actualOutput = "";
        String expectedOutput = "";
        String input = "";
        //判空
        if(judgeResult.caseResults()!=null){
            input = judgeResult.caseResults().input()==null?"":judgeResult.caseResults().input();
            actualOutput = judgeResult.caseResults().actualOutput()==null?"":judgeResult.caseResults().actualOutput();
            expectedOutput = judgeResult.caseResults().expectedOutput()==null?"":judgeResult.caseResults().expectedOutput();
        }

        responseObserver.onNext(JudgeCore.Response.newBuilder()
                .setCaseInfo(JudgeCore.ReturnCaseInfo.newBuilder()
                        .setActualOutput(actualOutput)
                        .setInput(input)
                        .setExpectedOutput(expectedOutput)
                        .build())
                .setExecutionTime(judgeResult.executionTime())
                .setMessage(judgeResult.message()==null?"":judgeResult.message())
                .setStatus(judgeResult.status().toString())
                .setMemoryUsed(judgeResult.memoryUsed())
                .build());

        responseObserver.onCompleted();
    }

    private static void requsetNotNull(JudgeCore.Request request, StreamObserver<JudgeCore.Response> responseObserver) {
        //判断参数是否合法
        try{
            Language.valueOf(request.getLanguage().toUpperCase());
        }catch (Exception e){
            responseObserver.onNext(JudgeCore.Response.newBuilder()
                    .setMessage("Language not supported")
                    .setStatus(SimpleResult.SYSTEM_ERROR.toString())
                    .build());
        }
        //判断代码是否为空
        if(Objects.isNull(request.getCode())|| request.getCode().isEmpty()|| request.getCode().isBlank()){
            responseObserver.onNext(JudgeCore.Response.newBuilder()
                   .setMessage("Code is empty")
                   .setStatus(SimpleResult.SYSTEM_ERROR.toString())
                   .build());
        }
        //判断测试用例是否为空
        if(Objects.isNull(request.getCasesList())|| request.getCasesList().isEmpty()){
            responseObserver.onNext(JudgeCore.Response.newBuilder()
                  .setMessage("Test cases is empty")
                  .setStatus(SimpleResult.SYSTEM_ERROR.toString())
                  .build());
        }
    }
}