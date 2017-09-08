package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class VarianceReasonFound implements Event{

	private List<VarianceReason> varianceReasons;

	public VarianceReasonFound(List<VarianceReason> varianceReasons) {
		this.setVarianceReasons(varianceReasons);
	}

	public List<VarianceReason> getVarianceReasons()	{
		return varianceReasons;
	}

	public void setVarianceReasons(List<VarianceReason> varianceReasons)	{
		this.varianceReasons = varianceReasons;
	}
}
