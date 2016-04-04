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
import java.io.IOException;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class AmazonS3FileUploadTask extends AbstractAmazonS3FileUploadTask {

  public AmazonS3FileUploadTask() {
    setDescription("Upload file to the Amazon S3 bucket.");
    setGroup("AWS");
  }

  @TaskAction
  public void upload() throws IOException {
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

    // metadata will be null iff the object does not exist
    ObjectMetadata metadata = existingObjectMetadata();

    if (metadata == null || (isOverwrite() && metadata.getETag().equals(md5()) == false)) {
      getLogger().info("uploading... " + bucketName + "/" + key);
      s3.putObject(new PutObjectRequest(bucketName, key, file)
          .withMetadata(getObjectMetadata()));
      getLogger().info("upload completed: " + getResourceUrl());
    }
    else {
      getLogger().info("s3://{}/{} already exists with matching md5 sum -- skipped", bucketName, key);
    }
    setResourceUrl(((AmazonS3Client) s3).getResourceUrl(bucketName, key));
  }

  private String md5() throws IOException {
    return Files.hash(getFile(), Hashing.md5()).toString();
  }
}
