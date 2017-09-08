package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WebPreferenceTypeFound implements Event{

	private List<WebPreferenceType> webPreferenceTypes;

	public WebPreferenceTypeFound(List<WebPreferenceType> webPreferenceTypes) {
		this.setWebPreferenceTypes(webPreferenceTypes);
	}

	public List<WebPreferenceType> getWebPreferenceTypes()	{
		return webPreferenceTypes;
	}

	public void setWebPreferenceTypes(List<WebPreferenceType> webPreferenceTypes)	{
		this.webPreferenceTypes = webPreferenceTypes;
	}
}
