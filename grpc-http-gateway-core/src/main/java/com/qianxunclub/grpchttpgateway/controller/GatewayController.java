package com.qianxunclub.grpchttpgateway.controller;

import com.google.protobuf.DescriptorProtos;
import com.qianxunclub.grpchttpgateway.configuration.GrpcConfiguration;
import com.qianxunclub.grpchttpgateway.model.CallResults;
import com.qianxunclub.grpchttpgateway.model.GrpcMethodDefinition;
import com.qianxunclub.grpchttpgateway.service.GrpcProxyService;
import com.qianxunclub.grpchttpgateway.utils.ChannelFactory;
import com.qianxunclub.grpchttpgateway.utils.GrpcReflectionUtils;
import com.qianxunclub.grpchttpgateway.utils.GrpcServiceUtils;
import com.qianxunclub.grpchttpgateway.utils.JSON;
import io.grpc.ManagedChannel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static io.grpc.CallOptions.DEFAULT;
import static java.util.Collections.singletonList;


@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class GatewayController {

    private final GrpcProxyService grpcProxyService;
    private final GrpcConfiguration grpcConfiguration;

    @GetMapping("/getEndpoint")
    public Object getEndpoint() {
        return grpcConfiguration.getAllEndpoint();
    }


    @GetMapping("/{serverName}")
    public Object get(@PathVariable String serverName) throws Exception {
        GrpcConfiguration.Endpoint endpoint = grpcConfiguration.getEndpoint(serverName);
        List<DescriptorProtos.FileDescriptorSet> fileDescriptorSets = GrpcServiceUtils.getFileDescriptorSetList(
                endpoint.getChannelHost(),
                endpoint.getChannelPort()
        );
        return GrpcServiceUtils.getMethodNames(fileDescriptorSets);
    }

    @PostMapping("/{serverName}/{fullMethodName}")
    public Object invoke(
            @PathVariable String serverName,
            @PathVariable String fullMethodName,
            @RequestBody String payload,
            @RequestParam(defaultValue = "{}") String headers
    ) throws Exception {
        GrpcConfiguration.Endpoint endpoint = grpcConfiguration.getEndpoint(serverName);
        GrpcMethodDefinition methodDefinition = GrpcReflectionUtils.parseToMethodDefinition(fullMethodName);
        Map<String, Object> headerMap = JSON.getGson().fromJson(headers, Map.class);
        ManagedChannel channel = null;
        try {
            channel = ChannelFactory.create(endpoint.getChannelHost(), endpoint.getChannelPort(), headerMap);
            CallResults results = grpcProxyService.invokeMethod(methodDefinition, channel, DEFAULT, singletonList(payload));
            return results.getResults();
        } finally {
            if (channel != null) {
                channel.shutdown();
            }
        }

    }

}
