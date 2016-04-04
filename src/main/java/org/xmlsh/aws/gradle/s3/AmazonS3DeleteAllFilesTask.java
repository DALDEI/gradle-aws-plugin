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

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AmazonS3DeleteAllFilesTask extends ConventionTask {

  @Getter
  @Setter
  public String bucketName;

  @Getter
  @Setter
  public String prefix = "";

  public AmazonS3DeleteAllFilesTask() {
    setDescription("Delete all files in the S3 bucket.");
    setGroup("AWS");
  }

  @TaskAction
  public void delete() {
    // to enable conventionMappings feature
    String bucketName = getBucketName();
    String prefix = getPrefix();

    if (bucketName == null)
      throw new GradleException("bucketName is not specified");

    AmazonS3PluginExtension ext = getProject().getExtensions().getByType(AmazonS3PluginExtension.class);
    AmazonS3 s3 = ext.getClient();

    if (prefix.startsWith("/")) {
      prefix = prefix.substring(1);
    }

    getLogger().info("Delete s3://{}/{}*", bucketName, prefix);

    List<S3ObjectSummary> objectSummaries;
    while ((objectSummaries = s3.listObjects(bucketName, prefix).getObjectSummaries()).isEmpty() == false) {
      objectSummaries.forEach(os -> {
        getLogger().info("  Deleting... s3://{}/{}", bucketName, os.getKey());
        s3.deleteObject(bucketName, os.getKey());
      });
    }
  }
}
