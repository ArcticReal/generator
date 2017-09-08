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
import com.skytala.eCommerce.command.AddProductStore;
import com.skytala.eCommerce.command.DeleteProductStore;
import com.skytala.eCommerce.command.UpdateProductStore;
import com.skytala.eCommerce.entity.ProductStore;
import com.skytala.eCommerce.entity.ProductStoreMapper;
import com.skytala.eCommerce.event.ProductStoreAdded;
import com.skytala.eCommerce.event.ProductStoreDeleted;
import com.skytala.eCommerce.event.ProductStoreFound;
import com.skytala.eCommerce.event.ProductStoreUpdated;
import com.skytala.eCommerce.query.FindProductStoresBy;

@RestController
@RequestMapping("/api/productStore")
public class ProductStoreController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStore>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStore
	 * @return a List with the ProductStores
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStore> findProductStoresBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoresBy query = new FindProductStoresBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreFound.class,
				event -> sendProductStoresFoundMessage(((ProductStoreFound) event).getProductStores(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoresFoundMessage(List<ProductStore> productStores, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStores);
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
	public boolean createProductStore(HttpServletRequest request) {

		ProductStore productStoreToBeAdded = new ProductStore();
		try {
			productStoreToBeAdded = ProductStoreMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStore(productStoreToBeAdded);

	}

	/**
	 * creates a new ProductStore entry in the ofbiz database
	 * 
	 * @param productStoreToBeAdded
	 *            the ProductStore thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStore(ProductStore productStoreToBeAdded) {

		AddProductStore com = new AddProductStore(productStoreToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreAdded.class,
				event -> sendProductStoreChangedMessage(((ProductStoreAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStore(HttpServletRequest request) {

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

		ProductStore productStoreToBeUpdated = new ProductStore();

		try {
			productStoreToBeUpdated = ProductStoreMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStore(productStoreToBeUpdated);

	}

	/**
	 * Updates the ProductStore with the specific Id
	 * 
	 * @param productStoreToBeUpdated the ProductStore thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStore(ProductStore productStoreToBeUpdated) {

		UpdateProductStore com = new UpdateProductStore(productStoreToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreUpdated.class,
				event -> sendProductStoreChangedMessage(((ProductStoreUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStore from the database
	 * 
	 * @param productStoreId:
	 *            the id of the ProductStore thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreById(@RequestParam(value = "productStoreId") String productStoreId) {

		DeleteProductStore com = new DeleteProductStore(productStoreId);

		int usedTicketId;

		synchronized (ProductStoreController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreDeleted.class,
				event -> sendProductStoreChangedMessage(((ProductStoreDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStore/\" plus one of the following: "
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
