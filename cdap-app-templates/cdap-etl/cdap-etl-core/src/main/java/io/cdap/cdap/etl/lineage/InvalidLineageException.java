/*
 * Copyright © 2018 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.cdap.etl.lineage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime exception thrown when the field operations to be recorded by the pipeline contains
 * invalid fields. Exception contains the invalid fields from all stages in the pipeline. Please see
 * {@link StageOperationsValidator} for checks that are done on the field before marking it as
 * invalid.
 */
public class InvalidLineageException extends RuntimeException {

  // Map of stage name to the InvalidFieldOperations
  private final Map<String, InvalidFieldOperations> invalidFieldOperations;

  public InvalidLineageException(Map<String, InvalidFieldOperations> invalids) {
    super(createErrorMessage(invalids));
    this.invalidFieldOperations = Collections.unmodifiableMap(new HashMap<>(invalids));
  }

  private static String createErrorMessage(Map<String, InvalidFieldOperations> invalids) {
    Map<String, Map<String, List<String>>> invalidOutputs = new HashMap<>();
    Map<String, Map<String, List<String>>> invalidInputs = new HashMap<>();

    for (Map.Entry<String, InvalidFieldOperations> invalidsEntry : invalids.entrySet()) {
      String stageName = invalidsEntry.getKey();
      InvalidFieldOperations stageInvalids = invalidsEntry.getValue();
      if (!stageInvalids.getInvalidInputs().isEmpty()) {
        invalidInputs.put(stageName, stageInvalids.getInvalidInputs());
      }
      if (!stageInvalids.getInvalidOutputs().isEmpty()) {
        invalidOutputs.put(stageName, stageInvalids.getInvalidOutputs());
      }
    }

    String message = invalidOutputErrorMessage(invalidOutputs);
    message += invalidInputErrorMessage(invalidInputs);
    return message;
  }

  private static String invalidErrorMessage(Map<String, Map<String, List<String>>> invalids) {
    StringBuilder stageFieldOperationBuilder = new StringBuilder();
    for (Map.Entry<String, Map<String, List<String>>> unusedOutputs : invalids.entrySet()) {
      if (stageFieldOperationBuilder.length() != 0) {
        stageFieldOperationBuilder.append(", ");
      }

      stageFieldOperationBuilder.append("<stage:");
      stageFieldOperationBuilder.append(unusedOutputs.getKey());
      stageFieldOperationBuilder.append(", ");
      StringBuilder fieldOperationBuilder = new StringBuilder();
      for (Map.Entry<String, List<String>> fieldOperations : unusedOutputs.getValue().entrySet()) {
        for (String operation : fieldOperations.getValue()) {
          if (fieldOperationBuilder.length() != 0) {
            fieldOperationBuilder.append(", ");
          }
          fieldOperationBuilder.append("[operation:");
          fieldOperationBuilder.append(operation);
          fieldOperationBuilder.append(", field:");
          fieldOperationBuilder.append(fieldOperations.getKey());
          fieldOperationBuilder.append("]");
        }
      }
      stageFieldOperationBuilder.append(fieldOperationBuilder);
      stageFieldOperationBuilder.append(">");
    }
    return stageFieldOperationBuilder.toString();
  }

  private static String invalidOutputErrorMessage(
      Map<String, Map<String, List<String>>> invalidOutputs) {
    if (invalidOutputs.isEmpty()) {
      return "";
    }

    return String.format(
        "Outputs of following operations are neither used by subsequent operations "
            + "in that stage nor are part of the output schema of that stage: %s. ",
        invalidErrorMessage(invalidOutputs));
  }

  private static String invalidInputErrorMessage(
      Map<String, Map<String, List<String>>> invalidInputs) {
    if (invalidInputs.isEmpty()) {
      return "";
    }

    return String.format(
        "Inputs of following operations are neither part of the input schema of a stage nor are "
            + "generated by any previous operations recorded by that stage: %s. ",
        invalidErrorMessage(invalidInputs));
  }

  /**
   * @return the map of stage and invalid fields used in that stage along with name of the
   *     operations which are causing these invalids
   */
  public Map<String, InvalidFieldOperations> getInvalidFieldOperations() {
    return Collections.unmodifiableMap(invalidFieldOperations);
  }
}
