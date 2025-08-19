package com.strandls.user.service;

import com.strandls.user.pojo.UserVerification;

public interface UserVerificationService {

	UserVerification getUserVerificationDetails(Long userId, String action);

	UserVerification getDetailsByVerificationId(String verificationId, String action);

	UserVerification saveOtp(Long id, String otp, String verificationType, String verificationId, String action);

	boolean saveOrUpdateOtp(UserVerification verification);

	void deleteOtp(Long id);

	UserVerification updateOtp(UserVerification verification);

}
