package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetStdCostFound implements Event{

	private List<FixedAssetStdCost> fixedAssetStdCosts;

	public FixedAssetStdCostFound(List<FixedAssetStdCost> fixedAssetStdCosts) {
		this.setFixedAssetStdCosts(fixedAssetStdCosts);
	}

	public List<FixedAssetStdCost> getFixedAssetStdCosts()	{
		return fixedAssetStdCosts;
	}

	public void setFixedAssetStdCosts(List<FixedAssetStdCost> fixedAssetStdCosts)	{
		this.fixedAssetStdCosts = fixedAssetStdCosts;
	}
}
