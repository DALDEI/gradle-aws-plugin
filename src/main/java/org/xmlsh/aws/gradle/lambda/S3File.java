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

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.GradleException;

public class S3File {
  @Getter
  @Setter
  private String bucketName;

  @Getter
  @Setter
  private String key;

  @Getter
  @Setter
  private String objectVersion;

  /**
   * Validates that both bucketName and key are provided.
   */
  public void validate() {
    boolean missingBucketName = bucketName == null || bucketName.trim().isEmpty();
    boolean missingKey = key == null || key.trim().isEmpty();
    if (missingBucketName || missingKey) {
      throw new GradleException("bucketName and key are required for an S3File");
    }
  }
}
