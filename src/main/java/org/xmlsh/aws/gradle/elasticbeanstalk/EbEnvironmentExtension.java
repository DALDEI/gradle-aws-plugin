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

import groovy.lang.Closure;
import lombok.Getter;
import lombok.Setter;

import org.gradle.util.Configurable;

public class EbEnvironmentExtension implements Configurable<Void> {

  @Getter
  @Setter
  private String envName;

  @Getter
  @Setter
  private String envDesc = "";

  @Getter
  @Setter
  private String cnamePrefix;

  @Getter
  @Setter
  private String templateName;

  @Getter
  @Setter
  private String versionLabel;

  @Override
  @SuppressWarnings("rawtypes")
  public Void configure(Closure closure) {
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    closure.setDelegate(this);
    closure.call();
    return null;
  }
}
