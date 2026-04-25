package com.flowboard.notification_service.Mapper.impl;

import com.flowboard.notification_service.Mapper.Mapper;
import com.flowboard.notification_service.dto.NotificationResponseDto;
import com.flowboard.notification_service.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationResponseDtoMapper implements Mapper<Notification, NotificationResponseDto> {
    private final ModelMapper modelMapper;
    @Override
    public NotificationResponseDto mapTo(Notification notification) {
        return modelMapper.map(notification, NotificationResponseDto.class);
    }

    @Override
    public Notification mapFrom(NotificationResponseDto notificationResponseDto) {
        return modelMapper.map(notificationResponseDto, Notification.class);
    }
}
