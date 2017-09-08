package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WebUserPreferenceFound implements Event{

	private List<WebUserPreference> webUserPreferences;

	public WebUserPreferenceFound(List<WebUserPreference> webUserPreferences) {
		this.setWebUserPreferences(webUserPreferences);
	}

	public List<WebUserPreference> getWebUserPreferences()	{
		return webUserPreferences;
	}

	public void setWebUserPreferences(List<WebUserPreference> webUserPreferences)	{
		this.webUserPreferences = webUserPreferences;
	}
}
