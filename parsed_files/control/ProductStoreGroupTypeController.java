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
import com.skytala.eCommerce.command.AddProductStoreGroupType;
import com.skytala.eCommerce.command.DeleteProductStoreGroupType;
import com.skytala.eCommerce.command.UpdateProductStoreGroupType;
import com.skytala.eCommerce.entity.ProductStoreGroupType;
import com.skytala.eCommerce.entity.ProductStoreGroupTypeMapper;
import com.skytala.eCommerce.event.ProductStoreGroupTypeAdded;
import com.skytala.eCommerce.event.ProductStoreGroupTypeDeleted;
import com.skytala.eCommerce.event.ProductStoreGroupTypeFound;
import com.skytala.eCommerce.event.ProductStoreGroupTypeUpdated;
import com.skytala.eCommerce.query.FindProductStoreGroupTypesBy;

@RestController
@RequestMapping("/api/productStoreGroupType")
public class ProductStoreGroupTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreGroupType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreGroupTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreGroupType
	 * @return a List with the ProductStoreGroupTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreGroupType> findProductStoreGroupTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreGroupTypesBy query = new FindProductStoreGroupTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreGroupTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupTypeFound.class,
				event -> sendProductStoreGroupTypesFoundMessage(((ProductStoreGroupTypeFound) event).getProductStoreGroupTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreGroupTypesFoundMessage(List<ProductStoreGroupType> productStoreGroupTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreGroupTypes);
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
	public boolean createProductStoreGroupType(HttpServletRequest request) {

		ProductStoreGroupType productStoreGroupTypeToBeAdded = new ProductStoreGroupType();
		try {
			productStoreGroupTypeToBeAdded = ProductStoreGroupTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreGroupType(productStoreGroupTypeToBeAdded);

	}

	/**
	 * creates a new ProductStoreGroupType entry in the ofbiz database
	 * 
	 * @param productStoreGroupTypeToBeAdded
	 *            the ProductStoreGroupType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreGroupType(ProductStoreGroupType productStoreGroupTypeToBeAdded) {

		AddProductStoreGroupType com = new AddProductStoreGroupType(productStoreGroupTypeToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupTypeAdded.class,
				event -> sendProductStoreGroupTypeChangedMessage(((ProductStoreGroupTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreGroupType(HttpServletRequest request) {

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

		ProductStoreGroupType productStoreGroupTypeToBeUpdated = new ProductStoreGroupType();

		try {
			productStoreGroupTypeToBeUpdated = ProductStoreGroupTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreGroupType(productStoreGroupTypeToBeUpdated);

	}

	/**
	 * Updates the ProductStoreGroupType with the specific Id
	 * 
	 * @param productStoreGroupTypeToBeUpdated the ProductStoreGroupType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreGroupType(ProductStoreGroupType productStoreGroupTypeToBeUpdated) {

		UpdateProductStoreGroupType com = new UpdateProductStoreGroupType(productStoreGroupTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupTypeUpdated.class,
				event -> sendProductStoreGroupTypeChangedMessage(((ProductStoreGroupTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreGroupType from the database
	 * 
	 * @param productStoreGroupTypeId:
	 *            the id of the ProductStoreGroupType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreGroupTypeById(@RequestParam(value = "productStoreGroupTypeId") String productStoreGroupTypeId) {

		DeleteProductStoreGroupType com = new DeleteProductStoreGroupType(productStoreGroupTypeId);

		int usedTicketId;

		synchronized (ProductStoreGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupTypeDeleted.class,
				event -> sendProductStoreGroupTypeChangedMessage(((ProductStoreGroupTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreGroupTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreGroupType/\" plus one of the following: "
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
