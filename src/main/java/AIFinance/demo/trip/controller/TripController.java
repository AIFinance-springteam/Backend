package AIFinance.demo.trip.controller;

import AIFinance.demo.auth.exception.AuthErrorCode;
import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.global.apiPayload.code.GeneralSuccessCode;
import AIFinance.demo.global.apiPayload.exception.GeneralException;
import AIFinance.demo.trip.dto.*;
import AIFinance.demo.trip.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trips")
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(
            @Valid @RequestBody CreateTripRequest request
    ) {
        TripResponse response = tripService.createTrip(currentUserId(), request);

        return ResponseEntity.status(GeneralSuccessCode.CREATED.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.CREATED, response));
    }

    @GetMapping
    public ApiResponse<List<TripSummaryResponse>> getMyTrips() {
        List<TripSummaryResponse> response = tripService.getMyTrips(currentUserId());

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @GetMapping("/{tripId}")
    public ApiResponse<TripDetailResponse> getTripDetail(
            @PathVariable Long tripId
    ) {
        TripDetailResponse response = tripService.getTripDetail(currentUserId(), tripId);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @PostMapping("/{tripId}/invite")
    public ApiResponse<InviteCreateResponse> createInvite(
            @PathVariable Long tripId
    ) {
        InviteCreateResponse response = tripService.createInvite(currentUserId(), tripId);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @GetMapping("/invite/{inviteCode}")
    public ApiResponse<InvitePreviewResponse> previewInvite(
            @PathVariable String inviteCode
    ) {
        InvitePreviewResponse response = tripService.previewInvite(inviteCode);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @PostMapping("/invite/{inviteCode}/join")
    public ApiResponse<JoinTripResponse> joinTrip(
            @PathVariable String inviteCode
    ) {
        JoinTripResponse response = tripService.joinTrip(currentUserId(), inviteCode);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @GetMapping("/{tripId}/members")
    public ApiResponse<List<MemberResponse>> getMembers(
            @PathVariable Long tripId
    ) {
        List<MemberResponse> response = tripService.getMembers(currentUserId(), tripId);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @DeleteMapping("/{tripId}/members/me")
    public ApiResponse<Void> leaveTrip(
            @PathVariable Long tripId
    ) {
        tripService.leaveTrip(currentUserId(), tripId);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
    }

    private Long currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Long userId)) {
            throw new GeneralException(AuthErrorCode.USER_NOT_FOUND);
        }
        return userId;
    }
}
