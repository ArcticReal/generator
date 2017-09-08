package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetMeterFound implements Event{

	private List<FixedAssetMeter> fixedAssetMeters;

	public FixedAssetMeterFound(List<FixedAssetMeter> fixedAssetMeters) {
		this.setFixedAssetMeters(fixedAssetMeters);
	}

	public List<FixedAssetMeter> getFixedAssetMeters()	{
		return fixedAssetMeters;
	}

	public void setFixedAssetMeters(List<FixedAssetMeter> fixedAssetMeters)	{
		this.fixedAssetMeters = fixedAssetMeters;
	}
}
