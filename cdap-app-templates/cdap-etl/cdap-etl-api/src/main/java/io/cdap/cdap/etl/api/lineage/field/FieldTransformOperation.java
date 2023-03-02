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

package io.cdap.cdap.etl.api.lineage.field;

import io.cdap.cdap.api.annotation.Beta;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represent the transform operation from collection of input fields to collection of output
 * fields.
 */
@Beta
public class FieldTransformOperation extends FieldOperation {

  private final List<String> inputFields;
  private final List<String> outputFields;

  /**
   * Create the instance of a transform operation.
   *
   * @param name the name of the operation
   * @param description the description of the operation
   * @param inputFields the list of input fields for operation
   * @param outputFields the array of output fields generated by operation
   */
  public FieldTransformOperation(String name, String description, List<String> inputFields,
      String... outputFields) {
    this(name, description, inputFields, Arrays.asList(outputFields));
  }

  /**
   * Create the instance of a transform operation.
   *
   * @param name the name of the operation
   * @param description the description of the operation
   * @param inputFields the list of input fields for operation
   * @param outputFields the list of output fields generated by operation
   */
  public FieldTransformOperation(String name, String description, List<String> inputFields,
      List<String> outputFields) {
    super(name, OperationType.TRANSFORM, description);
    this.inputFields = Collections.unmodifiableList(new ArrayList<>(inputFields));
    this.outputFields = Collections.unmodifiableList(new ArrayList<>(outputFields));
  }

  /**
   * @return the list of input fields for this transform operation
   */
  public List<String> getInputFields() {
    return inputFields;
  }

  /**
   * @return the list of output fields generated by this transform operation
   */
  public List<String> getOutputFields() {
    return outputFields;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    FieldTransformOperation that = (FieldTransformOperation) o;
    return Objects.equals(inputFields, that.inputFields)
        && Objects.equals(outputFields, that.outputFields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), inputFields, outputFields);
  }
}
