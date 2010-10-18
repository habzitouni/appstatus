/*
 * Copyright 2010 Capgemini
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */
package net.sf.appstatus.check.impl;

import net.sf.appstatus.IStatusChecker;

/**
 * @author Nicolas Richeton
 * 
 */
public abstract class AbstractStatusChecker implements IStatusChecker {

  protected static final int FATAL = 2;
  protected static final int OK = 0;
  protected static final int WARN = 1;

  /**
   * Create result. Details can then be added using
   * {@link StatusResult#setDescription(String)} and
   * {@link StatusResult#setResolutionSteps(String)}.
   * 
   * @param code
   *          {@link AbstractStatusChecker#OK} or
   *          {@link AbstractStatusChecker#FATAL}
   * @return a {@link StatusResult} object
   * @see StatusResult#OK
   * @see StatusResult#FATAL
   * @see StatusResult#ERROR
   */
  protected StatusResult createResult(int code) {
    StatusResult result = new StatusResult();
    result.setProbeName(getName());

    switch (code) {
    case OK:
      result.setCode(StatusResult.OK);
      break;
    case FATAL:
      result.setCode(StatusResult.FATAL);
      break;
    default:
      // WARN
      result.setCode(StatusResult.ERROR);
      break;
    }

    return result;
  }
}
