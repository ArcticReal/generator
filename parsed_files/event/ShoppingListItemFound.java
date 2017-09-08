package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShoppingListItemFound implements Event{

	private List<ShoppingListItem> shoppingListItems;

	public ShoppingListItemFound(List<ShoppingListItem> shoppingListItems) {
		this.setShoppingListItems(shoppingListItems);
	}

	public List<ShoppingListItem> getShoppingListItems()	{
		return shoppingListItems;
	}

	public void setShoppingListItems(List<ShoppingListItem> shoppingListItems)	{
		this.shoppingListItems = shoppingListItems;
	}
}
