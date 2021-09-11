/**
 * 
 */
package com.strandls.user.service.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.strandls.user.service.AuthenticationService;
import com.strandls.user.service.DowloadLogService;
import com.strandls.user.service.LanguageService;
import com.strandls.user.service.MailService;
import com.strandls.user.service.RoleService;
import com.strandls.user.service.SMSService;
import com.strandls.user.service.UserListService;
import com.strandls.user.service.UserService;
import com.strandls.user.service.UserVerificationService;

/**
 * @author Abhishek Rudra
 *
 */
public class UserServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(UserService.class).to(UserServiceImpl.class).in(Scopes.SINGLETON);
		bind(UserListService.class).to(UserListServiceImpl.class).in(Scopes.SINGLETON);
		bind(AuthenticationService.class).to(AuthenticationServiceImpl.class).in(Scopes.SINGLETON);
		bind(RoleService.class).to(RoleServiceImpl.class).in(Scopes.SINGLETON);
		bind(MailService.class).to(MailServiceImpl.class).in(Scopes.SINGLETON);
		bind(UserVerificationService.class).to(UserVerificationServiceImpl.class).in(Scopes.SINGLETON);
		bind(SMSService.class).to(SMSServiceImpl.class).in(Scopes.SINGLETON);
		bind(LanguageService.class).to(LanguageServiceImpl.class).in(Scopes.SINGLETON);
		bind(DowloadLogService.class).to(DownloadLogServiceImpl.class).in(Scopes.SINGLETON);
	}
}
