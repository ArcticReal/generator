package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReturnHeaderFound implements Event{

	private List<ReturnHeader> returnHeaders;

	public ReturnHeaderFound(List<ReturnHeader> returnHeaders) {
		this.setReturnHeaders(returnHeaders);
	}

	public List<ReturnHeader> getReturnHeaders()	{
		return returnHeaders;
	}

	public void setReturnHeaders(List<ReturnHeader> returnHeaders)	{
		this.returnHeaders = returnHeaders;
	}
}
