package com.strandls.user.service.impl;

import com.strandls.user.service.SMSService;
import com.strandls.user.util.SMSThread;
import com.strandls.user.util.SNSUtil;

import jakarta.inject.Inject;

public class SMSServiceImpl implements SMSService {

	@Inject
	private SNSUtil snsUtil;

	@Override
	public void sendSMS(String phoneNumber, String otp) {
		SMSThread sms = new SMSThread(snsUtil, phoneNumber, otp);
		Thread thread = new Thread(sms);
		thread.start();
	}

}
