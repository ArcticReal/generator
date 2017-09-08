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
import com.skytala.eCommerce.command.AddProductPriceAction;
import com.skytala.eCommerce.command.DeleteProductPriceAction;
import com.skytala.eCommerce.command.UpdateProductPriceAction;
import com.skytala.eCommerce.entity.ProductPriceAction;
import com.skytala.eCommerce.entity.ProductPriceActionMapper;
import com.skytala.eCommerce.event.ProductPriceActionAdded;
import com.skytala.eCommerce.event.ProductPriceActionDeleted;
import com.skytala.eCommerce.event.ProductPriceActionFound;
import com.skytala.eCommerce.event.ProductPriceActionUpdated;
import com.skytala.eCommerce.query.FindProductPriceActionsBy;

@RestController
@RequestMapping("/api/productPriceAction")
public class ProductPriceActionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPriceAction>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPriceActionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPriceAction
	 * @return a List with the ProductPriceActions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPriceAction> findProductPriceActionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPriceActionsBy query = new FindProductPriceActionsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPriceActionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceActionFound.class,
				event -> sendProductPriceActionsFoundMessage(((ProductPriceActionFound) event).getProductPriceActions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPriceActionsFoundMessage(List<ProductPriceAction> productPriceActions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPriceActions);
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
	public boolean createProductPriceAction(HttpServletRequest request) {

		ProductPriceAction productPriceActionToBeAdded = new ProductPriceAction();
		try {
			productPriceActionToBeAdded = ProductPriceActionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPriceAction(productPriceActionToBeAdded);

	}

	/**
	 * creates a new ProductPriceAction entry in the ofbiz database
	 * 
	 * @param productPriceActionToBeAdded
	 *            the ProductPriceAction thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPriceAction(ProductPriceAction productPriceActionToBeAdded) {

		AddProductPriceAction com = new AddProductPriceAction(productPriceActionToBeAdded);
		int usedTicketId;

		synchronized (ProductPriceActionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceActionAdded.class,
				event -> sendProductPriceActionChangedMessage(((ProductPriceActionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPriceAction(HttpServletRequest request) {

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

		ProductPriceAction productPriceActionToBeUpdated = new ProductPriceAction();

		try {
			productPriceActionToBeUpdated = ProductPriceActionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPriceAction(productPriceActionToBeUpdated);

	}

	/**
	 * Updates the ProductPriceAction with the specific Id
	 * 
	 * @param productPriceActionToBeUpdated the ProductPriceAction thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPriceAction(ProductPriceAction productPriceActionToBeUpdated) {

		UpdateProductPriceAction com = new UpdateProductPriceAction(productPriceActionToBeUpdated);

		int usedTicketId;

		synchronized (ProductPriceActionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceActionUpdated.class,
				event -> sendProductPriceActionChangedMessage(((ProductPriceActionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPriceAction from the database
	 * 
	 * @param productPriceActionId:
	 *            the id of the ProductPriceAction thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPriceActionById(@RequestParam(value = "productPriceActionId") String productPriceActionId) {

		DeleteProductPriceAction com = new DeleteProductPriceAction(productPriceActionId);

		int usedTicketId;

		synchronized (ProductPriceActionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceActionDeleted.class,
				event -> sendProductPriceActionChangedMessage(((ProductPriceActionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPriceActionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPriceAction/\" plus one of the following: "
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
