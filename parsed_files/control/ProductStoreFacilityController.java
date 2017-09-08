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
import com.skytala.eCommerce.command.AddProductStoreFacility;
import com.skytala.eCommerce.command.DeleteProductStoreFacility;
import com.skytala.eCommerce.command.UpdateProductStoreFacility;
import com.skytala.eCommerce.entity.ProductStoreFacility;
import com.skytala.eCommerce.entity.ProductStoreFacilityMapper;
import com.skytala.eCommerce.event.ProductStoreFacilityAdded;
import com.skytala.eCommerce.event.ProductStoreFacilityDeleted;
import com.skytala.eCommerce.event.ProductStoreFacilityFound;
import com.skytala.eCommerce.event.ProductStoreFacilityUpdated;
import com.skytala.eCommerce.query.FindProductStoreFacilitysBy;

@RestController
@RequestMapping("/api/productStoreFacility")
public class ProductStoreFacilityController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreFacility>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreFacilityController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreFacility
	 * @return a List with the ProductStoreFacilitys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreFacility> findProductStoreFacilitysBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreFacilitysBy query = new FindProductStoreFacilitysBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreFacilityController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreFacilityFound.class,
				event -> sendProductStoreFacilitysFoundMessage(((ProductStoreFacilityFound) event).getProductStoreFacilitys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreFacilitysFoundMessage(List<ProductStoreFacility> productStoreFacilitys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreFacilitys);
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
	public boolean createProductStoreFacility(HttpServletRequest request) {

		ProductStoreFacility productStoreFacilityToBeAdded = new ProductStoreFacility();
		try {
			productStoreFacilityToBeAdded = ProductStoreFacilityMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreFacility(productStoreFacilityToBeAdded);

	}

	/**
	 * creates a new ProductStoreFacility entry in the ofbiz database
	 * 
	 * @param productStoreFacilityToBeAdded
	 *            the ProductStoreFacility thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreFacility(ProductStoreFacility productStoreFacilityToBeAdded) {

		AddProductStoreFacility com = new AddProductStoreFacility(productStoreFacilityToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreFacilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreFacilityAdded.class,
				event -> sendProductStoreFacilityChangedMessage(((ProductStoreFacilityAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreFacility(HttpServletRequest request) {

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

		ProductStoreFacility productStoreFacilityToBeUpdated = new ProductStoreFacility();

		try {
			productStoreFacilityToBeUpdated = ProductStoreFacilityMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreFacility(productStoreFacilityToBeUpdated);

	}

	/**
	 * Updates the ProductStoreFacility with the specific Id
	 * 
	 * @param productStoreFacilityToBeUpdated the ProductStoreFacility thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreFacility(ProductStoreFacility productStoreFacilityToBeUpdated) {

		UpdateProductStoreFacility com = new UpdateProductStoreFacility(productStoreFacilityToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreFacilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreFacilityUpdated.class,
				event -> sendProductStoreFacilityChangedMessage(((ProductStoreFacilityUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreFacility from the database
	 * 
	 * @param productStoreFacilityId:
	 *            the id of the ProductStoreFacility thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreFacilityById(@RequestParam(value = "productStoreFacilityId") String productStoreFacilityId) {

		DeleteProductStoreFacility com = new DeleteProductStoreFacility(productStoreFacilityId);

		int usedTicketId;

		synchronized (ProductStoreFacilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreFacilityDeleted.class,
				event -> sendProductStoreFacilityChangedMessage(((ProductStoreFacilityDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreFacilityChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreFacility/\" plus one of the following: "
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
