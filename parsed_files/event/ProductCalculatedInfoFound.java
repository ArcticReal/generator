package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductCalculatedInfoFound implements Event{

	private List<ProductCalculatedInfo> productCalculatedInfos;

	public ProductCalculatedInfoFound(List<ProductCalculatedInfo> productCalculatedInfos) {
		this.setProductCalculatedInfos(productCalculatedInfos);
	}

	public List<ProductCalculatedInfo> getProductCalculatedInfos()	{
		return productCalculatedInfos;
	}

	public void setProductCalculatedInfos(List<ProductCalculatedInfo> productCalculatedInfos)	{
		this.productCalculatedInfos = productCalculatedInfos;
	}
}
