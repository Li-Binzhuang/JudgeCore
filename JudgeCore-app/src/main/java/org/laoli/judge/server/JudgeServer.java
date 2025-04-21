package org.laoli.judge.server;

import org.laoli.api.JudgeServiceGrpc;

/**
 * @Description TODO
 * @Author laoli
 * @Date 2025/4/21 17:47
 */

public class JudgeServer extends JudgeServiceGrpc.JudgeServiceImplBase {
    @Override
    public void judge(JudgeCorer.Request request, io.grpc.stub.StreamObserver<JudgeCorer.Response> responseObserver) {
        super.judge(request, responseObserver);
    }
}
