package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentAssocFound implements Event{

	private List<ContentAssoc> contentAssocs;

	public ContentAssocFound(List<ContentAssoc> contentAssocs) {
		this.setContentAssocs(contentAssocs);
	}

	public List<ContentAssoc> getContentAssocs()	{
		return contentAssocs;
	}

	public void setContentAssocs(List<ContentAssoc> contentAssocs)	{
		this.contentAssocs = contentAssocs;
	}
}
