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
import com.skytala.eCommerce.command.AddProductPricePurpose;
import com.skytala.eCommerce.command.DeleteProductPricePurpose;
import com.skytala.eCommerce.command.UpdateProductPricePurpose;
import com.skytala.eCommerce.entity.ProductPricePurpose;
import com.skytala.eCommerce.entity.ProductPricePurposeMapper;
import com.skytala.eCommerce.event.ProductPricePurposeAdded;
import com.skytala.eCommerce.event.ProductPricePurposeDeleted;
import com.skytala.eCommerce.event.ProductPricePurposeFound;
import com.skytala.eCommerce.event.ProductPricePurposeUpdated;
import com.skytala.eCommerce.query.FindProductPricePurposesBy;

@RestController
@RequestMapping("/api/productPricePurpose")
public class ProductPricePurposeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPricePurpose>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPricePurposeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPricePurpose
	 * @return a List with the ProductPricePurposes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPricePurpose> findProductPricePurposesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPricePurposesBy query = new FindProductPricePurposesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPricePurposeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPricePurposeFound.class,
				event -> sendProductPricePurposesFoundMessage(((ProductPricePurposeFound) event).getProductPricePurposes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPricePurposesFoundMessage(List<ProductPricePurpose> productPricePurposes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPricePurposes);
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
	public boolean createProductPricePurpose(HttpServletRequest request) {

		ProductPricePurpose productPricePurposeToBeAdded = new ProductPricePurpose();
		try {
			productPricePurposeToBeAdded = ProductPricePurposeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPricePurpose(productPricePurposeToBeAdded);

	}

	/**
	 * creates a new ProductPricePurpose entry in the ofbiz database
	 * 
	 * @param productPricePurposeToBeAdded
	 *            the ProductPricePurpose thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPricePurpose(ProductPricePurpose productPricePurposeToBeAdded) {

		AddProductPricePurpose com = new AddProductPricePurpose(productPricePurposeToBeAdded);
		int usedTicketId;

		synchronized (ProductPricePurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPricePurposeAdded.class,
				event -> sendProductPricePurposeChangedMessage(((ProductPricePurposeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPricePurpose(HttpServletRequest request) {

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

		ProductPricePurpose productPricePurposeToBeUpdated = new ProductPricePurpose();

		try {
			productPricePurposeToBeUpdated = ProductPricePurposeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPricePurpose(productPricePurposeToBeUpdated);

	}

	/**
	 * Updates the ProductPricePurpose with the specific Id
	 * 
	 * @param productPricePurposeToBeUpdated the ProductPricePurpose thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPricePurpose(ProductPricePurpose productPricePurposeToBeUpdated) {

		UpdateProductPricePurpose com = new UpdateProductPricePurpose(productPricePurposeToBeUpdated);

		int usedTicketId;

		synchronized (ProductPricePurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPricePurposeUpdated.class,
				event -> sendProductPricePurposeChangedMessage(((ProductPricePurposeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPricePurpose from the database
	 * 
	 * @param productPricePurposeId:
	 *            the id of the ProductPricePurpose thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPricePurposeById(@RequestParam(value = "productPricePurposeId") String productPricePurposeId) {

		DeleteProductPricePurpose com = new DeleteProductPricePurpose(productPricePurposeId);

		int usedTicketId;

		synchronized (ProductPricePurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPricePurposeDeleted.class,
				event -> sendProductPricePurposeChangedMessage(((ProductPricePurposeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPricePurposeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPricePurpose/\" plus one of the following: "
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
