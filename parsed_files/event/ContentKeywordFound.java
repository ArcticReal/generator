package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentKeywordFound implements Event{

	private List<ContentKeyword> contentKeywords;

	public ContentKeywordFound(List<ContentKeyword> contentKeywords) {
		this.setContentKeywords(contentKeywords);
	}

	public List<ContentKeyword> getContentKeywords()	{
		return contentKeywords;
	}

	public void setContentKeywords(List<ContentKeyword> contentKeywords)	{
		this.contentKeywords = contentKeywords;
	}
}
