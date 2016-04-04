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
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Named;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class EbConfigurationTemplateExtension implements Named {

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String desc;

  @Setter
  private Object optionSettings;

  @Getter
  @Setter
  private String solutionStackName;

  @Getter
  @Setter
  private boolean recreate = false;

  public EbConfigurationTemplateExtension(String name) {
    this.name = name;
  }

  public String getOptionSettings() throws IOException {
    if (optionSettings instanceof Closure) {
      Closure<?> closure = (Closure<?>) optionSettings;
      return closure.call().toString();
    }
    if (optionSettings instanceof File) {
      File file = (File) optionSettings;
      return Files.toString(file, Charsets.UTF_8);
    }
    return optionSettings.toString();
  }
}
