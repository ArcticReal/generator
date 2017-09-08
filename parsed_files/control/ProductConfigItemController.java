package com.skytala.eCommerce.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Splitter;
import com.skytala.eCommerce.command.AddProductConfigItem;
import com.skytala.eCommerce.command.DeleteProductConfigItem;
import com.skytala.eCommerce.command.UpdateProductConfigItem;
import com.skytala.eCommerce.entity.ProductConfigItem;
import com.skytala.eCommerce.entity.ProductConfigItemMapper;
import com.skytala.eCommerce.event.ProductConfigItemAdded;
import com.skytala.eCommerce.event.ProductConfigItemDeleted;
import com.skytala.eCommerce.event.ProductConfigItemFound;
import com.skytala.eCommerce.event.ProductConfigItemUpdated;
import com.skytala.eCommerce.query.FindProductConfigItemsBy;

@RestController
@RequestMapping("/api/productConfigItem")
public class ProductConfigItemController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductConfigItem>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductConfigItemController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductConfigItem
	 * @return a List with the ProductConfigItems
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductConfigItem> findProductConfigItemsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductConfigItemsBy query = new FindProductConfigItemsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductConfigItemController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigItemFound.class,
				event -> sendProductConfigItemsFoundMessage(((ProductConfigItemFound) event).getProductConfigItems(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductConfigItemsFoundMessage(List<ProductConfigItem> productConfigItems, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productConfigItems);
	}

	/**
	 * 
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return true on success; false on fail
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/add", consumes = "application/x-www-form-urlencoded")
	public boolean createProductConfigItem(HttpServletRequest request) {

		ProductConfigItem productConfigItemToBeAdded = new ProductConfigItem();
		try {
			productConfigItemToBeAdded = ProductConfigItemMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductConfigItem(productConfigItemToBeAdded);

	}

	/**
	 * creates a new ProductConfigItem entry in the ofbiz database
	 * 
	 * @param productConfigItemToBeAdded
	 *            the ProductConfigItem thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductConfigItem(ProductConfigItem productConfigItemToBeAdded) {

		AddProductConfigItem com = new AddProductConfigItem(productConfigItemToBeAdded);
		int usedTicketId;

		synchronized (ProductConfigItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigItemAdded.class,
				event -> sendProductConfigItemChangedMessage(((ProductConfigItemAdded) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);

	}

	/**
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request HttpServletRequest object
	 * @return true on success, false on fail
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/update", consumes = "application/x-www-form-urlencoded")
	public boolean updateProductConfigItem(HttpServletRequest request) {

		BufferedReader br;
		String data = null;
		Map<String, String> dataMap = null;

		try {
			br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if (br != null) {
				data = java.net.URLDecoder.decode(br.readLine(), "UTF-8");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		dataMap = Splitter.on('&').trimResults().withKeyValueSeparator(Splitter.on('=').limit(2).trimResults())
				.split(data);

		ProductConfigItem productConfigItemToBeUpdated = new ProductConfigItem();

		try {
			productConfigItemToBeUpdated = ProductConfigItemMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductConfigItem(productConfigItemToBeUpdated);

	}

	/**
	 * Updates the ProductConfigItem with the specific Id
	 * 
	 * @param productConfigItemToBeUpdated the ProductConfigItem thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductConfigItem(ProductConfigItem productConfigItemToBeUpdated) {

		UpdateProductConfigItem com = new UpdateProductConfigItem(productConfigItemToBeUpdated);

		int usedTicketId;

		synchronized (ProductConfigItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigItemUpdated.class,
				event -> sendProductConfigItemChangedMessage(((ProductConfigItemUpdated) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	/**
	 * removes a ProductConfigItem from the database
	 * 
	 * @param productConfigItemId:
	 *            the id of the ProductConfigItem thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductConfigItemById(@RequestParam(value = "productConfigItemId") String productConfigItemId) {

		DeleteProductConfigItem com = new DeleteProductConfigItem(productConfigItemId);

		int usedTicketId;

		synchronized (ProductConfigItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigItemDeleted.class,
				event -> sendProductConfigItemChangedMessage(((ProductConfigItemDeleted) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	public void sendProductConfigItemChangedMessage(boolean success, int usedTicketId) {
		commandReturnVal.put(usedTicketId, success);
	}

	@RequestMapping(value = (" * "))
	public String returnErrorPage(HttpServletRequest request) {

		String usedUri = request.getRequestURI();
		String[] splittedString = usedUri.split("/");

		String usedRequest = splittedString[splittedString.length - 1];

		if (validRequests.containsKey(usedRequest)) {
			return "Error: request method " + request.getMethod() + " not allowed for \"" + usedUri + "\"!\n"
					+ "Please use " + validRequests.get(usedRequest) + "!";

		}

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productConfigItem/\" plus one of the following: "
				+ "";

		Set<String> keySet = validRequests.keySet();
		Iterator<String> it = keySet.iterator();

		while (it.hasNext()) {
			returnVal += "\"" + it.next() + "\"";
			if (it.hasNext())
				returnVal += ", ";
		}

		returnVal += "!";

		return returnVal;

	}
}
