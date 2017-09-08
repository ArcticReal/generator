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
import com.skytala.eCommerce.command.AddProductFeatureDataResource;
import com.skytala.eCommerce.command.DeleteProductFeatureDataResource;
import com.skytala.eCommerce.command.UpdateProductFeatureDataResource;
import com.skytala.eCommerce.entity.ProductFeatureDataResource;
import com.skytala.eCommerce.entity.ProductFeatureDataResourceMapper;
import com.skytala.eCommerce.event.ProductFeatureDataResourceAdded;
import com.skytala.eCommerce.event.ProductFeatureDataResourceDeleted;
import com.skytala.eCommerce.event.ProductFeatureDataResourceFound;
import com.skytala.eCommerce.event.ProductFeatureDataResourceUpdated;
import com.skytala.eCommerce.query.FindProductFeatureDataResourcesBy;

@RestController
@RequestMapping("/api/productFeatureDataResource")
public class ProductFeatureDataResourceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFeatureDataResource>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFeatureDataResourceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFeatureDataResource
	 * @return a List with the ProductFeatureDataResources
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFeatureDataResource> findProductFeatureDataResourcesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFeatureDataResourcesBy query = new FindProductFeatureDataResourcesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFeatureDataResourceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureDataResourceFound.class,
				event -> sendProductFeatureDataResourcesFoundMessage(((ProductFeatureDataResourceFound) event).getProductFeatureDataResources(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFeatureDataResourcesFoundMessage(List<ProductFeatureDataResource> productFeatureDataResources, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFeatureDataResources);
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
	public boolean createProductFeatureDataResource(HttpServletRequest request) {

		ProductFeatureDataResource productFeatureDataResourceToBeAdded = new ProductFeatureDataResource();
		try {
			productFeatureDataResourceToBeAdded = ProductFeatureDataResourceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFeatureDataResource(productFeatureDataResourceToBeAdded);

	}

	/**
	 * creates a new ProductFeatureDataResource entry in the ofbiz database
	 * 
	 * @param productFeatureDataResourceToBeAdded
	 *            the ProductFeatureDataResource thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFeatureDataResource(ProductFeatureDataResource productFeatureDataResourceToBeAdded) {

		AddProductFeatureDataResource com = new AddProductFeatureDataResource(productFeatureDataResourceToBeAdded);
		int usedTicketId;

		synchronized (ProductFeatureDataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureDataResourceAdded.class,
				event -> sendProductFeatureDataResourceChangedMessage(((ProductFeatureDataResourceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFeatureDataResource(HttpServletRequest request) {

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

		ProductFeatureDataResource productFeatureDataResourceToBeUpdated = new ProductFeatureDataResource();

		try {
			productFeatureDataResourceToBeUpdated = ProductFeatureDataResourceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFeatureDataResource(productFeatureDataResourceToBeUpdated);

	}

	/**
	 * Updates the ProductFeatureDataResource with the specific Id
	 * 
	 * @param productFeatureDataResourceToBeUpdated the ProductFeatureDataResource thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFeatureDataResource(ProductFeatureDataResource productFeatureDataResourceToBeUpdated) {

		UpdateProductFeatureDataResource com = new UpdateProductFeatureDataResource(productFeatureDataResourceToBeUpdated);

		int usedTicketId;

		synchronized (ProductFeatureDataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureDataResourceUpdated.class,
				event -> sendProductFeatureDataResourceChangedMessage(((ProductFeatureDataResourceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFeatureDataResource from the database
	 * 
	 * @param productFeatureDataResourceId:
	 *            the id of the ProductFeatureDataResource thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFeatureDataResourceById(@RequestParam(value = "productFeatureDataResourceId") String productFeatureDataResourceId) {

		DeleteProductFeatureDataResource com = new DeleteProductFeatureDataResource(productFeatureDataResourceId);

		int usedTicketId;

		synchronized (ProductFeatureDataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureDataResourceDeleted.class,
				event -> sendProductFeatureDataResourceChangedMessage(((ProductFeatureDataResourceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFeatureDataResourceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFeatureDataResource/\" plus one of the following: "
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
