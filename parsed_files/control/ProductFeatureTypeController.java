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
import com.skytala.eCommerce.command.AddProductFeatureType;
import com.skytala.eCommerce.command.DeleteProductFeatureType;
import com.skytala.eCommerce.command.UpdateProductFeatureType;
import com.skytala.eCommerce.entity.ProductFeatureType;
import com.skytala.eCommerce.entity.ProductFeatureTypeMapper;
import com.skytala.eCommerce.event.ProductFeatureTypeAdded;
import com.skytala.eCommerce.event.ProductFeatureTypeDeleted;
import com.skytala.eCommerce.event.ProductFeatureTypeFound;
import com.skytala.eCommerce.event.ProductFeatureTypeUpdated;
import com.skytala.eCommerce.query.FindProductFeatureTypesBy;

@RestController
@RequestMapping("/api/productFeatureType")
public class ProductFeatureTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFeatureType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFeatureTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFeatureType
	 * @return a List with the ProductFeatureTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFeatureType> findProductFeatureTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFeatureTypesBy query = new FindProductFeatureTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFeatureTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureTypeFound.class,
				event -> sendProductFeatureTypesFoundMessage(((ProductFeatureTypeFound) event).getProductFeatureTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFeatureTypesFoundMessage(List<ProductFeatureType> productFeatureTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFeatureTypes);
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
	public boolean createProductFeatureType(HttpServletRequest request) {

		ProductFeatureType productFeatureTypeToBeAdded = new ProductFeatureType();
		try {
			productFeatureTypeToBeAdded = ProductFeatureTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFeatureType(productFeatureTypeToBeAdded);

	}

	/**
	 * creates a new ProductFeatureType entry in the ofbiz database
	 * 
	 * @param productFeatureTypeToBeAdded
	 *            the ProductFeatureType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFeatureType(ProductFeatureType productFeatureTypeToBeAdded) {

		AddProductFeatureType com = new AddProductFeatureType(productFeatureTypeToBeAdded);
		int usedTicketId;

		synchronized (ProductFeatureTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureTypeAdded.class,
				event -> sendProductFeatureTypeChangedMessage(((ProductFeatureTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFeatureType(HttpServletRequest request) {

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

		ProductFeatureType productFeatureTypeToBeUpdated = new ProductFeatureType();

		try {
			productFeatureTypeToBeUpdated = ProductFeatureTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFeatureType(productFeatureTypeToBeUpdated);

	}

	/**
	 * Updates the ProductFeatureType with the specific Id
	 * 
	 * @param productFeatureTypeToBeUpdated the ProductFeatureType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFeatureType(ProductFeatureType productFeatureTypeToBeUpdated) {

		UpdateProductFeatureType com = new UpdateProductFeatureType(productFeatureTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProductFeatureTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureTypeUpdated.class,
				event -> sendProductFeatureTypeChangedMessage(((ProductFeatureTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFeatureType from the database
	 * 
	 * @param productFeatureTypeId:
	 *            the id of the ProductFeatureType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFeatureTypeById(@RequestParam(value = "productFeatureTypeId") String productFeatureTypeId) {

		DeleteProductFeatureType com = new DeleteProductFeatureType(productFeatureTypeId);

		int usedTicketId;

		synchronized (ProductFeatureTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureTypeDeleted.class,
				event -> sendProductFeatureTypeChangedMessage(((ProductFeatureTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFeatureTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFeatureType/\" plus one of the following: "
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
