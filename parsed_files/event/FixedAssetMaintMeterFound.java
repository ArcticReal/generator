package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetMaintMeterFound implements Event{

	private List<FixedAssetMaintMeter> fixedAssetMaintMeters;

	public FixedAssetMaintMeterFound(List<FixedAssetMaintMeter> fixedAssetMaintMeters) {
		this.setFixedAssetMaintMeters(fixedAssetMaintMeters);
	}

	public List<FixedAssetMaintMeter> getFixedAssetMaintMeters()	{
		return fixedAssetMaintMeters;
	}

	public void setFixedAssetMaintMeters(List<FixedAssetMaintMeter> fixedAssetMaintMeters)	{
		this.fixedAssetMaintMeters = fixedAssetMaintMeters;
	}
}
