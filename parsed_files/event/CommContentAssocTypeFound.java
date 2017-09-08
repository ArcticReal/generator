package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CommContentAssocTypeFound implements Event{

	private List<CommContentAssocType> commContentAssocTypes;

	public CommContentAssocTypeFound(List<CommContentAssocType> commContentAssocTypes) {
		this.setCommContentAssocTypes(commContentAssocTypes);
	}

	public List<CommContentAssocType> getCommContentAssocTypes()	{
		return commContentAssocTypes;
	}

	public void setCommContentAssocTypes(List<CommContentAssocType> commContentAssocTypes)	{
		this.commContentAssocTypes = commContentAssocTypes;
	}
}
