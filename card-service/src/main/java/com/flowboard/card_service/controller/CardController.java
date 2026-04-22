package com.flowboard.card_service.controller;

import com.flowboard.card_service.dto.CardRequestDto;
import com.flowboard.card_service.dto.CardResponseDto;
import com.flowboard.card_service.dto.CardUpdateDto;
import com.flowboard.card_service.entity.Priority;
import com.flowboard.card_service.entity.Status;
import com.flowboard.card_service.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Card Controller", description = "Card management related APIs")
public class CardController {

    private final CardService cardService;

    private Integer extractUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");

        if (userIdHeader == null) {
            throw new RuntimeException("Missing X-User-Id header");
        }

        return Integer.parseInt(userIdHeader);
    }

    @Operation(summary = "Create card", description = "Creates a new card")
    @ApiResponse(responseCode = "201", description = "Card created successfully")
    @PostMapping("/create")
    public ResponseEntity<CardResponseDto> createCard(@RequestBody CardRequestDto dto,
                                                      HttpServletRequest request) {
        Integer userId = extractUserId(request);
        log.info("Creating card");
        CardResponseDto card = cardService.createCard(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(card);
    }

    @Operation(summary = "Get card by ID", description = "Returns card details")
    @ApiResponse(responseCode = "200", description = "Card fetched successfully")
    @GetMapping("/get/{cardId}")
    public ResponseEntity<CardResponseDto> getCard(@PathVariable Integer cardId,
                                                   HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.getCardById(cardId, userId));
    }

    @Operation(summary = "Get cards by list", description = "Returns all cards of given list")
    @ApiResponse(responseCode = "200", description = "Cards fetched successfully")
    @GetMapping("/get/list/{listId}")
    public ResponseEntity<List<CardResponseDto>> getCardsByList(@PathVariable Integer listId,
                                                                HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.getCardsByList(listId, userId));
    }

    @Operation(summary = "Get cards by board", description = "Returns all cards of given board")
    @ApiResponse(responseCode = "200", description = "Cards fetched successfully")
    @GetMapping("/get/board/{boardId}")
    public ResponseEntity<List<CardResponseDto>> getCardsByBoard(@PathVariable Integer boardId,
                                                                 HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.getCardsByBoard(boardId, userId));
    }

    @Operation(summary = "Get cards by assignee", description = "Returns cards assigned to user")
    @ApiResponse(responseCode = "200", description = "Cards fetched successfully")
    @GetMapping("/get/assignee/{assigneeId}")
    public ResponseEntity<List<CardResponseDto>> getCardsByAssignee(@PathVariable Integer assigneeId,
                                                                    HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.getCardsByAssignee(assigneeId, userId));
    }

    @Operation(summary = "Update card", description = "Updates card details")
    @ApiResponse(responseCode = "200", description = "Card updated successfully")
    @PutMapping("/update/{cardId}")
    public ResponseEntity<CardResponseDto> updateCard(@PathVariable Integer cardId,
                                                      @RequestBody CardUpdateDto dto,
                                                      HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.updateCard(cardId, dto, userId));
    }

    @Operation(summary = "Delete card", description = "Deletes card by ID")
    @ApiResponse(responseCode = "200", description = "Card deleted successfully")
    @DeleteMapping("/delete/{cardId}")
    public ResponseEntity<String> deleteCard(@PathVariable Integer cardId,
                                             HttpServletRequest request) {
        Integer userId = extractUserId(request);
        cardService.deleteCard(cardId, userId);
        return ResponseEntity.ok("Deleted successfully");
    }

    @Operation(summary = "Move card", description = "Moves card to another list and position")
    @ApiResponse(responseCode = "200", description = "Card moved successfully")
    @PutMapping("/{cardId}/move")
    public ResponseEntity<CardResponseDto> moveCard(@PathVariable Integer cardId,
                                                    @RequestParam Integer targetListId,
                                                    @RequestParam Integer position,
                                                    HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.moveCard(cardId, targetListId, position, userId));
    }

    @Operation(summary = "Reorder cards", description = "Reorders cards inside a list")
    @ApiResponse(responseCode = "200", description = "Cards reordered successfully")
    @PutMapping("/list/{listId}/reorder")
    public ResponseEntity<String> reorderCards(@PathVariable Integer listId,
                                               @RequestBody List<Integer> orderedCardIds,
                                               HttpServletRequest request) {
        Integer userId = extractUserId(request);
        cardService.reorderCards(listId, orderedCardIds, userId);
        return ResponseEntity.ok("Reordered successfully");
    }

    @Operation(summary = "Assign card", description = "Assigns card to user")
    @ApiResponse(responseCode = "200", description = "Card assigned successfully")
    @PutMapping("/{cardId}/assign")
    public ResponseEntity<CardResponseDto> assignCard(@PathVariable Integer cardId,
                                                      @RequestParam Integer assigneeId,
                                                      HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.assignCard(cardId, assigneeId, userId));
    }

    @Operation(summary = "Update priority", description = "Updates card priority")
    @ApiResponse(responseCode = "200", description = "Priority updated successfully")
    @PutMapping("/{cardId}/priority")
    public ResponseEntity<CardResponseDto> updatePriority(@PathVariable Integer cardId,
                                                          @RequestParam Priority priority,
                                                          HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.updatePriority(cardId, priority, userId));
    }

    @Operation(summary = "Update status", description = "Updates card status")
    @ApiResponse(responseCode = "200", description = "Status updated successfully")
    @PutMapping("/{cardId}/status")
    public ResponseEntity<CardResponseDto> updateStatus(@PathVariable Integer cardId,
                                                        @RequestParam Status status,
                                                        HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.updateStatus(cardId, status, userId));
    }

    @Operation(summary = "Archive card", description = "Archives card")
    @ApiResponse(responseCode = "200", description = "Card archived successfully")
    @PutMapping("/{cardId}/archive")
    public ResponseEntity<String> archiveCard(@PathVariable Integer cardId,
                                              HttpServletRequest request) {
        Integer userId = extractUserId(request);
        cardService.archiveCard(cardId, userId);
        return ResponseEntity.ok("Archived");
    }

    @Operation(summary = "Unarchive card", description = "Restores archived card")
    @ApiResponse(responseCode = "200", description = "Card unarchived successfully")
    @PutMapping("/{cardId}/unarchive")
    public ResponseEntity<String> unarchiveCard(@PathVariable Integer cardId,
                                                HttpServletRequest request) {
        Integer userId = extractUserId(request);
        cardService.unarchiveCard(cardId, userId);
        return ResponseEntity.ok("Unarchived");
    }

    @Operation(summary = "Get overdue cards", description = "Returns overdue cards of logged user")
    @ApiResponse(responseCode = "200", description = "Overdue cards fetched successfully")
    @GetMapping("/overdue")
    public ResponseEntity<List<CardResponseDto>> getOverdueCards(HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.getOverdueCards(userId));
    }

    @Operation(summary = "Get assigned user id", description = "Returns the user id to whome the card is assigned")
    @ApiResponse(responseCode = "200", description = "User id returned successfully")
    @GetMapping("/assigned-user/{cardId}")
    public Integer getAssignedUserId(@PathVariable(value = "cardId") Integer cardId) {
        return cardService.getAssignedUserId(cardId);
    }
}