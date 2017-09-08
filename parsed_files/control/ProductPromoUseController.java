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
import com.skytala.eCommerce.command.AddProductPromoUse;
import com.skytala.eCommerce.command.DeleteProductPromoUse;
import com.skytala.eCommerce.command.UpdateProductPromoUse;
import com.skytala.eCommerce.entity.ProductPromoUse;
import com.skytala.eCommerce.entity.ProductPromoUseMapper;
import com.skytala.eCommerce.event.ProductPromoUseAdded;
import com.skytala.eCommerce.event.ProductPromoUseDeleted;
import com.skytala.eCommerce.event.ProductPromoUseFound;
import com.skytala.eCommerce.event.ProductPromoUseUpdated;
import com.skytala.eCommerce.query.FindProductPromoUsesBy;

@RestController
@RequestMapping("/api/productPromoUse")
public class ProductPromoUseController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPromoUse>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPromoUseController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPromoUse
	 * @return a List with the ProductPromoUses
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPromoUse> findProductPromoUsesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPromoUsesBy query = new FindProductPromoUsesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPromoUseController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoUseFound.class,
				event -> sendProductPromoUsesFoundMessage(((ProductPromoUseFound) event).getProductPromoUses(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPromoUsesFoundMessage(List<ProductPromoUse> productPromoUses, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPromoUses);
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
	public boolean createProductPromoUse(HttpServletRequest request) {

		ProductPromoUse productPromoUseToBeAdded = new ProductPromoUse();
		try {
			productPromoUseToBeAdded = ProductPromoUseMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPromoUse(productPromoUseToBeAdded);

	}

	/**
	 * creates a new ProductPromoUse entry in the ofbiz database
	 * 
	 * @param productPromoUseToBeAdded
	 *            the ProductPromoUse thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPromoUse(ProductPromoUse productPromoUseToBeAdded) {

		AddProductPromoUse com = new AddProductPromoUse(productPromoUseToBeAdded);
		int usedTicketId;

		synchronized (ProductPromoUseController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoUseAdded.class,
				event -> sendProductPromoUseChangedMessage(((ProductPromoUseAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPromoUse(HttpServletRequest request) {

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

		ProductPromoUse productPromoUseToBeUpdated = new ProductPromoUse();

		try {
			productPromoUseToBeUpdated = ProductPromoUseMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPromoUse(productPromoUseToBeUpdated);

	}

	/**
	 * Updates the ProductPromoUse with the specific Id
	 * 
	 * @param productPromoUseToBeUpdated the ProductPromoUse thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPromoUse(ProductPromoUse productPromoUseToBeUpdated) {

		UpdateProductPromoUse com = new UpdateProductPromoUse(productPromoUseToBeUpdated);

		int usedTicketId;

		synchronized (ProductPromoUseController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoUseUpdated.class,
				event -> sendProductPromoUseChangedMessage(((ProductPromoUseUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPromoUse from the database
	 * 
	 * @param productPromoUseId:
	 *            the id of the ProductPromoUse thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPromoUseById(@RequestParam(value = "productPromoUseId") String productPromoUseId) {

		DeleteProductPromoUse com = new DeleteProductPromoUse(productPromoUseId);

		int usedTicketId;

		synchronized (ProductPromoUseController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoUseDeleted.class,
				event -> sendProductPromoUseChangedMessage(((ProductPromoUseDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPromoUseChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPromoUse/\" plus one of the following: "
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
