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
import com.skytala.eCommerce.command.AddProductCategoryRole;
import com.skytala.eCommerce.command.DeleteProductCategoryRole;
import com.skytala.eCommerce.command.UpdateProductCategoryRole;
import com.skytala.eCommerce.entity.ProductCategoryRole;
import com.skytala.eCommerce.entity.ProductCategoryRoleMapper;
import com.skytala.eCommerce.event.ProductCategoryRoleAdded;
import com.skytala.eCommerce.event.ProductCategoryRoleDeleted;
import com.skytala.eCommerce.event.ProductCategoryRoleFound;
import com.skytala.eCommerce.event.ProductCategoryRoleUpdated;
import com.skytala.eCommerce.query.FindProductCategoryRolesBy;

@RestController
@RequestMapping("/api/productCategoryRole")
public class ProductCategoryRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductCategoryRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductCategoryRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductCategoryRole
	 * @return a List with the ProductCategoryRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductCategoryRole> findProductCategoryRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductCategoryRolesBy query = new FindProductCategoryRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductCategoryRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryRoleFound.class,
				event -> sendProductCategoryRolesFoundMessage(((ProductCategoryRoleFound) event).getProductCategoryRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductCategoryRolesFoundMessage(List<ProductCategoryRole> productCategoryRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productCategoryRoles);
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
	public boolean createProductCategoryRole(HttpServletRequest request) {

		ProductCategoryRole productCategoryRoleToBeAdded = new ProductCategoryRole();
		try {
			productCategoryRoleToBeAdded = ProductCategoryRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductCategoryRole(productCategoryRoleToBeAdded);

	}

	/**
	 * creates a new ProductCategoryRole entry in the ofbiz database
	 * 
	 * @param productCategoryRoleToBeAdded
	 *            the ProductCategoryRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductCategoryRole(ProductCategoryRole productCategoryRoleToBeAdded) {

		AddProductCategoryRole com = new AddProductCategoryRole(productCategoryRoleToBeAdded);
		int usedTicketId;

		synchronized (ProductCategoryRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryRoleAdded.class,
				event -> sendProductCategoryRoleChangedMessage(((ProductCategoryRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductCategoryRole(HttpServletRequest request) {

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

		ProductCategoryRole productCategoryRoleToBeUpdated = new ProductCategoryRole();

		try {
			productCategoryRoleToBeUpdated = ProductCategoryRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductCategoryRole(productCategoryRoleToBeUpdated);

	}

	/**
	 * Updates the ProductCategoryRole with the specific Id
	 * 
	 * @param productCategoryRoleToBeUpdated the ProductCategoryRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductCategoryRole(ProductCategoryRole productCategoryRoleToBeUpdated) {

		UpdateProductCategoryRole com = new UpdateProductCategoryRole(productCategoryRoleToBeUpdated);

		int usedTicketId;

		synchronized (ProductCategoryRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryRoleUpdated.class,
				event -> sendProductCategoryRoleChangedMessage(((ProductCategoryRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductCategoryRole from the database
	 * 
	 * @param productCategoryRoleId:
	 *            the id of the ProductCategoryRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductCategoryRoleById(@RequestParam(value = "productCategoryRoleId") String productCategoryRoleId) {

		DeleteProductCategoryRole com = new DeleteProductCategoryRole(productCategoryRoleId);

		int usedTicketId;

		synchronized (ProductCategoryRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryRoleDeleted.class,
				event -> sendProductCategoryRoleChangedMessage(((ProductCategoryRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductCategoryRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productCategoryRole/\" plus one of the following: "
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
