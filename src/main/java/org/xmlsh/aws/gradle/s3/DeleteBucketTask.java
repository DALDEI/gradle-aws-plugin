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
import com.amazonaws.services.s3.model.ObjectListing;

public class DeleteBucketTask extends ConventionTask {

  @Getter
  @Setter
  String bucketName;

  @Getter
  @Setter
  boolean ifExists;

  @Getter
  @Setter
  boolean deleteObjects;

  public DeleteBucketTask() {
    setDescription("Create the Amazon S3 bucket.");
    setGroup("AWS");
  }

  @TaskAction
  public void deleteBucket() {
    // to enable conventionMappings feature
    String bucketName = getBucketName();
    boolean ifExists = isIfExists();

    if (bucketName == null)
      throw new GradleException("bucketName is not specified");

    AmazonS3PluginExtension ext = getProject().getExtensions().getByType(AmazonS3PluginExtension.class);
    AmazonS3 s3 = ext.getClient();

    if (ifExists == false || exists(s3)) {
      if (deleteObjects) {
        getLogger().info("Delete all S3 objects in bucket [{}]", bucketName);
        ObjectListing objectListing = s3.listObjects(bucketName);
        while (objectListing.getObjectSummaries().isEmpty() == false) {
          objectListing.getObjectSummaries().forEach(summary -> {
            getLogger().info(" => delete s3://{}/{}", bucketName, summary.getKey());
            s3.deleteObject(bucketName, summary.getKey());
          });
          objectListing = s3.listNextBatchOfObjects(objectListing);
        }
      }
      s3.deleteBucket(bucketName);
      getLogger().info("S3 bucket {} is deleted", bucketName);
    }
    else {
      getLogger().debug("S3 bucket {} does not exist", bucketName);
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
