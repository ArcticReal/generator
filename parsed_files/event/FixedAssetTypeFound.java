package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetTypeFound implements Event{

	private List<FixedAssetType> fixedAssetTypes;

	public FixedAssetTypeFound(List<FixedAssetType> fixedAssetTypes) {
		this.setFixedAssetTypes(fixedAssetTypes);
	}

	public List<FixedAssetType> getFixedAssetTypes()	{
		return fixedAssetTypes;
	}

	public void setFixedAssetTypes(List<FixedAssetType> fixedAssetTypes)	{
		this.fixedAssetTypes = fixedAssetTypes;
	}
}
