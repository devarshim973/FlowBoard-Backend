package com.flowboard.comment_service.controller;

import com.flowboard.comment_service.dto.CommentRequestDto;
import com.flowboard.comment_service.dto.CommentResponseDto;
import com.flowboard.comment_service.dto.CommentUpdateDto;
import com.flowboard.comment_service.service.CommentService;
import com.flowboard.comment_service.util.AppConstants;
import com.flowboard.comment_service.util.CustomPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comment Controller", description = "Comment management related APIs")
public class CommentServiceController {
    private final CommentService commentService;

    @Operation(summary = "Add comment", description = "Creates new comment on a card")
    @ApiResponse(responseCode = "201", description = "Comment created successfully")
    @PostMapping("/add")
    public ResponseEntity<CommentResponseDto> handelCreateComment(@Valid @RequestBody CommentRequestDto commentRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addComment(commentRequestDto));
    }

    @Operation(summary = "Get comments by card", description = "Returns paginated comments of card")
    @ApiResponse(responseCode = "200", description = "Comments fetched successfully")
    @GetMapping("/card/{cardId}")
    public ResponseEntity<CustomPageResponse<CommentResponseDto>> handelGetAllByCard(@PathVariable Integer cardId,
                                                                                     @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                                     @RequestParam(value = "size", defaultValue = AppConstants.size) int size,
                                                                                     @RequestParam(value = "sortBy", defaultValue = AppConstants.sortBy) String sortBy,
                                                                                     @RequestParam(value = "direction", defaultValue = AppConstants.direction) String direction) {
        return ResponseEntity.ok().body(commentService.getByCard(cardId, page, size, sortBy, direction));
    }

    @Operation(summary = "Get comment by ID", description = "Returns comment details")
    @ApiResponse(responseCode = "200", description = "Comment fetched successfully")
    @GetMapping("/get/{commentId}")
    public ResponseEntity<CommentResponseDto> handelGetCommentById(@PathVariable Integer commentId) {
        return ResponseEntity.ok().body(commentService.getCommentById(commentId));
    }

    @Operation(summary = "Get replies", description = "Returns paginated replies of parent comment")
    @ApiResponse(responseCode = "200", description = "Replies fetched successfully")
    @GetMapping("/replies/{commentId}")
    public ResponseEntity<CustomPageResponse<CommentResponseDto>> handelGetCommentReplies(@PathVariable Integer commentId,
                                                                      @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                      @RequestParam(value = "size", defaultValue = AppConstants.size) int size,
                                                                      @RequestParam(value = "sortBy", defaultValue = AppConstants.sortBy) String sortBy,
                                                                      @RequestParam(value = "direction", defaultValue = AppConstants.direction) String direction) {
        return ResponseEntity.ok().body(commentService.getReplies(commentId, page, size, sortBy, direction));
    }

    @Operation(summary = "Update comment", description = "Updates comment content")
    @ApiResponse(responseCode = "202", description = "Comment updated successfully")
    @PatchMapping("/update")
    public ResponseEntity<CommentResponseDto> handelUpdateComment(@Valid @RequestBody CommentUpdateDto commentUpdateDto) {
        return ResponseEntity.accepted().body(commentService.updateComment(commentUpdateDto));
    }

    @Operation(summary = "Get comment count", description = "Returns total comments count for card")
    @ApiResponse(responseCode = "200", description = "Count fetched successfully")
    @GetMapping("/count/{cardId}")
    public ResponseEntity<Long>  handelGetCommentCountForCard(@PathVariable Integer cardId) {
        return ResponseEntity.ok().body(commentService.getCommentCount(cardId));
    }

    @Operation(summary = "Delete comment", description = "Deletes comment by ID")
    @ApiResponse(responseCode = "202", description = "Comment deleted successfully")
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<String> handelDeleteComment(@PathVariable Integer commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.accepted().body("Comment deleted successfully");
    }
}
