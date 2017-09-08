package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetStdCostTypeFound implements Event{

	private List<FixedAssetStdCostType> fixedAssetStdCostTypes;

	public FixedAssetStdCostTypeFound(List<FixedAssetStdCostType> fixedAssetStdCostTypes) {
		this.setFixedAssetStdCostTypes(fixedAssetStdCostTypes);
	}

	public List<FixedAssetStdCostType> getFixedAssetStdCostTypes()	{
		return fixedAssetStdCostTypes;
	}

	public void setFixedAssetStdCostTypes(List<FixedAssetStdCostType> fixedAssetStdCostTypes)	{
		this.fixedAssetStdCostTypes = fixedAssetStdCostTypes;
	}
}
