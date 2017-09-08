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
import com.skytala.eCommerce.command.AddProductAverageCostType;
import com.skytala.eCommerce.command.DeleteProductAverageCostType;
import com.skytala.eCommerce.command.UpdateProductAverageCostType;
import com.skytala.eCommerce.entity.ProductAverageCostType;
import com.skytala.eCommerce.entity.ProductAverageCostTypeMapper;
import com.skytala.eCommerce.event.ProductAverageCostTypeAdded;
import com.skytala.eCommerce.event.ProductAverageCostTypeDeleted;
import com.skytala.eCommerce.event.ProductAverageCostTypeFound;
import com.skytala.eCommerce.event.ProductAverageCostTypeUpdated;
import com.skytala.eCommerce.query.FindProductAverageCostTypesBy;

@RestController
@RequestMapping("/api/productAverageCostType")
public class ProductAverageCostTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductAverageCostType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductAverageCostTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductAverageCostType
	 * @return a List with the ProductAverageCostTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductAverageCostType> findProductAverageCostTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductAverageCostTypesBy query = new FindProductAverageCostTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductAverageCostTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAverageCostTypeFound.class,
				event -> sendProductAverageCostTypesFoundMessage(((ProductAverageCostTypeFound) event).getProductAverageCostTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductAverageCostTypesFoundMessage(List<ProductAverageCostType> productAverageCostTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productAverageCostTypes);
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
	public boolean createProductAverageCostType(HttpServletRequest request) {

		ProductAverageCostType productAverageCostTypeToBeAdded = new ProductAverageCostType();
		try {
			productAverageCostTypeToBeAdded = ProductAverageCostTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductAverageCostType(productAverageCostTypeToBeAdded);

	}

	/**
	 * creates a new ProductAverageCostType entry in the ofbiz database
	 * 
	 * @param productAverageCostTypeToBeAdded
	 *            the ProductAverageCostType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductAverageCostType(ProductAverageCostType productAverageCostTypeToBeAdded) {

		AddProductAverageCostType com = new AddProductAverageCostType(productAverageCostTypeToBeAdded);
		int usedTicketId;

		synchronized (ProductAverageCostTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAverageCostTypeAdded.class,
				event -> sendProductAverageCostTypeChangedMessage(((ProductAverageCostTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductAverageCostType(HttpServletRequest request) {

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

		ProductAverageCostType productAverageCostTypeToBeUpdated = new ProductAverageCostType();

		try {
			productAverageCostTypeToBeUpdated = ProductAverageCostTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductAverageCostType(productAverageCostTypeToBeUpdated);

	}

	/**
	 * Updates the ProductAverageCostType with the specific Id
	 * 
	 * @param productAverageCostTypeToBeUpdated the ProductAverageCostType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductAverageCostType(ProductAverageCostType productAverageCostTypeToBeUpdated) {

		UpdateProductAverageCostType com = new UpdateProductAverageCostType(productAverageCostTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProductAverageCostTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAverageCostTypeUpdated.class,
				event -> sendProductAverageCostTypeChangedMessage(((ProductAverageCostTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductAverageCostType from the database
	 * 
	 * @param productAverageCostTypeId:
	 *            the id of the ProductAverageCostType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductAverageCostTypeById(@RequestParam(value = "productAverageCostTypeId") String productAverageCostTypeId) {

		DeleteProductAverageCostType com = new DeleteProductAverageCostType(productAverageCostTypeId);

		int usedTicketId;

		synchronized (ProductAverageCostTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAverageCostTypeDeleted.class,
				event -> sendProductAverageCostTypeChangedMessage(((ProductAverageCostTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductAverageCostTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productAverageCostType/\" plus one of the following: "
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
