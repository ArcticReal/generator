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
import com.skytala.eCommerce.command.AddProductPromoCodeParty;
import com.skytala.eCommerce.command.DeleteProductPromoCodeParty;
import com.skytala.eCommerce.command.UpdateProductPromoCodeParty;
import com.skytala.eCommerce.entity.ProductPromoCodeParty;
import com.skytala.eCommerce.entity.ProductPromoCodePartyMapper;
import com.skytala.eCommerce.event.ProductPromoCodePartyAdded;
import com.skytala.eCommerce.event.ProductPromoCodePartyDeleted;
import com.skytala.eCommerce.event.ProductPromoCodePartyFound;
import com.skytala.eCommerce.event.ProductPromoCodePartyUpdated;
import com.skytala.eCommerce.query.FindProductPromoCodePartysBy;

@RestController
@RequestMapping("/api/productPromoCodeParty")
public class ProductPromoCodePartyController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPromoCodeParty>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPromoCodePartyController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPromoCodeParty
	 * @return a List with the ProductPromoCodePartys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPromoCodeParty> findProductPromoCodePartysBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPromoCodePartysBy query = new FindProductPromoCodePartysBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPromoCodePartyController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCodePartyFound.class,
				event -> sendProductPromoCodePartysFoundMessage(((ProductPromoCodePartyFound) event).getProductPromoCodePartys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPromoCodePartysFoundMessage(List<ProductPromoCodeParty> productPromoCodePartys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPromoCodePartys);
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
	public boolean createProductPromoCodeParty(HttpServletRequest request) {

		ProductPromoCodeParty productPromoCodePartyToBeAdded = new ProductPromoCodeParty();
		try {
			productPromoCodePartyToBeAdded = ProductPromoCodePartyMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPromoCodeParty(productPromoCodePartyToBeAdded);

	}

	/**
	 * creates a new ProductPromoCodeParty entry in the ofbiz database
	 * 
	 * @param productPromoCodePartyToBeAdded
	 *            the ProductPromoCodeParty thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPromoCodeParty(ProductPromoCodeParty productPromoCodePartyToBeAdded) {

		AddProductPromoCodeParty com = new AddProductPromoCodeParty(productPromoCodePartyToBeAdded);
		int usedTicketId;

		synchronized (ProductPromoCodePartyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCodePartyAdded.class,
				event -> sendProductPromoCodePartyChangedMessage(((ProductPromoCodePartyAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPromoCodeParty(HttpServletRequest request) {

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

		ProductPromoCodeParty productPromoCodePartyToBeUpdated = new ProductPromoCodeParty();

		try {
			productPromoCodePartyToBeUpdated = ProductPromoCodePartyMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPromoCodeParty(productPromoCodePartyToBeUpdated);

	}

	/**
	 * Updates the ProductPromoCodeParty with the specific Id
	 * 
	 * @param productPromoCodePartyToBeUpdated the ProductPromoCodeParty thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPromoCodeParty(ProductPromoCodeParty productPromoCodePartyToBeUpdated) {

		UpdateProductPromoCodeParty com = new UpdateProductPromoCodeParty(productPromoCodePartyToBeUpdated);

		int usedTicketId;

		synchronized (ProductPromoCodePartyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCodePartyUpdated.class,
				event -> sendProductPromoCodePartyChangedMessage(((ProductPromoCodePartyUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPromoCodeParty from the database
	 * 
	 * @param productPromoCodePartyId:
	 *            the id of the ProductPromoCodeParty thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPromoCodePartyById(@RequestParam(value = "productPromoCodePartyId") String productPromoCodePartyId) {

		DeleteProductPromoCodeParty com = new DeleteProductPromoCodeParty(productPromoCodePartyId);

		int usedTicketId;

		synchronized (ProductPromoCodePartyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCodePartyDeleted.class,
				event -> sendProductPromoCodePartyChangedMessage(((ProductPromoCodePartyDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPromoCodePartyChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPromoCodeParty/\" plus one of the following: "
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
