package com.flowboard.comment_service.mapper.impl;

import com.flowboard.comment_service.dto.AttachmentRequestDto;
import com.flowboard.comment_service.entity.Attachment;
import com.flowboard.comment_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttachmentRequestMapper implements Mapper<AttachmentRequestDto, Attachment> {
    private final ModelMapper modelMapper;

    @Override
    public Attachment mapTo(AttachmentRequestDto attachmentRequestDto) {
        return modelMapper.map(attachmentRequestDto, Attachment.class);
    }

    @Override
    public AttachmentRequestDto mapFrom(Attachment attachment) {
        return modelMapper.map(attachment, AttachmentRequestDto.class);
    }
}
