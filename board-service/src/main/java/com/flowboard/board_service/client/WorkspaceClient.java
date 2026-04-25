package com.flowboard.board_service.client;

import com.flowboard.board_service.fallback.WorkspaceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "WORKSPACE-SERVICE", fallback = WorkspaceFallback.class)
public interface WorkspaceClient {
    @GetMapping("/api/v1/workspaces/owner/{id}")
    public Integer getOwnerId(@PathVariable Integer id);

    @GetMapping("/api/v1/workspaces/{workspaceId}/member/{memberId}")
    public Boolean isMember(@PathVariable Integer workspaceId,
                            @PathVariable Integer memberId);

    @GetMapping("/api/v1/workspaces/private/{workspaceId}")
    public Boolean isPrivate(@PathVariable Integer workspaceId);
}
