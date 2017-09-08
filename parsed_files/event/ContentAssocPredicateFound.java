package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentAssocPredicateFound implements Event{

	private List<ContentAssocPredicate> contentAssocPredicates;

	public ContentAssocPredicateFound(List<ContentAssocPredicate> contentAssocPredicates) {
		this.setContentAssocPredicates(contentAssocPredicates);
	}

	public List<ContentAssocPredicate> getContentAssocPredicates()	{
		return contentAssocPredicates;
	}

	public void setContentAssocPredicates(List<ContentAssocPredicate> contentAssocPredicates)	{
		this.contentAssocPredicates = contentAssocPredicates;
	}
}
