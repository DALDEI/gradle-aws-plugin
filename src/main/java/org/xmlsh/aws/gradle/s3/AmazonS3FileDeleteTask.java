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

import com.amazonaws.services.s3.AmazonS3;

public class AmazonS3FileDeleteTask extends ConventionTask {

  public AmazonS3FileDeleteTask() {
    setDescription("Delete file from the Amazon S3 bucket.");
    setGroup("AWS");
  }

  @Getter
  @Setter
  String bucketName;

  @Getter
  @Setter
  String key;

  @TaskAction
  public void delete() {
    // to enable conventionMappings feature
    String bucketName = getBucketName();
    String key = getKey();

    if (bucketName == null)
      throw new GradleException("bucketName is not specified");
    if (key == null)
      throw new GradleException("key is not specified");

    AmazonS3PluginExtension ext = getProject().getExtensions().getByType(AmazonS3PluginExtension.class);
    AmazonS3 s3 = ext.getClient();

    getLogger().info("deleting... " + bucketName + "/" + key);
    s3.deleteObject(bucketName, key);
  }
}
