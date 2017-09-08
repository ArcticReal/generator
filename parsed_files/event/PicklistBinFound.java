package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PicklistBinFound implements Event{

	private List<PicklistBin> picklistBins;

	public PicklistBinFound(List<PicklistBin> picklistBins) {
		this.setPicklistBins(picklistBins);
	}

	public List<PicklistBin> getPicklistBins()	{
		return picklistBins;
	}

	public void setPicklistBins(List<PicklistBin> picklistBins)	{
		this.picklistBins = picklistBins;
	}
}
