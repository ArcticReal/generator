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
import com.skytala.eCommerce.command.AddProductRole;
import com.skytala.eCommerce.command.DeleteProductRole;
import com.skytala.eCommerce.command.UpdateProductRole;
import com.skytala.eCommerce.entity.ProductRole;
import com.skytala.eCommerce.entity.ProductRoleMapper;
import com.skytala.eCommerce.event.ProductRoleAdded;
import com.skytala.eCommerce.event.ProductRoleDeleted;
import com.skytala.eCommerce.event.ProductRoleFound;
import com.skytala.eCommerce.event.ProductRoleUpdated;
import com.skytala.eCommerce.query.FindProductRolesBy;

@RestController
@RequestMapping("/api/productRole")
public class ProductRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductRole
	 * @return a List with the ProductRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductRole> findProductRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductRolesBy query = new FindProductRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductRoleFound.class,
				event -> sendProductRolesFoundMessage(((ProductRoleFound) event).getProductRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductRolesFoundMessage(List<ProductRole> productRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productRoles);
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
	public boolean createProductRole(HttpServletRequest request) {

		ProductRole productRoleToBeAdded = new ProductRole();
		try {
			productRoleToBeAdded = ProductRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductRole(productRoleToBeAdded);

	}

	/**
	 * creates a new ProductRole entry in the ofbiz database
	 * 
	 * @param productRoleToBeAdded
	 *            the ProductRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductRole(ProductRole productRoleToBeAdded) {

		AddProductRole com = new AddProductRole(productRoleToBeAdded);
		int usedTicketId;

		synchronized (ProductRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductRoleAdded.class,
				event -> sendProductRoleChangedMessage(((ProductRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductRole(HttpServletRequest request) {

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

		ProductRole productRoleToBeUpdated = new ProductRole();

		try {
			productRoleToBeUpdated = ProductRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductRole(productRoleToBeUpdated);

	}

	/**
	 * Updates the ProductRole with the specific Id
	 * 
	 * @param productRoleToBeUpdated the ProductRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductRole(ProductRole productRoleToBeUpdated) {

		UpdateProductRole com = new UpdateProductRole(productRoleToBeUpdated);

		int usedTicketId;

		synchronized (ProductRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductRoleUpdated.class,
				event -> sendProductRoleChangedMessage(((ProductRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductRole from the database
	 * 
	 * @param productRoleId:
	 *            the id of the ProductRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductRoleById(@RequestParam(value = "productRoleId") String productRoleId) {

		DeleteProductRole com = new DeleteProductRole(productRoleId);

		int usedTicketId;

		synchronized (ProductRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductRoleDeleted.class,
				event -> sendProductRoleChangedMessage(((ProductRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productRole/\" plus one of the following: "
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
