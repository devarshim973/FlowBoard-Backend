package com.flowboard.comment_service;

import com.cloudinary.Cloudinary;
import com.flowboard.comment_service.config.AppConfig;
import com.flowboard.comment_service.config.CloudinaryConfig;
import com.flowboard.comment_service.config.RabbitMQConfig;
import com.flowboard.comment_service.dto.AttachmentRequestDto;
import com.flowboard.comment_service.dto.AttachmentResponseDto;
import com.flowboard.comment_service.dto.BulkNotificationRequestDto;
import com.flowboard.comment_service.dto.CommentRequestDto;
import com.flowboard.comment_service.dto.CommentResponseDto;
import com.flowboard.comment_service.entity.Attachment;
import com.flowboard.comment_service.entity.Comment;
import com.flowboard.comment_service.exception.AttachmentNotFoundException;
import com.flowboard.comment_service.exception.CommentNotFoundException;
import com.flowboard.comment_service.exception.FileException;
import com.flowboard.comment_service.exception.GlobalExceptionHandler;
import com.flowboard.comment_service.exception.ServiceUnavailableException;
import com.flowboard.comment_service.fallback.CardFallback;
import com.flowboard.comment_service.fallback.NotificationFallback;
import com.flowboard.comment_service.fallback.UserFallback;
import com.flowboard.comment_service.mapper.impl.AttachmentRequestMapper;
import com.flowboard.comment_service.mapper.impl.AttachmentResponseMapper;
import com.flowboard.comment_service.mapper.impl.CommentRequestMapper;
import com.flowboard.comment_service.mapper.impl.CommentResponseMapper;
import com.flowboard.comment_service.util.AppConstants;
import com.flowboard.comment_service.util.CustomPageResponse;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentInfrastructureTest {

    private final AppConfig appConfig = new AppConfig();
    private final ModelMapper modelMapper = appConfig.modelMapper();

    @Test
    void appConfigCreatesStrictModelMapper() {
        assertEquals(MatchingStrategies.STRICT, modelMapper.getConfiguration().getMatchingStrategy());
    }

    @Test
    void cloudinaryConfigBuildsClientFromProperties() {
        CloudinaryConfig config = new CloudinaryConfig();
        ReflectionTestUtils.setField(config, "cloudName", "flowboard-cloud");
        ReflectionTestUtils.setField(config, "apiKey", "abc123");
        ReflectionTestUtils.setField(config, "apiSecret", "secret456");

        Cloudinary cloudinary = config.cloudinary();

        assertNotNull(cloudinary);
        assertEquals("flowboard-cloud", cloudinary.config.cloudName);
        assertEquals("abc123", cloudinary.config.apiKey);
    }

    @Test
    void rabbitMqConfigCreatesQueuesExchangeBindingsAndConverter() {
        RabbitMQConfig config = new RabbitMQConfig();
        ReflectionTestUtils.setField(config, "singleQueue", "single.queue");
        ReflectionTestUtils.setField(config, "bulkQueue", "bulk.queue");
        ReflectionTestUtils.setField(config, "exchange", "notification.exchange");
        ReflectionTestUtils.setField(config, "singleRoutingKey", "single.key");
        ReflectionTestUtils.setField(config, "bulkRoutingKey", "bulk.key");

        Queue singleQueue = config.singleNotificationQueue();
        Queue bulkQueue = config.bulkNotificationQueue();
        DirectExchange exchange = config.notificationExchange();
        Binding singleBinding = config.singleNotificationBinding(singleQueue, exchange);
        Binding bulkBinding = config.bulkNotificationBinding(bulkQueue, exchange);

        assertEquals("single.queue", singleQueue.getName());
        assertTrue(singleQueue.isDurable());
        assertEquals("bulk.queue", bulkQueue.getName());
        assertEquals("notification.exchange", exchange.getName());
        assertEquals("single.key", singleBinding.getRoutingKey());
        assertEquals("bulk.key", bulkBinding.getRoutingKey());
        assertTrue(config.jsonMessageConverter() instanceof Jackson2JsonMessageConverter);
    }

    @Test
    void fallbackClassesThrowExpectedServiceUnavailableException() {
        assertEquals(
                "Card service not available",
                assertThrows(ServiceUnavailableException.class, () -> new CardFallback().getAssignedUserId(5)).getMessage()
        );
        assertEquals(
                "Notification service not available",
                assertThrows(ServiceUnavailableException.class, () -> new NotificationFallback().sendBulk(new BulkNotificationRequestDto())).getMessage()
        );
        assertEquals(
                "User service not available",
                assertThrows(ServiceUnavailableException.class, () -> new UserFallback().getUserIdsByUsername(List.of("a@b.com"))).getMessage()
        );
    }

    @Test
    void globalExceptionHandlerReturnsBadRequestResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        assertEquals(
                "comment missing",
                handler.handelCommentNotFound(new CommentNotFoundException("comment missing")).getBody()
        );
        assertEquals(
                "bad file",
                handler.handelLargeFileSizeException(new FileException("bad file")).getBody()
        );
        assertEquals(
                "attachment missing",
                handler.handelAttachmentNotException(new AttachmentNotFoundException("attachment missing")).getBody()
        );
        assertEquals(
                HttpStatus.BAD_REQUEST,
                handler.handelAllException(new Exception("generic")).getStatusCode()
        );
    }

    @Test
    void attachmentMappersMapBothDirections() {
        AttachmentRequestDto requestDto = new AttachmentRequestDto();
        requestDto.setCardId(4);
        requestDto.setCommentId(7);
        requestDto.setUploaderId(9);

        AttachmentRequestMapper requestMapper = new AttachmentRequestMapper(modelMapper);
        Attachment attachment = requestMapper.mapTo(requestDto);
        assertEquals(4, attachment.getCardId());
        assertEquals(7, attachment.getCommentId());
        assertEquals(9, attachment.getUploaderId());

        Attachment entity = new Attachment();
        entity.setAttachmentId(12);
        entity.setCardId(4);
        entity.setUploaderId(9);
        entity.setFileName("mock.pdf");
        entity.setFileUrl("http://file");
        entity.setPublicId("cloud-id");
        entity.setFileType("application/pdf");
        entity.setSizeKb(44L);
        entity.setUploadedAt(LocalDateTime.now());

        AttachmentResponseMapper responseMapper = new AttachmentResponseMapper(modelMapper);
        AttachmentResponseDto responseDto = responseMapper.mapTo(entity);
        assertEquals(12, responseDto.getAttachmentId());
        assertEquals("mock.pdf", responseDto.getFileName());

        Attachment mappedBack = responseMapper.mapFrom(responseDto);
        assertEquals("http://file", mappedBack.getFileUrl());
        assertEquals(44L, mappedBack.getSizeKb());
    }

    @Test
    void commentMappersMapBothDirections() {
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setCardId(2);
        requestDto.setAuthorId(6);
        requestDto.setContent("Need review");
        requestDto.setParentCommentId(1);

        CommentRequestMapper requestMapper = new CommentRequestMapper(modelMapper);
        Comment comment = requestMapper.mapTo(requestDto);
        assertEquals(2, comment.getCardId());
        assertEquals("Need review", comment.getContent());

        Comment entity = new Comment();
        entity.setCommentId(14);
        entity.setCardId(2);
        entity.setAuthorId(6);
        entity.setContent("Updated");
        entity.setIsDeleted(true);
        entity.setParentCommentId(1);
        entity.setCreatedAt(LocalDateTime.now().minusHours(2));
        entity.setUpdatedAt(LocalDateTime.now());

        CommentResponseMapper responseMapper = new CommentResponseMapper(modelMapper);
        CommentResponseDto responseDto = responseMapper.mapTo(entity);
        assertEquals(14, responseDto.getCommentId());
        assertTrue(responseDto.getIsDeleted());

        Comment mappedBack = responseMapper.mapFrom(responseDto);
        assertEquals(6, mappedBack.getAuthorId());
        assertTrue(mappedBack.getIsDeleted());
        assertFalse(mappedBack.getParentCommentId() == null);
    }

    @Test
    void applicationAnnotationsRemainPresent() {
        assertTrue(CommentServiceApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class));
        assertTrue(CommentServiceApplication.class.isAnnotationPresent(org.springframework.cloud.openfeign.EnableFeignClients.class));
    }

    @Test
    void utilClassesExposeExpectedDefaults() {
        CommentResponseDto responseDto = new CommentResponseDto();
        responseDto.setCommentId(21);

        CustomPageResponse<CommentResponseDto> pageResponse =
                new CustomPageResponse<>(new PageImpl<>(List.of(responseDto), PageRequest.of(0, 10), 1));

        assertEquals(10, pageResponse.getPageSize());
        assertEquals(0, pageResponse.getPageNumber());
        assertEquals(1, pageResponse.getNumberOfElements());
        assertEquals(1, pageResponse.getTotalPages());
        assertEquals(1L, pageResponse.getTotalNumberOfElements());
        assertEquals(21, pageResponse.getContent().get(0).getCommentId());
        assertTrue(pageResponse.isFirst());
        assertTrue(pageResponse.isLast());

        assertEquals("0", AppConstants.page);
        assertEquals("10", AppConstants.size);
        assertEquals("commentId", AppConstants.sortBy);
        assertEquals("asc", AppConstants.direction);
        assertEquals(10240L, AppConstants.maxFileSize);
        assertTrue(AppConstants.allowedFileFormat.contains("application/pdf"));
        assertTrue(AppConstants.allowedFileFormat.contains("image/png"));
        assertTrue(AppConstants.allowedFileFormat.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
    }
}
