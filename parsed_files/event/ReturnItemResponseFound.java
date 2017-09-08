package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReturnItemResponseFound implements Event{

	private List<ReturnItemResponse> returnItemResponses;

	public ReturnItemResponseFound(List<ReturnItemResponse> returnItemResponses) {
		this.setReturnItemResponses(returnItemResponses);
	}

	public List<ReturnItemResponse> getReturnItemResponses()	{
		return returnItemResponses;
	}

	public void setReturnItemResponses(List<ReturnItemResponse> returnItemResponses)	{
		this.returnItemResponses = returnItemResponses;
	}
}
