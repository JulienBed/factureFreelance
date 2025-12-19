package com.facture.service;

import com.facture.dto.*;
import com.facture.entity.User;
import com.facture.exception.AuthenticationException;
import com.facture.util.OtpUtil;
import com.facture.util.PasswordUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AuthService {

    @Inject
    Logger logger;

    @Inject
    PasswordUtil passwordUtil;

    @Inject
    OtpUtil otpUtil;

    @Inject
    JwtService jwtService;

    @Inject
    EmailService emailService;

    @Transactional
    public void register(RegisterRequest request) {
        // Check if user already exists
        User existingUser = User.findByEmail(request.email);
        if (existingUser != null) {
            throw new AuthenticationException("User with this email already exists");
        }

        // Create new user
        User user = new User();
        user.email = request.email;
        user.passwordHash = passwordUtil.hashPassword(request.password);
        user.firstName = request.firstName;
        user.lastName = request.lastName;

        user.persist();
        logger.infof("New user registered: %s", request.email);
    }

    @Transactional
    public void login(LoginRequest request) {
        User user = User.findByEmail(request.email);
        if (user == null) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Verify password
        if (!passwordUtil.verifyPassword(request.password, user.passwordHash)) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Generate and save OTP
        String otp = otpUtil.generateOtp();
        user.otpSecret = otp;
        user.otpExpiry = otpUtil.getOtpExpiry();
        user.persist();

        // Send OTP by email
        emailService.sendOtpEmail(user.email, otp, user.firstName);
        logger.infof("OTP sent to user: %s", request.email);
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        User user = User.findByEmail(request.email);
        if (user == null) {
            throw new AuthenticationException("User not found");
        }

        // Check if OTP is valid
        if (user.otpSecret == null || !user.otpSecret.equals(request.otp)) {
            throw new AuthenticationException("Invalid OTP");
        }

        // Check if OTP is expired
        if (otpUtil.isOtpExpired(user.otpExpiry)) {
            throw new AuthenticationException("OTP has expired");
        }

        // Clear OTP
        user.otpSecret = null;
        user.otpExpiry = null;
        user.persist();

        // Generate JWT tokens
        String accessToken = jwtService.generateAccessToken(user.id, user.email);
        String refreshToken = jwtService.generateRefreshToken(user.id, user.email);

        logger.infof("User authenticated: %s", request.email);

        return new AuthResponse(accessToken, refreshToken, UserDto.fromEntity(user));
    }

    @Transactional
    public AuthResponse loginWithGoogle(String googleId, String email, String firstName, String lastName) {
        // Find or create user
        User user = User.findByGoogleId(googleId);
        if (user == null) {
            user = User.findByEmail(email);
            if (user == null) {
                // Create new user
                user = new User();
                user.email = email;
                user.firstName = firstName;
                user.lastName = lastName;
                user.googleId = googleId;
                user.persist();
                logger.infof("New user created via Google SSO: %s", email);
            } else {
                // Link existing user to Google
                user.googleId = googleId;
                user.persist();
                logger.infof("Existing user linked to Google SSO: %s", email);
            }
        }

        // Generate JWT tokens
        String accessToken = jwtService.generateAccessToken(user.id, user.email);
        String refreshToken = jwtService.generateRefreshToken(user.id, user.email);

        return new AuthResponse(accessToken, refreshToken, UserDto.fromEntity(user));
    }
}
