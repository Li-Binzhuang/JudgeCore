package org.laoli.judge.interfaces.grpc;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.laoli.api.JudgeCore;
import org.laoli.api.JudgeServiceGrpc;
import org.laoli.judge.application.JudgeApplicationService;
import org.laoli.judge.interfaces.dto.JudgeRequest;
import org.laoli.judge.interfaces.dto.JudgeResponse;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.model.enums.SimpleResult;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description gRPC 判题服务实现（接口适配层）
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@GrpcService
public class JudgeGrpcService extends JudgeServiceGrpc.JudgeServiceImplBase {
    
    private final JudgeApplicationService judgeApplicationService;
    
    public JudgeGrpcService(JudgeApplicationService judgeApplicationService) {
        this.judgeApplicationService = judgeApplicationService;
    }
    
    @Override
    public void judge(JudgeCore.Request request, StreamObserver<JudgeCore.Response> responseObserver) {
        try {
            // 参数验证
            validateRequest(request, responseObserver);
            
            // 转换为应用层 DTO
            JudgeRequest judgeRequest = convertToJudgeRequest(request);
            
            // 调用应用服务
            JudgeResponse judgeResponse = judgeApplicationService.judge(judgeRequest);
            
            // 转换为 gRPC 响应
            JudgeCore.Response response = convertToGrpcResponse(judgeResponse);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            responseObserver.onNext(JudgeCore.Response.newBuilder()
                    .setMessage(e.getMessage())
                    .setStatus(SimpleResult.SYSTEM_ERROR.toString())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error processing gRPC request", e);
            responseObserver.onNext(JudgeCore.Response.newBuilder()
                    .setMessage("Internal server error: " + e.getMessage())
                    .setStatus(SimpleResult.SYSTEM_ERROR.toString())
                    .build());
            responseObserver.onCompleted();
        }
    }
    
    /**
     * 验证请求参数
     */
    private void validateRequest(JudgeCore.Request request, StreamObserver<JudgeCore.Response> responseObserver) {
        // 验证语言
        try {
            Language.valueOf(request.getLanguage().toUpperCase());
        } catch (Exception e) {
            responseObserver.onNext(JudgeCore.Response.newBuilder()
                    .setMessage("Language not supported: " + request.getLanguage())
                    .setStatus(SimpleResult.SYSTEM_ERROR.toString())
                    .build());
            responseObserver.onCompleted();
            throw new IllegalArgumentException("Language not supported");
        }
        
        // 验证代码
        if (Objects.isNull(request.getCode()) || request.getCode().isEmpty() || request.getCode().isBlank()) {
            responseObserver.onNext(JudgeCore.Response.newBuilder()
                    .setMessage("Code is empty")
                    .setStatus(SimpleResult.SYSTEM_ERROR.toString())
                    .build());
            responseObserver.onCompleted();
            throw new IllegalArgumentException("Code is empty");
        }
        
        // 验证测试用例
        if (Objects.isNull(request.getCasesList()) || request.getCasesList().isEmpty()) {
            responseObserver.onNext(JudgeCore.Response.newBuilder()
                    .setMessage("Test cases is empty")
                    .setStatus(SimpleResult.SYSTEM_ERROR.toString())
                    .build());
            responseObserver.onCompleted();
            throw new IllegalArgumentException("Test cases is empty");
        }
    }
    
    /**
     * 转换 gRPC 请求到应用层 DTO
     */
    private JudgeRequest convertToJudgeRequest(JudgeCore.Request request) {
        JudgeRequest judgeRequest = new JudgeRequest();
        judgeRequest.setCode(request.getCode());
        judgeRequest.setLanguage(request.getLanguage());
        judgeRequest.setTimeLimit(request.getTimeLimit());
        judgeRequest.setMemoryLimit(request.getMemoryLimit());
        judgeRequest.setCases(request.getCasesList().stream()
                .map(c -> {
                    JudgeRequest.TestCaseDTO testCase = new JudgeRequest.TestCaseDTO();
                    testCase.setInput(c.getInput());
                    testCase.setExpectedOutput(c.getExpectedOutput());
                    return testCase;
                })
                .collect(Collectors.toList()));
        return judgeRequest;
    }
    
    /**
     * 转换应用层响应到 gRPC 响应
     */
    private JudgeCore.Response convertToGrpcResponse(JudgeResponse judgeResponse) {
        JudgeCore.Response.Builder builder = JudgeCore.Response.newBuilder()
                .setStatus(judgeResponse.getStatus())
                .setExecutionTime(judgeResponse.getExecutionTime())
                .setMemoryUsed(judgeResponse.getMemoryUsed());
        
        if (judgeResponse.getMessage() != null) {
            builder.setMessage(judgeResponse.getMessage());
        }
        
        if (judgeResponse.getCaseInfo() != null) {
            JudgeResponse.CaseInfoDTO caseInfo = judgeResponse.getCaseInfo();
            builder.setCaseInfo(JudgeCore.ReturnCaseInfo.newBuilder()
                    .setInput(caseInfo.getInput() != null ? caseInfo.getInput() : "")
                    .setExpectedOutput(caseInfo.getExpectedOutput() != null ? caseInfo.getExpectedOutput() : "")
                    .setActualOutput(caseInfo.getActualOutput() != null ? caseInfo.getActualOutput() : "")
                    .build());
        }
        
        return builder.build();
    }
}
