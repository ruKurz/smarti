/*
 * Copyright (c) 2016 Redlink GmbH
 */
package io.redlink.smarti.processor.hasso;

import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.regex.ner.csv.CsvVocabularyNerDetector;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 */
@Component
public class QuestionIdentifierVocab extends CsvVocabularyNerDetector {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String VOCAB = "wordlists/QuestionIdentifier.csv";

    public QuestionIdentifierVocab() {
        super("QuestionIdentifier", new NerTag("QuestionIdentifier"), Locale.GERMAN, false);
    }
    
    //TODO: HINT functionality removed

    @Override
    protected Reader readFrom() {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(VOCAB), UTF8);
    }
}