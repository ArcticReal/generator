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
import com.skytala.eCommerce.command.AddProductPriceType;
import com.skytala.eCommerce.command.DeleteProductPriceType;
import com.skytala.eCommerce.command.UpdateProductPriceType;
import com.skytala.eCommerce.entity.ProductPriceType;
import com.skytala.eCommerce.entity.ProductPriceTypeMapper;
import com.skytala.eCommerce.event.ProductPriceTypeAdded;
import com.skytala.eCommerce.event.ProductPriceTypeDeleted;
import com.skytala.eCommerce.event.ProductPriceTypeFound;
import com.skytala.eCommerce.event.ProductPriceTypeUpdated;
import com.skytala.eCommerce.query.FindProductPriceTypesBy;

@RestController
@RequestMapping("/api/productPriceType")
public class ProductPriceTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPriceType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPriceTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPriceType
	 * @return a List with the ProductPriceTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPriceType> findProductPriceTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPriceTypesBy query = new FindProductPriceTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPriceTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceTypeFound.class,
				event -> sendProductPriceTypesFoundMessage(((ProductPriceTypeFound) event).getProductPriceTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPriceTypesFoundMessage(List<ProductPriceType> productPriceTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPriceTypes);
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
	public boolean createProductPriceType(HttpServletRequest request) {

		ProductPriceType productPriceTypeToBeAdded = new ProductPriceType();
		try {
			productPriceTypeToBeAdded = ProductPriceTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPriceType(productPriceTypeToBeAdded);

	}

	/**
	 * creates a new ProductPriceType entry in the ofbiz database
	 * 
	 * @param productPriceTypeToBeAdded
	 *            the ProductPriceType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPriceType(ProductPriceType productPriceTypeToBeAdded) {

		AddProductPriceType com = new AddProductPriceType(productPriceTypeToBeAdded);
		int usedTicketId;

		synchronized (ProductPriceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceTypeAdded.class,
				event -> sendProductPriceTypeChangedMessage(((ProductPriceTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPriceType(HttpServletRequest request) {

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

		ProductPriceType productPriceTypeToBeUpdated = new ProductPriceType();

		try {
			productPriceTypeToBeUpdated = ProductPriceTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPriceType(productPriceTypeToBeUpdated);

	}

	/**
	 * Updates the ProductPriceType with the specific Id
	 * 
	 * @param productPriceTypeToBeUpdated the ProductPriceType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPriceType(ProductPriceType productPriceTypeToBeUpdated) {

		UpdateProductPriceType com = new UpdateProductPriceType(productPriceTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProductPriceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceTypeUpdated.class,
				event -> sendProductPriceTypeChangedMessage(((ProductPriceTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPriceType from the database
	 * 
	 * @param productPriceTypeId:
	 *            the id of the ProductPriceType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPriceTypeById(@RequestParam(value = "productPriceTypeId") String productPriceTypeId) {

		DeleteProductPriceType com = new DeleteProductPriceType(productPriceTypeId);

		int usedTicketId;

		synchronized (ProductPriceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceTypeDeleted.class,
				event -> sendProductPriceTypeChangedMessage(((ProductPriceTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPriceTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPriceType/\" plus one of the following: "
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
