package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ItemIssuanceFound implements Event{

	private List<ItemIssuance> itemIssuances;

	public ItemIssuanceFound(List<ItemIssuance> itemIssuances) {
		this.setItemIssuances(itemIssuances);
	}

	public List<ItemIssuance> getItemIssuances()	{
		return itemIssuances;
	}

	public void setItemIssuances(List<ItemIssuance> itemIssuances)	{
		this.itemIssuances = itemIssuances;
	}
}
