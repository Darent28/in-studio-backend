package com.is.in_studio.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.is.in_studio.auth.JwtUtil;
import com.is.in_studio.domain.dto.MembershipResponseDto;
import com.is.in_studio.service.MembershipService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/memberships")
public class UserMembershipController {

    private final MembershipService membershipService;
    private final JwtUtil jwtUtil;

    public UserMembershipController(MembershipService membershipService, JwtUtil jwtUtil) {
        this.membershipService = membershipService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/my")
    public List<MembershipResponseDto> getMy(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        Long userId = jwtUtil.extractUserId(header.substring(7));
        return membershipService.getByUser(userId);
    }
}
