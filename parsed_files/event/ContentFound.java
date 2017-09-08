package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentFound implements Event{

	private List<Content> contents;

	public ContentFound(List<Content> contents) {
		this.setContents(contents);
	}

	public List<Content> getContents()	{
		return contents;
	}

	public void setContents(List<Content> contents)	{
		this.contents = contents;
	}
}
