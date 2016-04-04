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

@SuppressWarnings("serial")
public class ParseException extends RuntimeException {

  private String expression;

  public ParseException(String expression) {
    this.expression = expression;
  }

  @Override
  public String getMessage() {
    return "fail to parse expression: " + expression;
  }
}
