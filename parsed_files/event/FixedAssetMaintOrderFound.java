package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetMaintOrderFound implements Event{

	private List<FixedAssetMaintOrder> fixedAssetMaintOrders;

	public FixedAssetMaintOrderFound(List<FixedAssetMaintOrder> fixedAssetMaintOrders) {
		this.setFixedAssetMaintOrders(fixedAssetMaintOrders);
	}

	public List<FixedAssetMaintOrder> getFixedAssetMaintOrders()	{
		return fixedAssetMaintOrders;
	}

	public void setFixedAssetMaintOrders(List<FixedAssetMaintOrder> fixedAssetMaintOrders)	{
		this.fixedAssetMaintOrders = fixedAssetMaintOrders;
	}
}
