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
package org.xmlsh.aws.gradle.ec2;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.ImportKeyPairRequest;
import com.amazonaws.services.ec2.model.ImportKeyPairResult;

public class AmazonEC2ImportKeyTask extends ConventionTask {

  @Getter
  @Setter
  private String keyName;

  @Getter
  @Setter
  private String publicKeyMaterial;

  @Getter
  @Setter
  public boolean ifNotExists;

  @Getter
  private ImportKeyPairResult importKeyPairResult;

  public AmazonEC2ImportKeyTask() {
    setDescription("Start EC2 instance.");
    setGroup("AWS");
  }

  @TaskAction
  public void importKey() {
    // to enable conventionMappings feature
    String keyName = getKeyName();
    String publicKeyMaterial = getPublicKeyMaterial();

    if (keyName == null)
      throw new GradleException("keyName is required");

    AmazonEC2PluginExtension ext = getProject().getExtensions().getByType(AmazonEC2PluginExtension.class);
    AmazonEC2 ec2 = ext.getClient();

    if (isIfNotExists() == false || exists(ec2) == false) {
      importKeyPairResult = ec2.importKeyPair(new ImportKeyPairRequest(keyName, publicKeyMaterial));
      getLogger().info("KeyPair imported: {}", importKeyPairResult.getKeyFingerprint());
    }
  }

  private boolean exists(AmazonEC2 ec2) {
    // to enable conventionMappings feature
    String keyName = getKeyName();

    try {
      DescribeKeyPairsResult describeKeyPairsResult =
          ec2.describeKeyPairs(new DescribeKeyPairsRequest().withKeyNames(keyName));
      return describeKeyPairsResult.getKeyPairs().isEmpty() == false;
    } catch (AmazonClientException e) {
      return false;
    }
  }

}
