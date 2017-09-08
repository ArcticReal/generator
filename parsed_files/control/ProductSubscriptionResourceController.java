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
import com.skytala.eCommerce.command.AddProductSubscriptionResource;
import com.skytala.eCommerce.command.DeleteProductSubscriptionResource;
import com.skytala.eCommerce.command.UpdateProductSubscriptionResource;
import com.skytala.eCommerce.entity.ProductSubscriptionResource;
import com.skytala.eCommerce.entity.ProductSubscriptionResourceMapper;
import com.skytala.eCommerce.event.ProductSubscriptionResourceAdded;
import com.skytala.eCommerce.event.ProductSubscriptionResourceDeleted;
import com.skytala.eCommerce.event.ProductSubscriptionResourceFound;
import com.skytala.eCommerce.event.ProductSubscriptionResourceUpdated;
import com.skytala.eCommerce.query.FindProductSubscriptionResourcesBy;

@RestController
@RequestMapping("/api/productSubscriptionResource")
public class ProductSubscriptionResourceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductSubscriptionResource>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductSubscriptionResourceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductSubscriptionResource
	 * @return a List with the ProductSubscriptionResources
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductSubscriptionResource> findProductSubscriptionResourcesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductSubscriptionResourcesBy query = new FindProductSubscriptionResourcesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductSubscriptionResourceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductSubscriptionResourceFound.class,
				event -> sendProductSubscriptionResourcesFoundMessage(((ProductSubscriptionResourceFound) event).getProductSubscriptionResources(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductSubscriptionResourcesFoundMessage(List<ProductSubscriptionResource> productSubscriptionResources, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productSubscriptionResources);
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
	public boolean createProductSubscriptionResource(HttpServletRequest request) {

		ProductSubscriptionResource productSubscriptionResourceToBeAdded = new ProductSubscriptionResource();
		try {
			productSubscriptionResourceToBeAdded = ProductSubscriptionResourceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductSubscriptionResource(productSubscriptionResourceToBeAdded);

	}

	/**
	 * creates a new ProductSubscriptionResource entry in the ofbiz database
	 * 
	 * @param productSubscriptionResourceToBeAdded
	 *            the ProductSubscriptionResource thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductSubscriptionResource(ProductSubscriptionResource productSubscriptionResourceToBeAdded) {

		AddProductSubscriptionResource com = new AddProductSubscriptionResource(productSubscriptionResourceToBeAdded);
		int usedTicketId;

		synchronized (ProductSubscriptionResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductSubscriptionResourceAdded.class,
				event -> sendProductSubscriptionResourceChangedMessage(((ProductSubscriptionResourceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductSubscriptionResource(HttpServletRequest request) {

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

		ProductSubscriptionResource productSubscriptionResourceToBeUpdated = new ProductSubscriptionResource();

		try {
			productSubscriptionResourceToBeUpdated = ProductSubscriptionResourceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductSubscriptionResource(productSubscriptionResourceToBeUpdated);

	}

	/**
	 * Updates the ProductSubscriptionResource with the specific Id
	 * 
	 * @param productSubscriptionResourceToBeUpdated the ProductSubscriptionResource thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductSubscriptionResource(ProductSubscriptionResource productSubscriptionResourceToBeUpdated) {

		UpdateProductSubscriptionResource com = new UpdateProductSubscriptionResource(productSubscriptionResourceToBeUpdated);

		int usedTicketId;

		synchronized (ProductSubscriptionResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductSubscriptionResourceUpdated.class,
				event -> sendProductSubscriptionResourceChangedMessage(((ProductSubscriptionResourceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductSubscriptionResource from the database
	 * 
	 * @param productSubscriptionResourceId:
	 *            the id of the ProductSubscriptionResource thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductSubscriptionResourceById(@RequestParam(value = "productSubscriptionResourceId") String productSubscriptionResourceId) {

		DeleteProductSubscriptionResource com = new DeleteProductSubscriptionResource(productSubscriptionResourceId);

		int usedTicketId;

		synchronized (ProductSubscriptionResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductSubscriptionResourceDeleted.class,
				event -> sendProductSubscriptionResourceChangedMessage(((ProductSubscriptionResourceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductSubscriptionResourceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productSubscriptionResource/\" plus one of the following: "
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
