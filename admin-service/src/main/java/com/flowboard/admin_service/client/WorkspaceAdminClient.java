package com.flowboard.admin_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "WORKSPACE-SERVICE", url = "${services.workspace.url:http://localhost:8084}")
public interface WorkspaceAdminClient {
    @GetMapping("/api/v1/admin/workspaces")
    Object getWorkspaces(@RequestHeader("X-User-Role") String role);

    @DeleteMapping("/api/v1/admin/workspaces/{workspaceId}")
    String deleteWorkspace(@RequestHeader("X-User-Role") String role, @PathVariable("workspaceId") Integer workspaceId);
}
