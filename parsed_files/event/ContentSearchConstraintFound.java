package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentSearchConstraintFound implements Event{

	private List<ContentSearchConstraint> contentSearchConstraints;

	public ContentSearchConstraintFound(List<ContentSearchConstraint> contentSearchConstraints) {
		this.setContentSearchConstraints(contentSearchConstraints);
	}

	public List<ContentSearchConstraint> getContentSearchConstraints()	{
		return contentSearchConstraints;
	}

	public void setContentSearchConstraints(List<ContentSearchConstraint> contentSearchConstraints)	{
		this.contentSearchConstraints = contentSearchConstraints;
	}
}
