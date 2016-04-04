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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.Runtime;

public class AWSLambdaCreateFunctionTask extends ConventionTask {

  @Getter
  @Setter
  private String functionName;

  @Getter
  @Setter
  private String role;

  @Getter
  @Setter
  private Runtime runtime = Runtime.Nodejs;

  @Getter
  @Setter
  private String handler;

  @Getter
  @Setter
  private String functionDescription;

  @Getter
  @Setter
  private Integer timeout;

  @Getter
  @Setter
  private Integer memorySize;

  @Getter
  @Setter
  private File zipFile;

  @Getter
  @Setter
  private S3File s3File;

  @Getter
  private CreateFunctionResult createFunctionResult;

  public AWSLambdaCreateFunctionTask() {
    setDescription("Create Lambda function.");
    setGroup("AWS");
  }

  @TaskAction
  public void createFunction() throws FileNotFoundException, IOException {
    // to enable conventionMappings feature
    String functionName = getFunctionName();

    if (functionName == null)
      throw new GradleException("functionName is required");

    File zipFile = getZipFile();
    S3File s3File = getS3File();

    if ((zipFile == null && s3File == null) || (zipFile != null && s3File != null)) {
      throw new GradleException("exactly one of zipFile or s3File is required");
    }

    AWSLambdaPluginExtension ext = getProject().getExtensions().getByType(AWSLambdaPluginExtension.class);
    AWSLambda lambda = ext.getClient();

    FunctionCode functionCode;
    if (zipFile != null) {
      try (RandomAccessFile raf = new RandomAccessFile(getZipFile(), "r");
          FileChannel channel = raf.getChannel()) {
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        buffer.load();
        functionCode = new FunctionCode().withZipFile(buffer);
      }
    }
    else {
      // assume s3File is not null
      s3File.validate();
      functionCode = new FunctionCode()
          .withS3Bucket(s3File.getBucketName())
          .withS3Key(s3File.getKey())
          .withS3ObjectVersion(s3File.getObjectVersion());
    }
    CreateFunctionRequest request = new CreateFunctionRequest()
        .withFunctionName(getFunctionName())
        .withRuntime(getRuntime())
        .withRole(getRole())
        .withHandler(getHandler())
        .withDescription(getFunctionDescription())
        .withTimeout(getTimeout())
        .withMemorySize(getMemorySize())
        .withCode(functionCode);
    createFunctionResult = lambda.createFunction(request);
    getLogger().info("Create Lambda function requested: {}", createFunctionResult.getFunctionArn());
  }
}
