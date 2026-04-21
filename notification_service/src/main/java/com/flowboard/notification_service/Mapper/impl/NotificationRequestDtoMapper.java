package com.flowboard.notification_service.Mapper.impl;

import com.flowboard.notification_service.Mapper.Mapper;
import com.flowboard.notification_service.dto.NotificationRequestDto;
import com.flowboard.notification_service.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationRequestDtoMapper implements Mapper<NotificationRequestDto, Notification> {
    private final ModelMapper modelMapper;
    @Override
    public Notification mapTo(NotificationRequestDto notificationRequestDto) {
        return modelMapper.map(notificationRequestDto, Notification.class);
    }

    @Override
    public NotificationRequestDto mapFrom(Notification notification) {
        return modelMapper.map(notification, NotificationRequestDto.class);
    }
}
