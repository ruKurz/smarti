/*
 * Copyright (c) 2017 Redlink GmbH.
 */
package io.redlink.smarti.webservice;

import io.redlink.smarti.api.StoreService;
import io.redlink.smarti.model.*;
import io.redlink.smarti.model.result.Result;
import io.redlink.smarti.services.ConversationService;
import io.redlink.smarti.utils.ResponseEntities;
import io.redlink.smarti.webservice.pojo.TemplateResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


/**
 *
 */
@CrossOrigin
@RestController
@RequestMapping(value = "conversation",
        produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
@Api("conversation")
public class ConversationWebservice {

    private enum Vote {
        up(1),
        down(-1);

        private final int delta;

        Vote(int delta) {
            this.delta = delta;
        }

        public int getDelta() {
            return delta;
        }
    }

    @Autowired
    private StoreService storeService;

    @Autowired
    private ConversationService conversationService;

    @ApiOperation(value = "create a conversation")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", response = Conversation.class)
    })
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> createConversation(@RequestBody(required = false) Conversation conversation) {
        conversation = Optional.ofNullable(conversation).orElseGet(Conversation::new);
        // Create a new Conversation -> id must be null
        conversation.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.store(conversation));
    }

    @ApiOperation(value = "retrieve a conversation", response = Conversation.class)
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getConversation(@PathVariable("id") ObjectId id) {
        final Conversation conversation = storeService.get(id);

        if (conversation == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(conversation);
        }
    }

    @ApiOperation(value = "update a conversation", response = Conversation.class)
    @RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateConversation(@PathVariable("id") ObjectId id,
                                                @RequestBody Conversation conversation) {
        // make sure the id is the right one
        conversation.setId(id);
        // todo: some additional checks?
        return ResponseEntity.ok(storeService.store(conversation));
    }

    @ApiOperation(value = "append a message to the conversation", response = Conversation.class)
    @RequestMapping(value = "{id}/message", method = RequestMethod.POST, consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addMessage(@PathVariable("id") ObjectId id,
                                        @RequestBody Message message) {
        final Conversation conversation = storeService.get(id);
        if (conversation == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(conversationService.appendMessage(conversation, message));
    }

    @ApiOperation(value = "up-/down-vote a message within a conversation", response = Conversation.class)
    @RequestMapping(value = "{id}/message/{messageId}/{vote}", method = RequestMethod.PUT)
    public ResponseEntity<?> rateMessage(@PathVariable("id") ObjectId conversationId,
                                         @PathVariable("messageId") String messageId,
                                         @PathVariable("vote") Vote vote) {
        final Conversation conversation = storeService.get(conversationId);
        if (conversation == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(conversationService.rateMessage(conversation, messageId, vote.getDelta()));
    }

    @ApiOperation(value = "retrieve the analysis result of the conversation", response = Token.class, responseContainer = "List")
    @RequestMapping(value = "{id}/analysis", method = RequestMethod.GET)
    public ResponseEntity<?> prepare(@PathVariable("id") ObjectId id) {
        final Conversation conversation = storeService.get(id);

        if (conversation == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(conversation.getTokens());
        }
    }

    @ApiOperation(value = "retrieve the intents of the conversation", response = TemplateResponse.class)
    @RequestMapping(value = "{id}/intent", method = RequestMethod.GET)
    public ResponseEntity<?> query(@PathVariable("id") ObjectId id) {
        final Conversation conversation = storeService.get(id);

        if (conversation == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(TemplateResponse.from(conversation));
        }
    }

    @ApiOperation(value = "update a query based on new slot-assignments", response = Query.class)
    @RequestMapping(value = "{id}/query/{intent}/{creator}", method = RequestMethod.POST, consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getQuery(@PathVariable("id") ObjectId id,
                                      @PathVariable("intent") String intent,
                                      @PathVariable("creator") String creator,
                                      @RequestBody List<Slot> updatedSlots) {
        // TODO: Implement this
        return ResponseEntities.notImplemented();
    }

    @ApiOperation(value = "retrieve the results for a intent from a specific creator", response = Result.class, responseContainer = "List")
    @RequestMapping(value = "{id}/intent/{intent}/{creator}", method = RequestMethod.GET)
    public ResponseEntity<?> getResults(@PathVariable("id") ObjectId id,
                                        @PathVariable("intent") String intent,
                                        @PathVariable("creator") String creator) {
        // TODO: Implement this
        return ResponseEntities.notImplemented();
    }

    @ApiOperation(value = "complete a conversation and add it to indexing", response = Conversation.class)
    @RequestMapping(value = "{id}/publish", method = RequestMethod.POST)
    public ResponseEntity<?> complete(@PathVariable("id") ObjectId id) {
        final Conversation conversation = storeService.get(id);

        if (conversation == null) {
            return ResponseEntity.notFound().build();
        } else {
            conversation.getMeta().setStatus(ConversationMeta.Status.Complete);
            return ResponseEntity.ok(conversationService.completeConversation(conversation));
        }
    }
}
