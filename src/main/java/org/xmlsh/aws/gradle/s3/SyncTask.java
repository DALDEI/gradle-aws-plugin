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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.file.EmptyFileVisitor;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class SyncTask extends ConventionTask {

  private static String md5(File file) {
    try {
      return Files.hash(file, Hashing.md5()).toString();
    } catch (IOException e) {
      return "";
    }
  }

  @Getter
  @Setter
  private String bucketName;

  @Getter
  @Setter
  private String prefix = "";

  @Getter
  @Setter
  private File source;

  @Getter
  @Setter
  private boolean delete;

  @Getter
  @Setter
  private int threads = 5;

  @Getter
  @Setter
  private Closure<ObjectMetadata> metadataProvider;

  @TaskAction
  public void uploadAction() throws InterruptedException {
    // to enable conventionMappings feature
    String bucketName = getBucketName();
    String prefix = getPrefix();
    File source = getSource();

    if (bucketName == null)
      throw new GradleException("bucketName is not specified");
    if (source == null)
      throw new GradleException("source is not specified");
    if (source.isDirectory() == false)
      throw new GradleException("source must be directory");

    prefix = prefix.startsWith("/") ? prefix.substring(1) : prefix;

    AmazonS3PluginExtension ext = getProject().getExtensions().getByType(AmazonS3PluginExtension.class);
    AmazonS3 s3 = ext.getClient();

    upload(s3, prefix);
    if (isDelete()) {
      deleteAbsent(s3, prefix);
    }
  }

  private void upload(AmazonS3 s3, String prefix) throws InterruptedException {
    // to enable conventionMappings feature
    String bucketName = getBucketName();
    File source = getSource();
    Closure<ObjectMetadata> metadataProvider = getMetadataProvider();

    ExecutorService es = Executors.newFixedThreadPool(threads);
    getLogger().info("Start uploading");
    getLogger().info("uploading... {} to s3://{}/{}", bucketName, bucketName, prefix);
    getProject().fileTree(source).visit(new EmptyFileVisitor() {
      public void visitFile(FileVisitDetails element) {
        es.execute(new UploadTask(s3, element, bucketName, prefix, metadataProvider, getLogger()));
      }
    });

    es.shutdown();
    es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    getLogger().info("Finish uploading");
  }

  private void deleteAbsent(AmazonS3 s3, String prefix) {
    // to enable conventionMappings feature
    String bucketName = getBucketName();
    String pathPrefix = getNormalizedPathPrefix();

    s3.listObjects(bucketName, prefix).getObjectSummaries().forEach(os -> {
      File f = getProject().file(pathPrefix + os.getKey().substring(prefix.length()));
      if (f.exists() == false) {
        getLogger().info("deleting... s3://{}/{}", bucketName, os.getKey());
        s3.deleteObject(bucketName, os.getKey());
      }
    });
  }

  private String getNormalizedPathPrefix() {
    String pathPrefix = getSource().toString();
    pathPrefix += pathPrefix.endsWith("/") ? "" : "/";
    return pathPrefix;
  }

  private static class UploadTask implements Runnable {

    private AmazonS3 s3;
    private FileVisitDetails element;
    private String bucketName;
    private String prefix;
    private Closure<ObjectMetadata> metadataProvider;
    private Logger logger;

    public UploadTask(AmazonS3 s3, FileVisitDetails element, String bucketName, String prefix,
        Closure<ObjectMetadata> metadataProvider, Logger logger) {
      this.s3 = s3;
      this.element = element;
      this.bucketName = bucketName;
      this.prefix = prefix;
      this.metadataProvider = metadataProvider;
      this.logger = logger;
    }

    @Override
    public void run() {
      // to enable conventionMappings feature

      String relativePath = prefix + element.getRelativePath().toString();
      String key = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;

      boolean doUpload = false;
      try {
        ObjectMetadata metadata = s3.getObjectMetadata(bucketName, key);
        if (metadata.getETag().equalsIgnoreCase(md5(element.getFile())) == false) {
          doUpload = true;
        }
      } catch (AmazonS3Exception e) {
        doUpload = true;
      }

      if (doUpload) {
        logger.info(" => s3://{}/{}", bucketName, key);
        s3.putObject(new PutObjectRequest(bucketName, key, element.getFile())
            .withMetadata(metadataProvider == null ? null : metadataProvider.call(bucketName, key, element.getFile())));
      }
      else {
        logger.info(" => s3://{}/{} (SKIP)", bucketName, key);
      }
    }
  }
}
