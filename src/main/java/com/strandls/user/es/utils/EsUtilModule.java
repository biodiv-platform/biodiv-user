package com.strandls.user.es.utils;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class EsUtilModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(EsUtility.class).in(Scopes.SINGLETON);

	}

}