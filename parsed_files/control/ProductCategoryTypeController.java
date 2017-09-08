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
import com.skytala.eCommerce.command.AddProductCategoryType;
import com.skytala.eCommerce.command.DeleteProductCategoryType;
import com.skytala.eCommerce.command.UpdateProductCategoryType;
import com.skytala.eCommerce.entity.ProductCategoryType;
import com.skytala.eCommerce.entity.ProductCategoryTypeMapper;
import com.skytala.eCommerce.event.ProductCategoryTypeAdded;
import com.skytala.eCommerce.event.ProductCategoryTypeDeleted;
import com.skytala.eCommerce.event.ProductCategoryTypeFound;
import com.skytala.eCommerce.event.ProductCategoryTypeUpdated;
import com.skytala.eCommerce.query.FindProductCategoryTypesBy;

@RestController
@RequestMapping("/api/productCategoryType")
public class ProductCategoryTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductCategoryType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductCategoryTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductCategoryType
	 * @return a List with the ProductCategoryTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductCategoryType> findProductCategoryTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductCategoryTypesBy query = new FindProductCategoryTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductCategoryTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryTypeFound.class,
				event -> sendProductCategoryTypesFoundMessage(((ProductCategoryTypeFound) event).getProductCategoryTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductCategoryTypesFoundMessage(List<ProductCategoryType> productCategoryTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productCategoryTypes);
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
	public boolean createProductCategoryType(HttpServletRequest request) {

		ProductCategoryType productCategoryTypeToBeAdded = new ProductCategoryType();
		try {
			productCategoryTypeToBeAdded = ProductCategoryTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductCategoryType(productCategoryTypeToBeAdded);

	}

	/**
	 * creates a new ProductCategoryType entry in the ofbiz database
	 * 
	 * @param productCategoryTypeToBeAdded
	 *            the ProductCategoryType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductCategoryType(ProductCategoryType productCategoryTypeToBeAdded) {

		AddProductCategoryType com = new AddProductCategoryType(productCategoryTypeToBeAdded);
		int usedTicketId;

		synchronized (ProductCategoryTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryTypeAdded.class,
				event -> sendProductCategoryTypeChangedMessage(((ProductCategoryTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductCategoryType(HttpServletRequest request) {

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

		ProductCategoryType productCategoryTypeToBeUpdated = new ProductCategoryType();

		try {
			productCategoryTypeToBeUpdated = ProductCategoryTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductCategoryType(productCategoryTypeToBeUpdated);

	}

	/**
	 * Updates the ProductCategoryType with the specific Id
	 * 
	 * @param productCategoryTypeToBeUpdated the ProductCategoryType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductCategoryType(ProductCategoryType productCategoryTypeToBeUpdated) {

		UpdateProductCategoryType com = new UpdateProductCategoryType(productCategoryTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProductCategoryTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryTypeUpdated.class,
				event -> sendProductCategoryTypeChangedMessage(((ProductCategoryTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductCategoryType from the database
	 * 
	 * @param productCategoryTypeId:
	 *            the id of the ProductCategoryType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductCategoryTypeById(@RequestParam(value = "productCategoryTypeId") String productCategoryTypeId) {

		DeleteProductCategoryType com = new DeleteProductCategoryType(productCategoryTypeId);

		int usedTicketId;

		synchronized (ProductCategoryTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryTypeDeleted.class,
				event -> sendProductCategoryTypeChangedMessage(((ProductCategoryTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductCategoryTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productCategoryType/\" plus one of the following: "
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
