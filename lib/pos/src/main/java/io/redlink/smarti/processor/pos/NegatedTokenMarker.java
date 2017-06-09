/*
 * Copyright (c) 2016 - 2017 Redlink GmbH
 */

package io.redlink.smarti.processor.pos;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.ProcessingException;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.Chunk;
import io.redlink.nlp.model.Section;
import io.redlink.nlp.model.util.NlpUtils;
import io.redlink.smarti.model.Conversation;
import io.redlink.smarti.model.Token;
import io.redlink.smarti.model.Token.Hint;

import org.springframework.stereotype.Component;

import static io.redlink.nlp.model.NlpAnnotations.NEGATION_ANNOTATION;
import static io.redlink.smarti.processing.SmartiAnnotations.CONVERSATION_ANNOTATION;
import static io.redlink.smarti.processing.SmartiAnnotations.MESSAGE_IDX_ANNOTATION;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class NegatedTokenMarker extends Processor {

    
    public NegatedTokenMarker() {
        super("negation.tokenmarker","Negated Token Marker",Phase.post,100); //POST
    }
    
    @Override
    public Map<String, Object> getDefaultConfiguration() {
        return Collections.emptyMap();
    }
    
    @Override
    protected void init() throws Exception {
        //no op
    }
    
    @Override
    protected void doProcessing(ProcessingData processingData) throws ProcessingException {
        Optional<AnalyzedText> ato = NlpUtils.getAnalyzedText(processingData);
        if(!ato.isPresent()){
            return; //nothing to do
        }
        AnalyzedText at = ato.get();
        Conversation conv = processingData.getAnnotation(CONVERSATION_ANNOTATION);
        if(conv == null){
            log.warn("parsed {} does not have a '{}' annotation", processingData, CONVERSATION_ANNOTATION);
            return;
        }
        int startIdx = conv.getMeta().getLastMessageAnalyzed() + 1;
        Iterator<Section> sections = at.getSections();
        while(sections.hasNext()){
            Section section = sections.next();
            Integer msgIdx = section.getAnnotation(MESSAGE_IDX_ANNOTATION);
            if(msgIdx != null && msgIdx >= startIdx ){
                List<Token> msgTokens = conv.getTokens().stream()
                        .filter(t -> t.getMessageIdx() == msgIdx)
                        .collect(Collectors.toList());
                if(!msgTokens.isEmpty()){
                    int offset = section.getStart();
                    Iterator<Chunk> chunks = section.getChunks();
                    while(chunks.hasNext()){
                        Chunk chunk = chunks.next();
                        Boolean negation = chunk.getAnnotation(NEGATION_ANNOTATION);
                        if(Boolean.TRUE.equals(negation)){
                            int negStart = chunk.getStart() - offset;
                            int negEnd = chunk.getEnd() - offset;
                            for(Token token : msgTokens){
                                if(token.getStart() >= negStart && token.getEnd() <= negEnd){
                                    token.addHint(Hint.negated);
                                    log.debug("  - negate {}", token);
                                }
                            }
                        }
                    }
                }
                
                
            }
        }

    }

}