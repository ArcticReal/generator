package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShoppingListTypeFound implements Event{

	private List<ShoppingListType> shoppingListTypes;

	public ShoppingListTypeFound(List<ShoppingListType> shoppingListTypes) {
		this.setShoppingListTypes(shoppingListTypes);
	}

	public List<ShoppingListType> getShoppingListTypes()	{
		return shoppingListTypes;
	}

	public void setShoppingListTypes(List<ShoppingListType> shoppingListTypes)	{
		this.shoppingListTypes = shoppingListTypes;
	}
}
