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
import com.skytala.eCommerce.command.AddProductStoreGroup;
import com.skytala.eCommerce.command.DeleteProductStoreGroup;
import com.skytala.eCommerce.command.UpdateProductStoreGroup;
import com.skytala.eCommerce.entity.ProductStoreGroup;
import com.skytala.eCommerce.entity.ProductStoreGroupMapper;
import com.skytala.eCommerce.event.ProductStoreGroupAdded;
import com.skytala.eCommerce.event.ProductStoreGroupDeleted;
import com.skytala.eCommerce.event.ProductStoreGroupFound;
import com.skytala.eCommerce.event.ProductStoreGroupUpdated;
import com.skytala.eCommerce.query.FindProductStoreGroupsBy;

@RestController
@RequestMapping("/api/productStoreGroup")
public class ProductStoreGroupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreGroup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreGroupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreGroup
	 * @return a List with the ProductStoreGroups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreGroup> findProductStoreGroupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreGroupsBy query = new FindProductStoreGroupsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreGroupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupFound.class,
				event -> sendProductStoreGroupsFoundMessage(((ProductStoreGroupFound) event).getProductStoreGroups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreGroupsFoundMessage(List<ProductStoreGroup> productStoreGroups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreGroups);
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
	public boolean createProductStoreGroup(HttpServletRequest request) {

		ProductStoreGroup productStoreGroupToBeAdded = new ProductStoreGroup();
		try {
			productStoreGroupToBeAdded = ProductStoreGroupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreGroup(productStoreGroupToBeAdded);

	}

	/**
	 * creates a new ProductStoreGroup entry in the ofbiz database
	 * 
	 * @param productStoreGroupToBeAdded
	 *            the ProductStoreGroup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreGroup(ProductStoreGroup productStoreGroupToBeAdded) {

		AddProductStoreGroup com = new AddProductStoreGroup(productStoreGroupToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupAdded.class,
				event -> sendProductStoreGroupChangedMessage(((ProductStoreGroupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreGroup(HttpServletRequest request) {

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

		ProductStoreGroup productStoreGroupToBeUpdated = new ProductStoreGroup();

		try {
			productStoreGroupToBeUpdated = ProductStoreGroupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreGroup(productStoreGroupToBeUpdated);

	}

	/**
	 * Updates the ProductStoreGroup with the specific Id
	 * 
	 * @param productStoreGroupToBeUpdated the ProductStoreGroup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreGroup(ProductStoreGroup productStoreGroupToBeUpdated) {

		UpdateProductStoreGroup com = new UpdateProductStoreGroup(productStoreGroupToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupUpdated.class,
				event -> sendProductStoreGroupChangedMessage(((ProductStoreGroupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreGroup from the database
	 * 
	 * @param productStoreGroupId:
	 *            the id of the ProductStoreGroup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreGroupById(@RequestParam(value = "productStoreGroupId") String productStoreGroupId) {

		DeleteProductStoreGroup com = new DeleteProductStoreGroup(productStoreGroupId);

		int usedTicketId;

		synchronized (ProductStoreGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupDeleted.class,
				event -> sendProductStoreGroupChangedMessage(((ProductStoreGroupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreGroupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreGroup/\" plus one of the following: "
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
