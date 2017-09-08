package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReturnStatusFound implements Event{

	private List<ReturnStatus> returnStatuss;

	public ReturnStatusFound(List<ReturnStatus> returnStatuss) {
		this.setReturnStatuss(returnStatuss);
	}

	public List<ReturnStatus> getReturnStatuss()	{
		return returnStatuss;
	}

	public void setReturnStatuss(List<ReturnStatus> returnStatuss)	{
		this.returnStatuss = returnStatuss;
	}
}
