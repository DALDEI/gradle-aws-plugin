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

import java.io.File;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Named;

public class EbAppVersionExtension implements Named {

  @Setter
  private Object label;

  @Getter
  @Setter
  private String description = "";

  @Getter
  @Setter
  private String bucket;

  @Setter
  private Object key;

  @Getter
  @Setter
  private File file;

  public String getLabel() {
    if (label instanceof Closure) {
      return ((Closure<?>) label).call().toString();
    }
    return label.toString();
  }

  String getKey() {
    if (key instanceof Closure) {
      return ((Closure<?>) key).call().toString();
    }
    return key.toString();
  }

  @Override
  public String getName() {
    return getLabel();
  }
}
