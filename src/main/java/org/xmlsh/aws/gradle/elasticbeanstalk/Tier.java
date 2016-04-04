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
package org.xmlsh.aws.gradle.elasticbeanstalk;

import com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier;

public enum Tier {

  WebServer(new EnvironmentTier()
      .withType("Standard")
      .withName("WebServer")
      .withVersion("1.0")),

  Worker(new EnvironmentTier()
      .withType("SQS/HTTP")
      .withName("Worker")
      .withVersion("1.0"));

  final EnvironmentTier environmentTier;

  Tier(EnvironmentTier environmentTier) {
    this.environmentTier = environmentTier;
  }

  public EnvironmentTier toEnvironmentTier() {
    return environmentTier;
  }
}
