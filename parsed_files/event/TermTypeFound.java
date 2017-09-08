package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TermTypeFound implements Event{

	private List<TermType> termTypes;

	public TermTypeFound(List<TermType> termTypes) {
		this.setTermTypes(termTypes);
	}

	public List<TermType> getTermTypes()	{
		return termTypes;
	}

	public void setTermTypes(List<TermType> termTypes)	{
		this.termTypes = termTypes;
	}
}
