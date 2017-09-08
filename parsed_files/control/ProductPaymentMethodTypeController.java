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
import com.skytala.eCommerce.command.AddProductPaymentMethodType;
import com.skytala.eCommerce.command.DeleteProductPaymentMethodType;
import com.skytala.eCommerce.command.UpdateProductPaymentMethodType;
import com.skytala.eCommerce.entity.ProductPaymentMethodType;
import com.skytala.eCommerce.entity.ProductPaymentMethodTypeMapper;
import com.skytala.eCommerce.event.ProductPaymentMethodTypeAdded;
import com.skytala.eCommerce.event.ProductPaymentMethodTypeDeleted;
import com.skytala.eCommerce.event.ProductPaymentMethodTypeFound;
import com.skytala.eCommerce.event.ProductPaymentMethodTypeUpdated;
import com.skytala.eCommerce.query.FindProductPaymentMethodTypesBy;

@RestController
@RequestMapping("/api/productPaymentMethodType")
public class ProductPaymentMethodTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPaymentMethodType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPaymentMethodTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPaymentMethodType
	 * @return a List with the ProductPaymentMethodTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPaymentMethodType> findProductPaymentMethodTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPaymentMethodTypesBy query = new FindProductPaymentMethodTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPaymentMethodTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPaymentMethodTypeFound.class,
				event -> sendProductPaymentMethodTypesFoundMessage(((ProductPaymentMethodTypeFound) event).getProductPaymentMethodTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPaymentMethodTypesFoundMessage(List<ProductPaymentMethodType> productPaymentMethodTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPaymentMethodTypes);
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
	public boolean createProductPaymentMethodType(HttpServletRequest request) {

		ProductPaymentMethodType productPaymentMethodTypeToBeAdded = new ProductPaymentMethodType();
		try {
			productPaymentMethodTypeToBeAdded = ProductPaymentMethodTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPaymentMethodType(productPaymentMethodTypeToBeAdded);

	}

	/**
	 * creates a new ProductPaymentMethodType entry in the ofbiz database
	 * 
	 * @param productPaymentMethodTypeToBeAdded
	 *            the ProductPaymentMethodType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPaymentMethodType(ProductPaymentMethodType productPaymentMethodTypeToBeAdded) {

		AddProductPaymentMethodType com = new AddProductPaymentMethodType(productPaymentMethodTypeToBeAdded);
		int usedTicketId;

		synchronized (ProductPaymentMethodTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPaymentMethodTypeAdded.class,
				event -> sendProductPaymentMethodTypeChangedMessage(((ProductPaymentMethodTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPaymentMethodType(HttpServletRequest request) {

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

		ProductPaymentMethodType productPaymentMethodTypeToBeUpdated = new ProductPaymentMethodType();

		try {
			productPaymentMethodTypeToBeUpdated = ProductPaymentMethodTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPaymentMethodType(productPaymentMethodTypeToBeUpdated);

	}

	/**
	 * Updates the ProductPaymentMethodType with the specific Id
	 * 
	 * @param productPaymentMethodTypeToBeUpdated the ProductPaymentMethodType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPaymentMethodType(ProductPaymentMethodType productPaymentMethodTypeToBeUpdated) {

		UpdateProductPaymentMethodType com = new UpdateProductPaymentMethodType(productPaymentMethodTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProductPaymentMethodTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPaymentMethodTypeUpdated.class,
				event -> sendProductPaymentMethodTypeChangedMessage(((ProductPaymentMethodTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPaymentMethodType from the database
	 * 
	 * @param productPaymentMethodTypeId:
	 *            the id of the ProductPaymentMethodType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPaymentMethodTypeById(@RequestParam(value = "productPaymentMethodTypeId") String productPaymentMethodTypeId) {

		DeleteProductPaymentMethodType com = new DeleteProductPaymentMethodType(productPaymentMethodTypeId);

		int usedTicketId;

		synchronized (ProductPaymentMethodTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPaymentMethodTypeDeleted.class,
				event -> sendProductPaymentMethodTypeChangedMessage(((ProductPaymentMethodTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPaymentMethodTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPaymentMethodType/\" plus one of the following: "
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
