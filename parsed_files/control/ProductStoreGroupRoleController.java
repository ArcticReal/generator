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
import com.skytala.eCommerce.command.AddProductStoreGroupRole;
import com.skytala.eCommerce.command.DeleteProductStoreGroupRole;
import com.skytala.eCommerce.command.UpdateProductStoreGroupRole;
import com.skytala.eCommerce.entity.ProductStoreGroupRole;
import com.skytala.eCommerce.entity.ProductStoreGroupRoleMapper;
import com.skytala.eCommerce.event.ProductStoreGroupRoleAdded;
import com.skytala.eCommerce.event.ProductStoreGroupRoleDeleted;
import com.skytala.eCommerce.event.ProductStoreGroupRoleFound;
import com.skytala.eCommerce.event.ProductStoreGroupRoleUpdated;
import com.skytala.eCommerce.query.FindProductStoreGroupRolesBy;

@RestController
@RequestMapping("/api/productStoreGroupRole")
public class ProductStoreGroupRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreGroupRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreGroupRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreGroupRole
	 * @return a List with the ProductStoreGroupRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreGroupRole> findProductStoreGroupRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreGroupRolesBy query = new FindProductStoreGroupRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreGroupRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupRoleFound.class,
				event -> sendProductStoreGroupRolesFoundMessage(((ProductStoreGroupRoleFound) event).getProductStoreGroupRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreGroupRolesFoundMessage(List<ProductStoreGroupRole> productStoreGroupRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreGroupRoles);
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
	public boolean createProductStoreGroupRole(HttpServletRequest request) {

		ProductStoreGroupRole productStoreGroupRoleToBeAdded = new ProductStoreGroupRole();
		try {
			productStoreGroupRoleToBeAdded = ProductStoreGroupRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreGroupRole(productStoreGroupRoleToBeAdded);

	}

	/**
	 * creates a new ProductStoreGroupRole entry in the ofbiz database
	 * 
	 * @param productStoreGroupRoleToBeAdded
	 *            the ProductStoreGroupRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreGroupRole(ProductStoreGroupRole productStoreGroupRoleToBeAdded) {

		AddProductStoreGroupRole com = new AddProductStoreGroupRole(productStoreGroupRoleToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreGroupRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupRoleAdded.class,
				event -> sendProductStoreGroupRoleChangedMessage(((ProductStoreGroupRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreGroupRole(HttpServletRequest request) {

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

		ProductStoreGroupRole productStoreGroupRoleToBeUpdated = new ProductStoreGroupRole();

		try {
			productStoreGroupRoleToBeUpdated = ProductStoreGroupRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreGroupRole(productStoreGroupRoleToBeUpdated);

	}

	/**
	 * Updates the ProductStoreGroupRole with the specific Id
	 * 
	 * @param productStoreGroupRoleToBeUpdated the ProductStoreGroupRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreGroupRole(ProductStoreGroupRole productStoreGroupRoleToBeUpdated) {

		UpdateProductStoreGroupRole com = new UpdateProductStoreGroupRole(productStoreGroupRoleToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreGroupRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupRoleUpdated.class,
				event -> sendProductStoreGroupRoleChangedMessage(((ProductStoreGroupRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreGroupRole from the database
	 * 
	 * @param productStoreGroupRoleId:
	 *            the id of the ProductStoreGroupRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreGroupRoleById(@RequestParam(value = "productStoreGroupRoleId") String productStoreGroupRoleId) {

		DeleteProductStoreGroupRole com = new DeleteProductStoreGroupRole(productStoreGroupRoleId);

		int usedTicketId;

		synchronized (ProductStoreGroupRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupRoleDeleted.class,
				event -> sendProductStoreGroupRoleChangedMessage(((ProductStoreGroupRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreGroupRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreGroupRole/\" plus one of the following: "
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
