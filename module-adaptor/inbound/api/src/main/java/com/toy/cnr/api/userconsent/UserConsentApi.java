package com.toy.cnr.api.userconsent;

import com.toy.cnr.api.common.util.ResponseMapper;
import com.toy.cnr.api.userconsent.request.UserConsentCreateRequest;
import com.toy.cnr.api.userconsent.response.UserConsentResponse;
import com.toy.cnr.api.userconsent.usecase.UserConsentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "UserConsent", description = "이용 동의 API")
@RestController
@RequestMapping("/v1/user-consent")
public class UserConsentApi {

    private final UserConsentUseCase userConsentUseCase;

    public UserConsentApi(UserConsentUseCase userConsentUseCase) {
        this.userConsentUseCase = userConsentUseCase;
    }

    @Operation(summary = "동의값 저장", description = "사용자의 이용 동의값을 저장합니다.")
    @PostMapping
    public ResponseEntity<List<UserConsentResponse>> saveConsents(@RequestBody UserConsentCreateRequest request) {
        return ResponseMapper.toResponseEntity(userConsentUseCase.saveConsents(request));
    }

    @Operation(summary = "전체 동의 여부 조회", description = "userId 또는 deviceId로 모든 필수 항목에 동의했는지 조회합니다.")
    @GetMapping("/all-agreed")
    public ResponseEntity<Boolean> isAllAgreed(
        @Parameter(description = "유저 아이디") @RequestParam(required = false) Long userId,
        @Parameter(description = "디바이스 아이디") @RequestParam(required = false) String deviceId
    ) {
        return ResponseMapper.toResponseEntity(userConsentUseCase.isAllAgreed(userId, deviceId));
    }
}
