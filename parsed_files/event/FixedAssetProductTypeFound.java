package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetProductTypeFound implements Event{

	private List<FixedAssetProductType> fixedAssetProductTypes;

	public FixedAssetProductTypeFound(List<FixedAssetProductType> fixedAssetProductTypes) {
		this.setFixedAssetProductTypes(fixedAssetProductTypes);
	}

	public List<FixedAssetProductType> getFixedAssetProductTypes()	{
		return fixedAssetProductTypes;
	}

	public void setFixedAssetProductTypes(List<FixedAssetProductType> fixedAssetProductTypes)	{
		this.fixedAssetProductTypes = fixedAssetProductTypes;
	}
}
