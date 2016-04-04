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

import java.io.File;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

public class AmazonS3ProgressiveFileUploadTask extends AbstractAmazonS3FileUploadTask {

  public AmazonS3ProgressiveFileUploadTask() {
    setDescription("Upload file to the Amazon S3 bucket.");
    setGroup("AWS");
  }

  @TaskAction
  public void upload() throws InterruptedException {
    // to enable conventionMappings feature
    String bucketName = getBucketName();
    String key = getKey();
    File file = getFile();

    if (bucketName == null)
      throw new GradleException("bucketName is not specified");
    if (key == null)
      throw new GradleException("key is not specified");
    if (file == null)
      throw new GradleException("file is not specified");
    if (file.isFile() == false)
      throw new GradleException("file must be regular file");

    AmazonS3PluginExtension ext = getProject().getExtensions().getByType(AmazonS3PluginExtension.class);
    AmazonS3 s3 = ext.getClient();

    TransferManager s3mgr = new TransferManager(s3);
    getLogger().info("Uploading... s3://{}/{}", bucketName, key);

    Upload upload = s3mgr.upload(new PutObjectRequest(getBucketName(), getKey(), getFile())
        .withMetadata(getObjectMetadata()));
    upload.addProgressListener(new ProgressListener() {
      public void progressChanged(ProgressEvent event) {
        getLogger().info("  {}% uploaded", upload.getProgress().getPercentTransferred());
      }
    });
    upload.waitForCompletion();
    setResourceUrl(((AmazonS3Client) s3).getResourceUrl(bucketName, key));
    getLogger().info("Upload completed: {}", getResourceUrl());
  }
}
