package io.github.hossensyedriadh.cbs.card.api.controller.v1;

import io.github.hossensyedriadh.cbs.card.api.command.*;
import io.github.hossensyedriadh.cbs.card.api.model.CardView;
import io.github.hossensyedriadh.cbs.card.api.service.CardPinService;
import io.github.hossensyedriadh.cbs.card.api.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/cards/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
public class CardController {
    private final CardService cardService;
    private final CardPinService cardPinService;

    @Autowired
    public CardController(CardService cardService, CardPinService cardPinService) {
        this.cardService = cardService;
        this.cardPinService = cardPinService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/account/{id}")
    public ResponseEntity<List<CardView>> getCards(@PathVariable("id") Long accountId) {
        List<CardView> cardViews = this.cardService.getCards(accountId);

        if (cardViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(cardViews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardView> getCard(@PathVariable("id") Long id) {
        Optional<CardView> cardView = this.cardService.getCard(id);

        return cardView.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardView> issueCard(@RequestBody IssueCardCommand command) {
        CardView cardView = this.cardService.issueCard(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(cardView.getId()).toUri();

        return ResponseEntity.created(location).body(cardView);
    }

    @PostMapping(value = "/replacement", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardView> replaceCard(@RequestBody ReplaceCardCommand command) {
        CardView cardView = this.cardService.replaceCard(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(cardView.getId()).toUri();

        return ResponseEntity.created(location).body(cardView);
    }

    @PostMapping(value = "/activation", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardView> activation(@RequestBody ActivateCardCommand command) {
        CardView cardView = this.cardService.activateCard(command);

        return ResponseEntity.ok(cardView);
    }

    @PostMapping(value = "/change-pin", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> changePin(@RequestBody ChangeCardPinCommand command) {
        this.cardPinService.changePin(command);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/unblock-pin/{cardId}")
    public ResponseEntity<Void> unblockPin(@PathVariable("cardId") Long cardId) {
        this.cardPinService.unblockPin(cardId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/temporary-block", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardView> temporaryBlockCard(@RequestBody BlockCardCommand command) {
        CardView cardView = this.cardService.temporarilyBlockCard(command);

        return ResponseEntity.ok(cardView);
    }

    @PostMapping(value = "/permanent-block", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardView> permanentlyBlockCard(@RequestBody BlockCardCommand command) {
        CardView cardView = this.cardService.permanentlyBlockCard(command);

        return ResponseEntity.ok(cardView);
    }

    @PostMapping(value = "/unblock", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardView> unblockCard(@RequestBody UnblockCardCommand command) {
        CardView cardView = this.cardService.unblockCard(command);

        return ResponseEntity.ok(cardView);
    }

    @PostMapping(value = "/closure", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardView> closeCard(@RequestBody CloseCardCommand command) {
        CardView cardView = this.cardService.closeCard(command);

        return ResponseEntity.ok(cardView);
    }
}
