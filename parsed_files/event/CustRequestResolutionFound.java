package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestResolutionFound implements Event{

	private List<CustRequestResolution> custRequestResolutions;

	public CustRequestResolutionFound(List<CustRequestResolution> custRequestResolutions) {
		this.setCustRequestResolutions(custRequestResolutions);
	}

	public List<CustRequestResolution> getCustRequestResolutions()	{
		return custRequestResolutions;
	}

	public void setCustRequestResolutions(List<CustRequestResolution> custRequestResolutions)	{
		this.custRequestResolutions = custRequestResolutions;
	}
}
