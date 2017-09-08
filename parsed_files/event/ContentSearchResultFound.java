package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentSearchResultFound implements Event{

	private List<ContentSearchResult> contentSearchResults;

	public ContentSearchResultFound(List<ContentSearchResult> contentSearchResults) {
		this.setContentSearchResults(contentSearchResults);
	}

	public List<ContentSearchResult> getContentSearchResults()	{
		return contentSearchResults;
	}

	public void setContentSearchResults(List<ContentSearchResult> contentSearchResults)	{
		this.contentSearchResults = contentSearchResults;
	}
}
