package com.strandls.user.service.impl;

import java.util.Date;
import java.util.List;

import com.strandls.user.dao.UserVerificationDao;
import com.strandls.user.pojo.UserVerification;
import com.strandls.user.service.UserVerificationService;

import jakarta.inject.Inject;

public class UserVerificationServiceImpl implements UserVerificationService {

	@Inject
	private UserVerificationDao userVerificationDao;

	@Override
	public UserVerification getUserVerificationDetails(Long userId, String action) {
		UserVerification userVerification = userVerificationDao.findByUserId(userId, action);
		if (userVerification == null) {
			List<UserVerification> multipleUV = userVerificationDao.findMultipleCase(userId, null, action);
			if (multipleUV != null && !multipleUV.isEmpty()) {
				for (UserVerification uv : multipleUV) {
					userVerificationDao.delete(uv);
				}
			}
		}
		return userVerification;
	}

	@Override
	public void deleteOtp(Long id) {
		userVerificationDao.delete(new UserVerification(id));
	}

	@Override
	public UserVerification updateOtp(UserVerification verification) {
		return userVerificationDao.update(verification);
	}

	@Override
	public UserVerification saveOtp(Long id, String otp, String verificationType, String verificationId,
			String action) {
		UserVerification verification = new UserVerification();
		verification.setOtp(otp);
		verification.setTimeout(24 * 60 * 60 * 1000L);
		verification.setUserId(id);
		verification.setDate(new Date());
		verification.setVerificationType(verificationType);
		verification.setVerificationId(verificationId);
		verification.setNoOfAttempts(0);
		verification.setAction(action);
		return userVerificationDao.save(verification);
	}

	@Override
	public UserVerification getDetailsByVerificationId(String verificationId, String action) {
		UserVerification userVerification = userVerificationDao.findByVerificationId(verificationId, action);
		if (userVerification == null) {
			List<UserVerification> multipleUV = userVerificationDao.findMultipleCase(null, verificationId, action);
			if (multipleUV != null && !multipleUV.isEmpty()) {
				for (UserVerification uv : multipleUV) {
					userVerificationDao.delete(uv);
				}
			}
		}
		return userVerification;
	}

	@Override
	public boolean saveOrUpdateOtp(UserVerification verification) {
		return userVerificationDao.saveOrUpdateVerification(verification);
	}

}
