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
import com.skytala.eCommerce.command.AddProductPriceChange;
import com.skytala.eCommerce.command.DeleteProductPriceChange;
import com.skytala.eCommerce.command.UpdateProductPriceChange;
import com.skytala.eCommerce.entity.ProductPriceChange;
import com.skytala.eCommerce.entity.ProductPriceChangeMapper;
import com.skytala.eCommerce.event.ProductPriceChangeAdded;
import com.skytala.eCommerce.event.ProductPriceChangeDeleted;
import com.skytala.eCommerce.event.ProductPriceChangeFound;
import com.skytala.eCommerce.event.ProductPriceChangeUpdated;
import com.skytala.eCommerce.query.FindProductPriceChangesBy;

@RestController
@RequestMapping("/api/productPriceChange")
public class ProductPriceChangeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPriceChange>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPriceChangeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPriceChange
	 * @return a List with the ProductPriceChanges
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPriceChange> findProductPriceChangesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPriceChangesBy query = new FindProductPriceChangesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPriceChangeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceChangeFound.class,
				event -> sendProductPriceChangesFoundMessage(((ProductPriceChangeFound) event).getProductPriceChanges(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPriceChangesFoundMessage(List<ProductPriceChange> productPriceChanges, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPriceChanges);
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
	public boolean createProductPriceChange(HttpServletRequest request) {

		ProductPriceChange productPriceChangeToBeAdded = new ProductPriceChange();
		try {
			productPriceChangeToBeAdded = ProductPriceChangeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPriceChange(productPriceChangeToBeAdded);

	}

	/**
	 * creates a new ProductPriceChange entry in the ofbiz database
	 * 
	 * @param productPriceChangeToBeAdded
	 *            the ProductPriceChange thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPriceChange(ProductPriceChange productPriceChangeToBeAdded) {

		AddProductPriceChange com = new AddProductPriceChange(productPriceChangeToBeAdded);
		int usedTicketId;

		synchronized (ProductPriceChangeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceChangeAdded.class,
				event -> sendProductPriceChangeChangedMessage(((ProductPriceChangeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPriceChange(HttpServletRequest request) {

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

		ProductPriceChange productPriceChangeToBeUpdated = new ProductPriceChange();

		try {
			productPriceChangeToBeUpdated = ProductPriceChangeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPriceChange(productPriceChangeToBeUpdated);

	}

	/**
	 * Updates the ProductPriceChange with the specific Id
	 * 
	 * @param productPriceChangeToBeUpdated the ProductPriceChange thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPriceChange(ProductPriceChange productPriceChangeToBeUpdated) {

		UpdateProductPriceChange com = new UpdateProductPriceChange(productPriceChangeToBeUpdated);

		int usedTicketId;

		synchronized (ProductPriceChangeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceChangeUpdated.class,
				event -> sendProductPriceChangeChangedMessage(((ProductPriceChangeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPriceChange from the database
	 * 
	 * @param productPriceChangeId:
	 *            the id of the ProductPriceChange thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPriceChangeById(@RequestParam(value = "productPriceChangeId") String productPriceChangeId) {

		DeleteProductPriceChange com = new DeleteProductPriceChange(productPriceChangeId);

		int usedTicketId;

		synchronized (ProductPriceChangeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceChangeDeleted.class,
				event -> sendProductPriceChangeChangedMessage(((ProductPriceChangeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPriceChangeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPriceChange/\" plus one of the following: "
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