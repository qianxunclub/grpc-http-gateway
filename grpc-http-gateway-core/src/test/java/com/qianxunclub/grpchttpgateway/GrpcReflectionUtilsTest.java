package com.qianxunclub.grpchttpgateway;

import com.qianxunclub.grpchttpgateway.utils.GrpcReflectionUtils;
import org.junit.Test;

public class GrpcReflectionUtilsTest {

    @Test
    public void testParseToMethodDefinition() {
        System.out.println(GrpcReflectionUtils.parseToMethodDefinition("io.grpc.reflection.Test.print"));
    }
}
