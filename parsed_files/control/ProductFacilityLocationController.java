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
import com.skytala.eCommerce.command.AddProductFacilityLocation;
import com.skytala.eCommerce.command.DeleteProductFacilityLocation;
import com.skytala.eCommerce.command.UpdateProductFacilityLocation;
import com.skytala.eCommerce.entity.ProductFacilityLocation;
import com.skytala.eCommerce.entity.ProductFacilityLocationMapper;
import com.skytala.eCommerce.event.ProductFacilityLocationAdded;
import com.skytala.eCommerce.event.ProductFacilityLocationDeleted;
import com.skytala.eCommerce.event.ProductFacilityLocationFound;
import com.skytala.eCommerce.event.ProductFacilityLocationUpdated;
import com.skytala.eCommerce.query.FindProductFacilityLocationsBy;

@RestController
@RequestMapping("/api/productFacilityLocation")
public class ProductFacilityLocationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFacilityLocation>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFacilityLocationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFacilityLocation
	 * @return a List with the ProductFacilityLocations
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFacilityLocation> findProductFacilityLocationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFacilityLocationsBy query = new FindProductFacilityLocationsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFacilityLocationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFacilityLocationFound.class,
				event -> sendProductFacilityLocationsFoundMessage(((ProductFacilityLocationFound) event).getProductFacilityLocations(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFacilityLocationsFoundMessage(List<ProductFacilityLocation> productFacilityLocations, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFacilityLocations);
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
	public boolean createProductFacilityLocation(HttpServletRequest request) {

		ProductFacilityLocation productFacilityLocationToBeAdded = new ProductFacilityLocation();
		try {
			productFacilityLocationToBeAdded = ProductFacilityLocationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFacilityLocation(productFacilityLocationToBeAdded);

	}

	/**
	 * creates a new ProductFacilityLocation entry in the ofbiz database
	 * 
	 * @param productFacilityLocationToBeAdded
	 *            the ProductFacilityLocation thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFacilityLocation(ProductFacilityLocation productFacilityLocationToBeAdded) {

		AddProductFacilityLocation com = new AddProductFacilityLocation(productFacilityLocationToBeAdded);
		int usedTicketId;

		synchronized (ProductFacilityLocationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFacilityLocationAdded.class,
				event -> sendProductFacilityLocationChangedMessage(((ProductFacilityLocationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFacilityLocation(HttpServletRequest request) {

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

		ProductFacilityLocation productFacilityLocationToBeUpdated = new ProductFacilityLocation();

		try {
			productFacilityLocationToBeUpdated = ProductFacilityLocationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFacilityLocation(productFacilityLocationToBeUpdated);

	}

	/**
	 * Updates the ProductFacilityLocation with the specific Id
	 * 
	 * @param productFacilityLocationToBeUpdated the ProductFacilityLocation thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFacilityLocation(ProductFacilityLocation productFacilityLocationToBeUpdated) {

		UpdateProductFacilityLocation com = new UpdateProductFacilityLocation(productFacilityLocationToBeUpdated);

		int usedTicketId;

		synchronized (ProductFacilityLocationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFacilityLocationUpdated.class,
				event -> sendProductFacilityLocationChangedMessage(((ProductFacilityLocationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFacilityLocation from the database
	 * 
	 * @param productFacilityLocationId:
	 *            the id of the ProductFacilityLocation thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFacilityLocationById(@RequestParam(value = "productFacilityLocationId") String productFacilityLocationId) {

		DeleteProductFacilityLocation com = new DeleteProductFacilityLocation(productFacilityLocationId);

		int usedTicketId;

		synchronized (ProductFacilityLocationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFacilityLocationDeleted.class,
				event -> sendProductFacilityLocationChangedMessage(((ProductFacilityLocationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFacilityLocationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFacilityLocation/\" plus one of the following: "
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
