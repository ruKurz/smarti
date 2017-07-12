/*
 * Copyright 2017 Redlink GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.redlink.smarti.model.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * A result/preview of a Query
 */
@ApiModel
public abstract class Result {

    @ApiModelProperty(required = true)
    @JsonProperty("creator")
    private String creator;

    @ApiModelProperty(value = "reply", notes = "a suggested answer, ready to be sent to the customer")
    private String replySuggestion;

    protected Result(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getReplySuggestion() {
        return replySuggestion;
    }

    public void setReplySuggestion(String replySuggestion) {
        this.replySuggestion = replySuggestion;
    }

}
