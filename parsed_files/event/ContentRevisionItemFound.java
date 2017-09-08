package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentRevisionItemFound implements Event{

	private List<ContentRevisionItem> contentRevisionItems;

	public ContentRevisionItemFound(List<ContentRevisionItem> contentRevisionItems) {
		this.setContentRevisionItems(contentRevisionItems);
	}

	public List<ContentRevisionItem> getContentRevisionItems()	{
		return contentRevisionItems;
	}

	public void setContentRevisionItems(List<ContentRevisionItem> contentRevisionItems)	{
		this.contentRevisionItems = contentRevisionItems;
	}
}
