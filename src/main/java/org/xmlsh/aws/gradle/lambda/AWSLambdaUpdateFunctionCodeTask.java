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
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeResult;

public class AWSLambdaUpdateFunctionCodeTask extends ConventionTask {

  @Getter
  @Setter
  private String functionName;

  @Getter
  @Setter
  private File zipFile;

  @Getter
  @Setter
  private S3File s3File;

  @Getter
  private UpdateFunctionCodeResult updateFunctionCode;

  public AWSLambdaUpdateFunctionCodeTask() {
    setDescription("Update Lambda function code.");
    setGroup("AWS");
  }

  @TaskAction
  public void updateFunctionCode() throws FileNotFoundException, IOException {
    // to enable conventionMappings feature
    String functionName = getFunctionName();

    if (functionName == null)
      throw new GradleException("functionName is required");

    if ((zipFile == null && s3File == null) || (zipFile != null && s3File != null)) {
      throw new GradleException("exactly one of zipFile or s3File is required");
    }

    AWSLambdaPluginExtension ext = getProject().getExtensions().getByType(AWSLambdaPluginExtension.class);
    AWSLambda lambda = ext.getClient();

    UpdateFunctionCodeRequest request = new UpdateFunctionCodeRequest()
        .withFunctionName(getFunctionName());
    if (zipFile != null) {
      try (RandomAccessFile raf = new RandomAccessFile(getZipFile(), "r");
          FileChannel channel = raf.getChannel()) {
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        buffer.load();
        request = request.withZipFile(buffer);
      }
    }
    else {
      // assume s3File is not null
      s3File.validate();
      request = request
          .withS3Bucket(s3File.getBucketName())
          .withS3Key(s3File.getKey())
          .withS3ObjectVersion(s3File.getObjectVersion());
    }
    UpdateFunctionCodeResult updateFunctionCode = lambda.updateFunctionCode(request);
    getLogger().info("Update Lambda function requested: {}", updateFunctionCode.getFunctionArn());
  }
}
