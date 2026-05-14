package com.flowboard.list_service;

import com.flowboard.list_service.config.AppConfig;
import com.flowboard.list_service.dto.TaskListRequestDto;
import com.flowboard.list_service.dto.TaskListResponseDto;
import com.flowboard.list_service.entity.TaskList;
import com.flowboard.list_service.exception.ServiceUnavailableException;
import com.flowboard.list_service.fallback.BoardFallback;
import com.flowboard.list_service.fallback.WorkspaceFallback;
import com.flowboard.list_service.mapper.impl.TaskListRequestMapper;
import com.flowboard.list_service.mapper.impl.TaskListResponseMapper;
import com.flowboard.list_service.util.CustomPageResponse;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

class ListInfrastructureTest {

    @Test
    void mainShouldDelegateToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            ListServiceApplication.main(new String[]{"--test"});

            springApplication.verify(() -> SpringApplication.run(ListServiceApplication.class, new String[]{"--test"}));
        }
    }

    @Test
    void appConfigShouldUseStrictMatchingStrategy() {
        ModelMapper modelMapper = new AppConfig().modelMapper();

        assertEquals(MatchingStrategies.STRICT, modelMapper.getConfiguration().getMatchingStrategy());
    }

    @Test
    void fallbackClientsShouldThrowServiceUnavailableException() {
        BoardFallback boardFallback = new BoardFallback();
        WorkspaceFallback workspaceFallback = new WorkspaceFallback();

        assertThrows(ServiceUnavailableException.class, () -> boardFallback.isMember(1, 1));
        assertThrows(ServiceUnavailableException.class, () -> boardFallback.getWorkspaceId(1));
        assertThrows(ServiceUnavailableException.class, () -> boardFallback.isPrivate(1));
        assertThrows(ServiceUnavailableException.class, () -> workspaceFallback.isMember(1, 1));
        assertThrows(ServiceUnavailableException.class, () -> workspaceFallback.isPrivate(1));
    }

    @Test
    void mappersShouldTranslateBetweenDtosAndEntities() {
        ModelMapper modelMapper = new ModelMapper();
        TaskListRequestMapper requestMapper = new TaskListRequestMapper(modelMapper);
        TaskListResponseMapper responseMapper = new TaskListResponseMapper(modelMapper);

        TaskListRequestDto requestDto = new TaskListRequestDto();
        requestDto.setBoardId(10);
        requestDto.setName("Todo");
        requestDto.setColor("blue");

        TaskList taskList = requestMapper.mapTo(requestDto);
        TaskListRequestDto requestRoundTrip = requestMapper.mapFrom(taskList);

        TaskListResponseDto responseDto = new TaskListResponseDto();
        responseDto.setListId(7);
        responseDto.setName("Doing");
        responseDto.setBoardId("10");
        TaskList entityRoundTrip = responseMapper.mapFrom(responseDto);
        TaskListResponseDto mappedResponse = responseMapper.mapTo(entityRoundTrip);

        assertEquals("Todo", taskList.getName());
        assertEquals("Todo", requestRoundTrip.getName());
        assertEquals(7, entityRoundTrip.getListId());
        assertEquals("Doing", mappedResponse.getName());
    }

    @Test
    void customPageResponseShouldMirrorPageMetadata() {
        TaskListResponseDto dto = new TaskListResponseDto();
        dto.setListId(1);
        PageImpl<TaskListResponseDto> page = new PageImpl<>(List.of(dto));

        CustomPageResponse<TaskListResponseDto> response = new CustomPageResponse<>(page);

        assertEquals(1, response.getNumberOfElements());
        assertEquals(1L, response.getTotalNumberOfElements());
        assertEquals(List.of(dto), response.getContent());
        assertEquals(1, response.getTotalPages());
    }
}
