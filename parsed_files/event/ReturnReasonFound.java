package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReturnReasonFound implements Event{

	private List<ReturnReason> returnReasons;

	public ReturnReasonFound(List<ReturnReason> returnReasons) {
		this.setReturnReasons(returnReasons);
	}

	public List<ReturnReason> getReturnReasons()	{
		return returnReasons;
	}

	public void setReturnReasons(List<ReturnReason> returnReasons)	{
		this.returnReasons = returnReasons;
	}
}
