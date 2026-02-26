package org.laoli.api;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.60.1)",
    comments = "Source: JudgeService.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class JudgeServiceGrpc {

  private JudgeServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "grpc.JudgeService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.laoli.api.JudgeCore.Request,
      org.laoli.api.JudgeCore.Response> getJudgeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Judge",
      requestType = org.laoli.api.JudgeCore.Request.class,
      responseType = org.laoli.api.JudgeCore.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.laoli.api.JudgeCore.Request,
      org.laoli.api.JudgeCore.Response> getJudgeMethod() {
    io.grpc.MethodDescriptor<org.laoli.api.JudgeCore.Request, org.laoli.api.JudgeCore.Response> getJudgeMethod;
    if ((getJudgeMethod = JudgeServiceGrpc.getJudgeMethod) == null) {
      synchronized (JudgeServiceGrpc.class) {
        if ((getJudgeMethod = JudgeServiceGrpc.getJudgeMethod) == null) {
          JudgeServiceGrpc.getJudgeMethod = getJudgeMethod =
              io.grpc.MethodDescriptor.<org.laoli.api.JudgeCore.Request, org.laoli.api.JudgeCore.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Judge"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.laoli.api.JudgeCore.Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.laoli.api.JudgeCore.Response.getDefaultInstance()))
              .setSchemaDescriptor(new JudgeServiceMethodDescriptorSupplier("Judge"))
              .build();
        }
      }
    }
    return getJudgeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static JudgeServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<JudgeServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<JudgeServiceStub>() {
        @java.lang.Override
        public JudgeServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new JudgeServiceStub(channel, callOptions);
        }
      };
    return JudgeServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static JudgeServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<JudgeServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<JudgeServiceBlockingStub>() {
        @java.lang.Override
        public JudgeServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new JudgeServiceBlockingStub(channel, callOptions);
        }
      };
    return JudgeServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static JudgeServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<JudgeServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<JudgeServiceFutureStub>() {
        @java.lang.Override
        public JudgeServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new JudgeServiceFutureStub(channel, callOptions);
        }
      };
    return JudgeServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void judge(org.laoli.api.JudgeCore.Request request,
        io.grpc.stub.StreamObserver<org.laoli.api.JudgeCore.Response> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getJudgeMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service JudgeService.
   */
  public static abstract class JudgeServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return JudgeServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service JudgeService.
   */
  public static final class JudgeServiceStub
      extends io.grpc.stub.AbstractAsyncStub<JudgeServiceStub> {
    private JudgeServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected JudgeServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new JudgeServiceStub(channel, callOptions);
    }

    /**
     */
    public void judge(org.laoli.api.JudgeCore.Request request,
        io.grpc.stub.StreamObserver<org.laoli.api.JudgeCore.Response> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getJudgeMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service JudgeService.
   */
  public static final class JudgeServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<JudgeServiceBlockingStub> {
    private JudgeServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected JudgeServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new JudgeServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.laoli.api.JudgeCore.Response judge(org.laoli.api.JudgeCore.Request request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getJudgeMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service JudgeService.
   */
  public static final class JudgeServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<JudgeServiceFutureStub> {
    private JudgeServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected JudgeServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new JudgeServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.laoli.api.JudgeCore.Response> judge(
        org.laoli.api.JudgeCore.Request request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getJudgeMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_JUDGE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_JUDGE:
          serviceImpl.judge((org.laoli.api.JudgeCore.Request) request,
              (io.grpc.stub.StreamObserver<org.laoli.api.JudgeCore.Response>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getJudgeMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.laoli.api.JudgeCore.Request,
              org.laoli.api.JudgeCore.Response>(
                service, METHODID_JUDGE)))
        .build();
  }

  private static abstract class JudgeServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    JudgeServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.laoli.api.JudgeCore.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("JudgeService");
    }
  }

  private static final class JudgeServiceFileDescriptorSupplier
      extends JudgeServiceBaseDescriptorSupplier {
    JudgeServiceFileDescriptorSupplier() {}
  }

  private static final class JudgeServiceMethodDescriptorSupplier
      extends JudgeServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    JudgeServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (JudgeServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new JudgeServiceFileDescriptorSupplier())
              .addMethod(getJudgeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
