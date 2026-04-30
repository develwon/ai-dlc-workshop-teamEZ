package com.tableorder.controller.customer;

import com.tableorder.dto.TableLoginRequest;
import com.tableorder.dto.TableLoginResponse;
import com.tableorder.service.TableAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableAuthController {

    private final TableAuthService tableAuthService;

    @PostMapping("/login")
    public ResponseEntity<TableLoginResponse> login(
            @Valid @RequestBody TableLoginRequest request) {
        TableLoginResponse response = tableAuthService.authenticate(request);
        return ResponseEntity.ok(response);
    }
}
