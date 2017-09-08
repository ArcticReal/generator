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
import com.skytala.eCommerce.command.AddProductFacility;
import com.skytala.eCommerce.command.DeleteProductFacility;
import com.skytala.eCommerce.command.UpdateProductFacility;
import com.skytala.eCommerce.entity.ProductFacility;
import com.skytala.eCommerce.entity.ProductFacilityMapper;
import com.skytala.eCommerce.event.ProductFacilityAdded;
import com.skytala.eCommerce.event.ProductFacilityDeleted;
import com.skytala.eCommerce.event.ProductFacilityFound;
import com.skytala.eCommerce.event.ProductFacilityUpdated;
import com.skytala.eCommerce.query.FindProductFacilitysBy;

@RestController
@RequestMapping("/api/productFacility")
public class ProductFacilityController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFacility>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFacilityController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFacility
	 * @return a List with the ProductFacilitys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFacility> findProductFacilitysBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFacilitysBy query = new FindProductFacilitysBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFacilityController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFacilityFound.class,
				event -> sendProductFacilitysFoundMessage(((ProductFacilityFound) event).getProductFacilitys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFacilitysFoundMessage(List<ProductFacility> productFacilitys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFacilitys);
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
	public boolean createProductFacility(HttpServletRequest request) {

		ProductFacility productFacilityToBeAdded = new ProductFacility();
		try {
			productFacilityToBeAdded = ProductFacilityMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFacility(productFacilityToBeAdded);

	}

	/**
	 * creates a new ProductFacility entry in the ofbiz database
	 * 
	 * @param productFacilityToBeAdded
	 *            the ProductFacility thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFacility(ProductFacility productFacilityToBeAdded) {

		AddProductFacility com = new AddProductFacility(productFacilityToBeAdded);
		int usedTicketId;

		synchronized (ProductFacilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFacilityAdded.class,
				event -> sendProductFacilityChangedMessage(((ProductFacilityAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFacility(HttpServletRequest request) {

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

		ProductFacility productFacilityToBeUpdated = new ProductFacility();

		try {
			productFacilityToBeUpdated = ProductFacilityMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFacility(productFacilityToBeUpdated);

	}

	/**
	 * Updates the ProductFacility with the specific Id
	 * 
	 * @param productFacilityToBeUpdated the ProductFacility thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFacility(ProductFacility productFacilityToBeUpdated) {

		UpdateProductFacility com = new UpdateProductFacility(productFacilityToBeUpdated);

		int usedTicketId;

		synchronized (ProductFacilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFacilityUpdated.class,
				event -> sendProductFacilityChangedMessage(((ProductFacilityUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFacility from the database
	 * 
	 * @param productFacilityId:
	 *            the id of the ProductFacility thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFacilityById(@RequestParam(value = "productFacilityId") String productFacilityId) {

		DeleteProductFacility com = new DeleteProductFacility(productFacilityId);

		int usedTicketId;

		synchronized (ProductFacilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFacilityDeleted.class,
				event -> sendProductFacilityChangedMessage(((ProductFacilityDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFacilityChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFacility/\" plus one of the following: "
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
