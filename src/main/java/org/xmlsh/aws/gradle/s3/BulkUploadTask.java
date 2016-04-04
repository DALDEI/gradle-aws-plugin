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

import groovy.lang.Closure;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.file.EmptyFileVisitor;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class BulkUploadTask extends ConventionTask {

  @Getter
  @Setter
  private String bucketName;

  @Getter
  @Setter
  private String prefix;

  @Getter
  @Setter
  private FileTree source;

  @Getter
  @Setter
  private Closure<ObjectMetadata> metadataProvider;

  @TaskAction
  public void upload() {
    // to enable conventionMappings feature
    String bucketName = getBucketName();
    String prefix = getNormalizedPrefix();
    FileTree source = getSource();

    AmazonS3PluginExtension ext = getProject().getExtensions().getByType(AmazonS3PluginExtension.class);
    AmazonS3 s3 = ext.getClient();

    getLogger().info("uploading... {} to s3://{}/{}", source, bucketName, prefix);
    source.visit(new EmptyFileVisitor() {
      public void visitFile(FileVisitDetails element) {
        String key = prefix + element.getRelativePath();
        getLogger().info(" => s3://{}/{}", bucketName, key);
        Closure<ObjectMetadata> metadataProvider = getMetadataProvider();
        s3.putObject(new PutObjectRequest(bucketName, key, element.getFile())
            .withMetadata(metadataProvider == null ? null : metadataProvider.call(getBucketName(), key,
                element.getFile())));
      }
    });
  }

  private String getNormalizedPrefix() {
    String prefix = getPrefix();
    if (prefix.startsWith("/")) {
      prefix = prefix.substring(1);
    }
    if (prefix.endsWith("/") == false) {
      prefix += "/";
    }
    return prefix;
  }
}
