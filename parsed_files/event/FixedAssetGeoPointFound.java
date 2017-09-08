package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetGeoPointFound implements Event{

	private List<FixedAssetGeoPoint> fixedAssetGeoPoints;

	public FixedAssetGeoPointFound(List<FixedAssetGeoPoint> fixedAssetGeoPoints) {
		this.setFixedAssetGeoPoints(fixedAssetGeoPoints);
	}

	public List<FixedAssetGeoPoint> getFixedAssetGeoPoints()	{
		return fixedAssetGeoPoints;
	}

	public void setFixedAssetGeoPoints(List<FixedAssetGeoPoint> fixedAssetGeoPoints)	{
		this.fixedAssetGeoPoints = fixedAssetGeoPoints;
	}
}
