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
import com.skytala.eCommerce.command.AddProductPriceActionType;
import com.skytala.eCommerce.command.DeleteProductPriceActionType;
import com.skytala.eCommerce.command.UpdateProductPriceActionType;
import com.skytala.eCommerce.entity.ProductPriceActionType;
import com.skytala.eCommerce.entity.ProductPriceActionTypeMapper;
import com.skytala.eCommerce.event.ProductPriceActionTypeAdded;
import com.skytala.eCommerce.event.ProductPriceActionTypeDeleted;
import com.skytala.eCommerce.event.ProductPriceActionTypeFound;
import com.skytala.eCommerce.event.ProductPriceActionTypeUpdated;
import com.skytala.eCommerce.query.FindProductPriceActionTypesBy;

@RestController
@RequestMapping("/api/productPriceActionType")
public class ProductPriceActionTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPriceActionType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPriceActionTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPriceActionType
	 * @return a List with the ProductPriceActionTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPriceActionType> findProductPriceActionTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPriceActionTypesBy query = new FindProductPriceActionTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPriceActionTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceActionTypeFound.class,
				event -> sendProductPriceActionTypesFoundMessage(((ProductPriceActionTypeFound) event).getProductPriceActionTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPriceActionTypesFoundMessage(List<ProductPriceActionType> productPriceActionTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPriceActionTypes);
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
	public boolean createProductPriceActionType(HttpServletRequest request) {

		ProductPriceActionType productPriceActionTypeToBeAdded = new ProductPriceActionType();
		try {
			productPriceActionTypeToBeAdded = ProductPriceActionTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPriceActionType(productPriceActionTypeToBeAdded);

	}

	/**
	 * creates a new ProductPriceActionType entry in the ofbiz database
	 * 
	 * @param productPriceActionTypeToBeAdded
	 *            the ProductPriceActionType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPriceActionType(ProductPriceActionType productPriceActionTypeToBeAdded) {

		AddProductPriceActionType com = new AddProductPriceActionType(productPriceActionTypeToBeAdded);
		int usedTicketId;

		synchronized (ProductPriceActionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceActionTypeAdded.class,
				event -> sendProductPriceActionTypeChangedMessage(((ProductPriceActionTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPriceActionType(HttpServletRequest request) {

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

		ProductPriceActionType productPriceActionTypeToBeUpdated = new ProductPriceActionType();

		try {
			productPriceActionTypeToBeUpdated = ProductPriceActionTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPriceActionType(productPriceActionTypeToBeUpdated);

	}

	/**
	 * Updates the ProductPriceActionType with the specific Id
	 * 
	 * @param productPriceActionTypeToBeUpdated the ProductPriceActionType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPriceActionType(ProductPriceActionType productPriceActionTypeToBeUpdated) {

		UpdateProductPriceActionType com = new UpdateProductPriceActionType(productPriceActionTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProductPriceActionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceActionTypeUpdated.class,
				event -> sendProductPriceActionTypeChangedMessage(((ProductPriceActionTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPriceActionType from the database
	 * 
	 * @param productPriceActionTypeId:
	 *            the id of the ProductPriceActionType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPriceActionTypeById(@RequestParam(value = "productPriceActionTypeId") String productPriceActionTypeId) {

		DeleteProductPriceActionType com = new DeleteProductPriceActionType(productPriceActionTypeId);

		int usedTicketId;

		synchronized (ProductPriceActionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceActionTypeDeleted.class,
				event -> sendProductPriceActionTypeChangedMessage(((ProductPriceActionTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPriceActionTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPriceActionType/\" plus one of the following: "
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
