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
import com.skytala.eCommerce.command.AddProductFeatureGroup;
import com.skytala.eCommerce.command.DeleteProductFeatureGroup;
import com.skytala.eCommerce.command.UpdateProductFeatureGroup;
import com.skytala.eCommerce.entity.ProductFeatureGroup;
import com.skytala.eCommerce.entity.ProductFeatureGroupMapper;
import com.skytala.eCommerce.event.ProductFeatureGroupAdded;
import com.skytala.eCommerce.event.ProductFeatureGroupDeleted;
import com.skytala.eCommerce.event.ProductFeatureGroupFound;
import com.skytala.eCommerce.event.ProductFeatureGroupUpdated;
import com.skytala.eCommerce.query.FindProductFeatureGroupsBy;

@RestController
@RequestMapping("/api/productFeatureGroup")
public class ProductFeatureGroupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFeatureGroup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFeatureGroupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFeatureGroup
	 * @return a List with the ProductFeatureGroups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFeatureGroup> findProductFeatureGroupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFeatureGroupsBy query = new FindProductFeatureGroupsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFeatureGroupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureGroupFound.class,
				event -> sendProductFeatureGroupsFoundMessage(((ProductFeatureGroupFound) event).getProductFeatureGroups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFeatureGroupsFoundMessage(List<ProductFeatureGroup> productFeatureGroups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFeatureGroups);
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
	public boolean createProductFeatureGroup(HttpServletRequest request) {

		ProductFeatureGroup productFeatureGroupToBeAdded = new ProductFeatureGroup();
		try {
			productFeatureGroupToBeAdded = ProductFeatureGroupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFeatureGroup(productFeatureGroupToBeAdded);

	}

	/**
	 * creates a new ProductFeatureGroup entry in the ofbiz database
	 * 
	 * @param productFeatureGroupToBeAdded
	 *            the ProductFeatureGroup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFeatureGroup(ProductFeatureGroup productFeatureGroupToBeAdded) {

		AddProductFeatureGroup com = new AddProductFeatureGroup(productFeatureGroupToBeAdded);
		int usedTicketId;

		synchronized (ProductFeatureGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureGroupAdded.class,
				event -> sendProductFeatureGroupChangedMessage(((ProductFeatureGroupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFeatureGroup(HttpServletRequest request) {

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

		ProductFeatureGroup productFeatureGroupToBeUpdated = new ProductFeatureGroup();

		try {
			productFeatureGroupToBeUpdated = ProductFeatureGroupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFeatureGroup(productFeatureGroupToBeUpdated);

	}

	/**
	 * Updates the ProductFeatureGroup with the specific Id
	 * 
	 * @param productFeatureGroupToBeUpdated the ProductFeatureGroup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFeatureGroup(ProductFeatureGroup productFeatureGroupToBeUpdated) {

		UpdateProductFeatureGroup com = new UpdateProductFeatureGroup(productFeatureGroupToBeUpdated);

		int usedTicketId;

		synchronized (ProductFeatureGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureGroupUpdated.class,
				event -> sendProductFeatureGroupChangedMessage(((ProductFeatureGroupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFeatureGroup from the database
	 * 
	 * @param productFeatureGroupId:
	 *            the id of the ProductFeatureGroup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFeatureGroupById(@RequestParam(value = "productFeatureGroupId") String productFeatureGroupId) {

		DeleteProductFeatureGroup com = new DeleteProductFeatureGroup(productFeatureGroupId);

		int usedTicketId;

		synchronized (ProductFeatureGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureGroupDeleted.class,
				event -> sendProductFeatureGroupChangedMessage(((ProductFeatureGroupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFeatureGroupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFeatureGroup/\" plus one of the following: "
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
