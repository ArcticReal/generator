package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TerminationTypeFound implements Event{

	private List<TerminationType> terminationTypes;

	public TerminationTypeFound(List<TerminationType> terminationTypes) {
		this.setTerminationTypes(terminationTypes);
	}

	public List<TerminationType> getTerminationTypes()	{
		return terminationTypes;
	}

	public void setTerminationTypes(List<TerminationType> terminationTypes)	{
		this.terminationTypes = terminationTypes;
	}
}
