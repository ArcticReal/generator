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
import com.skytala.eCommerce.command.AddProductPromoAction;
import com.skytala.eCommerce.command.DeleteProductPromoAction;
import com.skytala.eCommerce.command.UpdateProductPromoAction;
import com.skytala.eCommerce.entity.ProductPromoAction;
import com.skytala.eCommerce.entity.ProductPromoActionMapper;
import com.skytala.eCommerce.event.ProductPromoActionAdded;
import com.skytala.eCommerce.event.ProductPromoActionDeleted;
import com.skytala.eCommerce.event.ProductPromoActionFound;
import com.skytala.eCommerce.event.ProductPromoActionUpdated;
import com.skytala.eCommerce.query.FindProductPromoActionsBy;

@RestController
@RequestMapping("/api/productPromoAction")
public class ProductPromoActionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPromoAction>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPromoActionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPromoAction
	 * @return a List with the ProductPromoActions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPromoAction> findProductPromoActionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPromoActionsBy query = new FindProductPromoActionsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPromoActionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoActionFound.class,
				event -> sendProductPromoActionsFoundMessage(((ProductPromoActionFound) event).getProductPromoActions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPromoActionsFoundMessage(List<ProductPromoAction> productPromoActions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPromoActions);
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
	public boolean createProductPromoAction(HttpServletRequest request) {

		ProductPromoAction productPromoActionToBeAdded = new ProductPromoAction();
		try {
			productPromoActionToBeAdded = ProductPromoActionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPromoAction(productPromoActionToBeAdded);

	}

	/**
	 * creates a new ProductPromoAction entry in the ofbiz database
	 * 
	 * @param productPromoActionToBeAdded
	 *            the ProductPromoAction thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPromoAction(ProductPromoAction productPromoActionToBeAdded) {

		AddProductPromoAction com = new AddProductPromoAction(productPromoActionToBeAdded);
		int usedTicketId;

		synchronized (ProductPromoActionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoActionAdded.class,
				event -> sendProductPromoActionChangedMessage(((ProductPromoActionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPromoAction(HttpServletRequest request) {

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

		ProductPromoAction productPromoActionToBeUpdated = new ProductPromoAction();

		try {
			productPromoActionToBeUpdated = ProductPromoActionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPromoAction(productPromoActionToBeUpdated);

	}

	/**
	 * Updates the ProductPromoAction with the specific Id
	 * 
	 * @param productPromoActionToBeUpdated the ProductPromoAction thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPromoAction(ProductPromoAction productPromoActionToBeUpdated) {

		UpdateProductPromoAction com = new UpdateProductPromoAction(productPromoActionToBeUpdated);

		int usedTicketId;

		synchronized (ProductPromoActionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoActionUpdated.class,
				event -> sendProductPromoActionChangedMessage(((ProductPromoActionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPromoAction from the database
	 * 
	 * @param productPromoActionId:
	 *            the id of the ProductPromoAction thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPromoActionById(@RequestParam(value = "productPromoActionId") String productPromoActionId) {

		DeleteProductPromoAction com = new DeleteProductPromoAction(productPromoActionId);

		int usedTicketId;

		synchronized (ProductPromoActionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoActionDeleted.class,
				event -> sendProductPromoActionChangedMessage(((ProductPromoActionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPromoActionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPromoAction/\" plus one of the following: "
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
