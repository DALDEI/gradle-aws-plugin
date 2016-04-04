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
package org.xmlsh.aws.gradle.s3;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;

public class CreateBucketTask extends ConventionTask {

  @Getter
  @Setter
  public String bucketName;

  @Getter
  @Setter
  public boolean ifNotExists;

  public CreateBucketTask() {
    setDescription("Create the Amazon S3 bucket.");
    setGroup("AWS");
  }

  @TaskAction
  public void createBucket() {
    // to enable conventionMappings feature
    String bucketName = getBucketName();

    if (bucketName == null)
      throw new GradleException("bucketName is not specified");

    AmazonS3PluginExtension ext = getProject().getExtensions().getByType(AmazonS3PluginExtension.class);
    AmazonS3 s3 = ext.getClient();

    if (isIfNotExists() == false || exists(s3) == false) {
      s3.createBucket(bucketName);
      getLogger().info("S3 Bucket '{}' created", bucketName);
    }
  }

  private boolean exists(AmazonS3 s3) {
    // to enable conventionMappings feature
    String bucketName = getBucketName();

    try {
      s3.getBucketLocation(bucketName);
      return true;
    } catch (AmazonClientException e) {
      return false;
    }
  }
}
