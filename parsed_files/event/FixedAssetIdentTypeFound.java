package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetIdentTypeFound implements Event{

	private List<FixedAssetIdentType> fixedAssetIdentTypes;

	public FixedAssetIdentTypeFound(List<FixedAssetIdentType> fixedAssetIdentTypes) {
		this.setFixedAssetIdentTypes(fixedAssetIdentTypes);
	}

	public List<FixedAssetIdentType> getFixedAssetIdentTypes()	{
		return fixedAssetIdentTypes;
	}

	public void setFixedAssetIdentTypes(List<FixedAssetIdentType> fixedAssetIdentTypes)	{
		this.fixedAssetIdentTypes = fixedAssetIdentTypes;
	}
}
