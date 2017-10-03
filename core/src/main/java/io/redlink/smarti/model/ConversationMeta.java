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

package io.redlink.smarti.model;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Useful metadata about a conversation
 */
@ApiModel
public class ConversationMeta {

    /**
     * The key used to store the support area of the channel
     */
    public static final String PROP_SUPPORT_AREA = "support_area";

    /**
     * The key to store the user token of the conversation
     */
    public static final String PROP_TOKEN = "token";

    /**
     * The key to store the id of the channel the conversation was part of
     */
    public static final String PROP_CHANNEL_ID = "channel_id";

    /**
     * The key to store the name of the channel the conversation was part of
     */
    public static final String PROP_CHANNEL_NAME = "channel";

    /**
     * The key to store tags of a conversation
     */
    public static final String PROP_TAGS = "tags";

    public enum Status {
        New,
        Ongoing,
        Complete
    }

    @ApiModelProperty("conversation status")
    private Status status = Status.New;

    @ApiModelProperty(value = "message offset", notes = "offset for the next analysis iteration")
    private int lastMessageAnalyzed = -1;

    private Map<String,List<String>> properties = new HashMap<>();

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getLastMessageAnalyzed() {
        return lastMessageAnalyzed;
    }

    public void setLastMessageAnalyzed(int lastMessageAnalyzed) {
        this.lastMessageAnalyzed = lastMessageAnalyzed;
    }

    @JsonAnyGetter
    public Map<String, List<String>> getProperties() {
        return properties;
    }
    
    @JsonAnySetter
    public void setProperty(String key, List<String> values){
        if(CollectionUtils.isEmpty(values)){
            removeProperty(key);
        } else {
            properties.put(key, values);
        }
    }
    public void setProperty(String key, String value){
        if(value == null){
            removeProperty(key);
        } else {
            List<String> list = new ArrayList<>(1);
            list.add(value);
            setProperty(key, list);
        }
    }
    
    public List<String> getProperty(String key){
        return getProperty(key, null);
    }
    
    public List<String> getProperty(String key, List<String> defaultValue){
        return properties.getOrDefault(key, defaultValue);
    }
    
    public List<String> removeProperty(String key){
        return properties.remove(key);
    }
    
    public void addPropertyValue(String key, String value){
        if(value != null){
            List<String> values = properties.get(key);
            if(values == null){
                values = new ArrayList<>();
                setProperty(key, values);
            }
            values.add(value);
        }
    }

}
