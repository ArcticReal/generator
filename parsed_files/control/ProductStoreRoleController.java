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
import com.skytala.eCommerce.command.AddProductStoreRole;
import com.skytala.eCommerce.command.DeleteProductStoreRole;
import com.skytala.eCommerce.command.UpdateProductStoreRole;
import com.skytala.eCommerce.entity.ProductStoreRole;
import com.skytala.eCommerce.entity.ProductStoreRoleMapper;
import com.skytala.eCommerce.event.ProductStoreRoleAdded;
import com.skytala.eCommerce.event.ProductStoreRoleDeleted;
import com.skytala.eCommerce.event.ProductStoreRoleFound;
import com.skytala.eCommerce.event.ProductStoreRoleUpdated;
import com.skytala.eCommerce.query.FindProductStoreRolesBy;

@RestController
@RequestMapping("/api/productStoreRole")
public class ProductStoreRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreRole
	 * @return a List with the ProductStoreRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreRole> findProductStoreRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreRolesBy query = new FindProductStoreRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreRoleFound.class,
				event -> sendProductStoreRolesFoundMessage(((ProductStoreRoleFound) event).getProductStoreRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreRolesFoundMessage(List<ProductStoreRole> productStoreRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreRoles);
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
	public boolean createProductStoreRole(HttpServletRequest request) {

		ProductStoreRole productStoreRoleToBeAdded = new ProductStoreRole();
		try {
			productStoreRoleToBeAdded = ProductStoreRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreRole(productStoreRoleToBeAdded);

	}

	/**
	 * creates a new ProductStoreRole entry in the ofbiz database
	 * 
	 * @param productStoreRoleToBeAdded
	 *            the ProductStoreRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreRole(ProductStoreRole productStoreRoleToBeAdded) {

		AddProductStoreRole com = new AddProductStoreRole(productStoreRoleToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreRoleAdded.class,
				event -> sendProductStoreRoleChangedMessage(((ProductStoreRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreRole(HttpServletRequest request) {

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

		ProductStoreRole productStoreRoleToBeUpdated = new ProductStoreRole();

		try {
			productStoreRoleToBeUpdated = ProductStoreRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreRole(productStoreRoleToBeUpdated);

	}

	/**
	 * Updates the ProductStoreRole with the specific Id
	 * 
	 * @param productStoreRoleToBeUpdated the ProductStoreRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreRole(ProductStoreRole productStoreRoleToBeUpdated) {

		UpdateProductStoreRole com = new UpdateProductStoreRole(productStoreRoleToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreRoleUpdated.class,
				event -> sendProductStoreRoleChangedMessage(((ProductStoreRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreRole from the database
	 * 
	 * @param productStoreRoleId:
	 *            the id of the ProductStoreRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreRoleById(@RequestParam(value = "productStoreRoleId") String productStoreRoleId) {

		DeleteProductStoreRole com = new DeleteProductStoreRole(productStoreRoleId);

		int usedTicketId;

		synchronized (ProductStoreRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreRoleDeleted.class,
				event -> sendProductStoreRoleChangedMessage(((ProductStoreRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreRole/\" plus one of the following: "
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
