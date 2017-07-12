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

package io.redlink.smarti.api;

import io.redlink.smarti.model.*;
import io.redlink.smarti.model.Token.Type;
import io.redlink.smarti.model.result.Result;
import io.redlink.smarti.services.TemplateRegistry;
import io.redlink.smarti.util.QueryBuilderUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 */
public abstract class QueryBuilder {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final TemplateRegistry registry;

    protected QueryBuilder(TemplateRegistry registry) {
        this.registry = registry;
    }

    public static boolean containsTokenWithType(List<Token> queryTokens, Type type) {
        return containsTokenWithType(queryTokens, type, false);
    }

    public static boolean containsTokenWithType(List<Token> queryTokens, Type type, boolean ignoreNegated) {
        return queryTokens.stream()
                .filter(t -> !(ignoreNegated && t.hasHint(Token.Hint.negated)))
                .filter(t -> t.getType() == type)
                .findFirst().isPresent();
    }


    public final void buildQuery(Conversation conversation) {
        conversation.getTemplates().stream()
                .filter(t -> t.getState() != State.Rejected)
                .filter(t -> {
                    final TemplateDefinition def = registry.getTemplate(t);
                    if(def != null){
                        return def.isValid(t, conversation.getTokens());
                    } else {
                        log.warn("Missing TemplateDefinition for type '{}' (Template: {})",t.getType(),t.getClass().getName());
                        return false;
                    }
                })
                .filter(this::acceptTemplate)
                .forEach(t -> doBuildQuery(t, conversation));
    }

    public abstract boolean acceptTemplate(Template intent);

    protected abstract void doBuildQuery(Template intent, Conversation conversation);
    
    
    
    /**
     * Getter for any (valid) {@link Token} referenced by the parsed
     * {@link Template} with the parsed role
     * @param role the role
     * @param template the query template
     * @param conversation the conversation
     * @param tokenTypes the allowed types of the token
     * @return the Token or <code>null</code> if not preset or invalid
     */
    protected Token getToken(String role, Template template, Conversation conversation, Token.Type...tokenTypes) {
        Set<Token.Type> types = toTypeSet(tokenTypes);
        final Optional<Token> token = template.getSlots().stream()
                .filter(s -> StringUtils.equals(s.getRole(), role))
                .filter(s -> s.getTokenIndex() >= 0)
                .map(s -> conversation.getTokens().get(s.getTokenIndex()))
                .filter(t -> types == null || types.contains(t.getType()))
                .findAny();
        if (token.isPresent()) {
            return token.get();
        } else {
            return null;
        }
    }

    /**
     * Getter for all (valid) {@link Token}s referenced by the parsed
     * {@link Template} with the parsed role
     * @param role the role
     * @param template the query template
     * @param conversation the conversation
     * @param tokenTypes the allowed types of the tokens
     * @return the Token or <code>null</code> if not preset or invalid
     */
    protected List<Token> getTokens(String role, Template template, Conversation conversation, Token.Type...tokenTypes) {
        final Set<Token.Type> types = toTypeSet(tokenTypes);
        return template.getSlots().stream()
                .filter(s -> StringUtils.equals(s.getRole(), role))
                .filter(s -> s.getTokenIndex() >= 0)
                .map(s -> conversation.getTokens().get(s.getTokenIndex()))
                .filter(t -> types == null || types.contains(t.getType()))
                .collect(Collectors.toList());
    }

    /**
     * Converts the parsed array of {@link Type} to an {@link EnumSet}.
     * @param tokenTypes the types to convert
     * @return the {@link EnumSet} with the parsed {@link Type} or <code>null</code> if no types where parsed
     */
    private Set<Token.Type> toTypeSet(Token.Type... tokenTypes) {
        if(tokenTypes == null || tokenTypes.length < 1) {
            return null;
        } else {
            Set<Token.Type> types = EnumSet.noneOf(Token.Type.class);
            for(Token.Type type : tokenTypes){
                if(type != null){
                    types.add(type);
                }
            }
            return types.isEmpty() ? null : types;
        }
    }

    /**
     * Check if the template has this role assigned to a (valid) token.
     * @param role the role
     * @param template the query template
     * @param conversation the conversation
     * @param tokenTypes the allowed token type
     * @return {@code true} if the template has a token assigned to the provided role
     */
    protected boolean hasToken(String role, Template template, Conversation conversation, Token.Type...tokenTypes) {
        final Set<Token.Type> types = toTypeSet(tokenTypes);
        return template.getSlots().stream()
                .filter(s -> StringUtils.equals(s.getRole(), role))
                .filter(s -> s.getTokenIndex() >= 0)
                .map(s -> conversation.getTokens().get(s.getTokenIndex()))
                .filter(t -> types == null || types.contains(t.getType()))
                .findFirst().isPresent();
    }

    public List<? extends Result> execute(Template template, Conversation conversation) throws IOException {
        return Collections.emptyList();
    }

    public boolean isResultSupported() {
        return false;
    }

    public String getCreatorName() {
        return QueryBuilderUtils.getQueryBuilderName(getClass());
    }

}
