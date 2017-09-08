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
import com.skytala.eCommerce.command.AddProductAssocType;
import com.skytala.eCommerce.command.DeleteProductAssocType;
import com.skytala.eCommerce.command.UpdateProductAssocType;
import com.skytala.eCommerce.entity.ProductAssocType;
import com.skytala.eCommerce.entity.ProductAssocTypeMapper;
import com.skytala.eCommerce.event.ProductAssocTypeAdded;
import com.skytala.eCommerce.event.ProductAssocTypeDeleted;
import com.skytala.eCommerce.event.ProductAssocTypeFound;
import com.skytala.eCommerce.event.ProductAssocTypeUpdated;
import com.skytala.eCommerce.query.FindProductAssocTypesBy;

@RestController
@RequestMapping("/api/productAssocType")
public class ProductAssocTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductAssocType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductAssocTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductAssocType
	 * @return a List with the ProductAssocTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductAssocType> findProductAssocTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductAssocTypesBy query = new FindProductAssocTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductAssocTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAssocTypeFound.class,
				event -> sendProductAssocTypesFoundMessage(((ProductAssocTypeFound) event).getProductAssocTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductAssocTypesFoundMessage(List<ProductAssocType> productAssocTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productAssocTypes);
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
	public boolean createProductAssocType(HttpServletRequest request) {

		ProductAssocType productAssocTypeToBeAdded = new ProductAssocType();
		try {
			productAssocTypeToBeAdded = ProductAssocTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductAssocType(productAssocTypeToBeAdded);

	}

	/**
	 * creates a new ProductAssocType entry in the ofbiz database
	 * 
	 * @param productAssocTypeToBeAdded
	 *            the ProductAssocType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductAssocType(ProductAssocType productAssocTypeToBeAdded) {

		AddProductAssocType com = new AddProductAssocType(productAssocTypeToBeAdded);
		int usedTicketId;

		synchronized (ProductAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAssocTypeAdded.class,
				event -> sendProductAssocTypeChangedMessage(((ProductAssocTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductAssocType(HttpServletRequest request) {

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

		ProductAssocType productAssocTypeToBeUpdated = new ProductAssocType();

		try {
			productAssocTypeToBeUpdated = ProductAssocTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductAssocType(productAssocTypeToBeUpdated);

	}

	/**
	 * Updates the ProductAssocType with the specific Id
	 * 
	 * @param productAssocTypeToBeUpdated the ProductAssocType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductAssocType(ProductAssocType productAssocTypeToBeUpdated) {

		UpdateProductAssocType com = new UpdateProductAssocType(productAssocTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProductAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAssocTypeUpdated.class,
				event -> sendProductAssocTypeChangedMessage(((ProductAssocTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductAssocType from the database
	 * 
	 * @param productAssocTypeId:
	 *            the id of the ProductAssocType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductAssocTypeById(@RequestParam(value = "productAssocTypeId") String productAssocTypeId) {

		DeleteProductAssocType com = new DeleteProductAssocType(productAssocTypeId);

		int usedTicketId;

		synchronized (ProductAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAssocTypeDeleted.class,
				event -> sendProductAssocTypeChangedMessage(((ProductAssocTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductAssocTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productAssocType/\" plus one of the following: "
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
