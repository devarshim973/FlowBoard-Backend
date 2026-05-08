package com.flowboard.comment_service.mapper.impl;

import com.flowboard.comment_service.dto.AttachmentResponseDto;
import com.flowboard.comment_service.entity.Attachment;
import com.flowboard.comment_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttachmentResponseMapper implements Mapper<Attachment, AttachmentResponseDto> {
    private final ModelMapper modelMapper;

    @Override
    public AttachmentResponseDto mapTo(Attachment attachment) {
        return modelMapper.map(attachment, AttachmentResponseDto.class);
    }

    @Override
    public Attachment mapFrom(AttachmentResponseDto attachmentResponseDto) {
        return modelMapper.map(attachmentResponseDto, Attachment.class);
    }
}
