package com.strandls.user.service;

import com.strandls.user.pojo.User;

import jakarta.servlet.http.HttpServletRequest;

public interface MailService {

	void sendActivationMail(HttpServletRequest request, User user, String otp);

	void sendWelcomeMail(HttpServletRequest request, User user);

	void sendForgotPasswordMail(HttpServletRequest request, User user, String otp);

}
