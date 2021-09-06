package com.strandls.user.service.impl;

import javax.inject.Inject;

import com.strandls.user.dao.LanguageDao;
import com.strandls.user.pojo.Language;
import com.strandls.user.service.LanguageService;

public class LanguageServiceImpl implements LanguageService {

	@Inject
	private LanguageDao languageDao;

	@Override
	public Language getLanguageByTwoLetterCode(String language) {
		Language lang = languageDao.findLangByProperty("twoLetterCode", language);
		if (lang == null) {
			return getCurrentLanguage();
		}
		return lang;
	}
	
	private Language getCurrentLanguage() {
		return languageDao.findLangByProperty("name", Language.DEFAULT_LANGUAGE);
	}

}
