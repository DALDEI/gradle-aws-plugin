/*
 * Copyright 2015-2016 David A. Lee. <dlee@calldei.com>
 *
 * This is a derived work from Classmethods, Inc.
 *
 * Current License Terms - Dual licensed under the following :
 *
 * "Simplified BSD License" included in license.txt
 *
 * The original work (derived from) license terms:  Apache License, Version 2.0
 * included in copyright.origin/LICENSE.TXT
 */
/*
 * TOTALLY BOGUS
 * Stupid
 */
package org.xmlsh.aws.gradle.lambda;

import groovy.lang.Closure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.LogType;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class AWSLambdaInvokeTask extends ConventionTask {

  @Getter
  @Setter
  private String functionName;

  @Getter
  @Setter
  private InvocationType invocationType;

  @Getter
  @Setter
  private LogType logType = LogType.None;

  @Getter
  @Setter
  private String clientContext;

  @Getter
  @Setter
  private Object payload;

  @Getter
  private InvokeResult invokeResult;

  public AWSLambdaInvokeTask() {
    setDescription("Invoke Lambda function.");
    setGroup("AWS");
  }

  @TaskAction
  public void deleteFunction() throws FileNotFoundException, IOException {
    // to enable conventionMappings feature
    String functionName = getFunctionName();

    if (functionName == null)
      throw new GradleException("functionName is required");

    AWSLambdaPluginExtension ext = getProject().getExtensions().getByType(AWSLambdaPluginExtension.class);
    AWSLambda lambda = ext.getClient();

    InvokeRequest request = new InvokeRequest()
        .withFunctionName(functionName)
        .withInvocationType(getInvocationType())
        .withLogType(getLogType())
        .withClientContext(getClientContext());
    setupPayload(request);
    invokeResult = lambda.invoke(request);
    getLogger().info("Invoke Lambda function requested: {}", functionName);
  }

  private void setupPayload(InvokeRequest request) throws IOException {
    Object payload = getPayload();
    String str;
    if (payload instanceof ByteBuffer) {
      request.setPayload((ByteBuffer) payload);
      return;
    }
    if (payload instanceof File) {
      File file = (File) payload;
      str = Files.toString(file, Charsets.UTF_8);
    }
    else if (payload instanceof Closure) {
      Closure<?> closure = (Closure<?>) payload;
      str = closure.call().toString();
    }
    else {
      str = payload.toString();
    }
    request.setPayload(str);
  }
}
