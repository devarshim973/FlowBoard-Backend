package com.flowboard.comment_service.controller;

import com.flowboard.comment_service.dto.CommentRequestDto;
import com.flowboard.comment_service.dto.CommentResponseDto;
import com.flowboard.comment_service.dto.CommentUpdateDto;
import com.flowboard.comment_service.service.CommentService;
import com.flowboard.comment_service.util.AppConstants;
import com.flowboard.comment_service.util.CustomPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentServiceController {
    private final CommentService commentService;
    @PostMapping("/add")
    public ResponseEntity<CommentResponseDto> handelCreateComment(@Valid @RequestBody CommentRequestDto commentRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addComment(commentRequestDto));
    }

    @GetMapping("/card/{cardId}")
    public ResponseEntity<CustomPageResponse<CommentResponseDto>> handelGetAllByCard(@PathVariable Integer cardId,
                                                                                     @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                                     @RequestParam(value = "size", defaultValue = AppConstants.size) int size,
                                                                                     @RequestParam(value = "sortBy", defaultValue = AppConstants.sortBy) String sortBy,
                                                                                     @RequestParam(value = "direction", defaultValue = AppConstants.direction) String direction) {
        return ResponseEntity.ok().body(commentService.getByCard(cardId, page, size, sortBy, direction));
    }

    @GetMapping("/get/{commentId}")
    public ResponseEntity<CommentResponseDto> handelGetCommentById(@PathVariable Integer commentId) {
        return ResponseEntity.ok().body(commentService.getCommentById(commentId));
    }

    @GetMapping("/replies/{commentId}")
    public ResponseEntity<CustomPageResponse<CommentResponseDto>> handelGetCommentReplies(@PathVariable Integer commentId,
                                                                      @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                      @RequestParam(value = "size", defaultValue = AppConstants.size) int size,
                                                                      @RequestParam(value = "sortBy", defaultValue = AppConstants.sortBy) String sortBy,
                                                                      @RequestParam(value = "direction", defaultValue = AppConstants.direction) String direction) {
        return ResponseEntity.ok().body(commentService.getReplies(commentId, page, size, sortBy, direction));
    }

    @PatchMapping("/update")
    public ResponseEntity<CommentResponseDto> handelUpdateComment(@Valid @RequestBody CommentUpdateDto commentUpdateDto) {
        return ResponseEntity.accepted().body(commentService.updateComment(commentUpdateDto));
    }

    @GetMapping("/count/{cardId}")
    public ResponseEntity<Long>  handelGetCommentCountForCard(@PathVariable Integer cardId) {
        return ResponseEntity.ok().body(commentService.getCommentCount(cardId));
    }

    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<String> handelDeleteComment(@PathVariable Integer commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.accepted().body("Comment deleted successfully");
    }
}
