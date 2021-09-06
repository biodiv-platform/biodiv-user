/**
 * 
 */
package com.strandls.user.controller;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.strandls.user.auth.GoogleAuthorizationResource;
import com.strandls.user.auth.GoogleResource;

/**
 * @author Abhishek Rudra
 *
 */
public class UserControllerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(UserController.class).in(Scopes.SINGLETON);
		bind(AuthenticationController.class).in(Scopes.SINGLETON);
		bind(GoogleResource.class).in(Scopes.SINGLETON);
		bind(GoogleAuthorizationResource.class).in(Scopes.SINGLETON);
		bind(RolesController.class).in(Scopes.SINGLETON);
		bind(DownloadLogController.class).in(Scopes.SINGLETON);
	}
}
