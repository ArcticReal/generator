package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FixedAssetProductFound implements Event{

	private List<FixedAssetProduct> fixedAssetProducts;

	public FixedAssetProductFound(List<FixedAssetProduct> fixedAssetProducts) {
		this.setFixedAssetProducts(fixedAssetProducts);
	}

	public List<FixedAssetProduct> getFixedAssetProducts()	{
		return fixedAssetProducts;
	}

	public void setFixedAssetProducts(List<FixedAssetProduct> fixedAssetProducts)	{
		this.fixedAssetProducts = fixedAssetProducts;
	}
}
