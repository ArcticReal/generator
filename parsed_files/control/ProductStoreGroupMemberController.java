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
import com.skytala.eCommerce.command.AddProductStoreGroupMember;
import com.skytala.eCommerce.command.DeleteProductStoreGroupMember;
import com.skytala.eCommerce.command.UpdateProductStoreGroupMember;
import com.skytala.eCommerce.entity.ProductStoreGroupMember;
import com.skytala.eCommerce.entity.ProductStoreGroupMemberMapper;
import com.skytala.eCommerce.event.ProductStoreGroupMemberAdded;
import com.skytala.eCommerce.event.ProductStoreGroupMemberDeleted;
import com.skytala.eCommerce.event.ProductStoreGroupMemberFound;
import com.skytala.eCommerce.event.ProductStoreGroupMemberUpdated;
import com.skytala.eCommerce.query.FindProductStoreGroupMembersBy;

@RestController
@RequestMapping("/api/productStoreGroupMember")
public class ProductStoreGroupMemberController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreGroupMember>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreGroupMemberController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreGroupMember
	 * @return a List with the ProductStoreGroupMembers
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreGroupMember> findProductStoreGroupMembersBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreGroupMembersBy query = new FindProductStoreGroupMembersBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreGroupMemberController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupMemberFound.class,
				event -> sendProductStoreGroupMembersFoundMessage(((ProductStoreGroupMemberFound) event).getProductStoreGroupMembers(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreGroupMembersFoundMessage(List<ProductStoreGroupMember> productStoreGroupMembers, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreGroupMembers);
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
	public boolean createProductStoreGroupMember(HttpServletRequest request) {

		ProductStoreGroupMember productStoreGroupMemberToBeAdded = new ProductStoreGroupMember();
		try {
			productStoreGroupMemberToBeAdded = ProductStoreGroupMemberMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreGroupMember(productStoreGroupMemberToBeAdded);

	}

	/**
	 * creates a new ProductStoreGroupMember entry in the ofbiz database
	 * 
	 * @param productStoreGroupMemberToBeAdded
	 *            the ProductStoreGroupMember thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreGroupMember(ProductStoreGroupMember productStoreGroupMemberToBeAdded) {

		AddProductStoreGroupMember com = new AddProductStoreGroupMember(productStoreGroupMemberToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreGroupMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupMemberAdded.class,
				event -> sendProductStoreGroupMemberChangedMessage(((ProductStoreGroupMemberAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreGroupMember(HttpServletRequest request) {

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

		ProductStoreGroupMember productStoreGroupMemberToBeUpdated = new ProductStoreGroupMember();

		try {
			productStoreGroupMemberToBeUpdated = ProductStoreGroupMemberMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreGroupMember(productStoreGroupMemberToBeUpdated);

	}

	/**
	 * Updates the ProductStoreGroupMember with the specific Id
	 * 
	 * @param productStoreGroupMemberToBeUpdated the ProductStoreGroupMember thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreGroupMember(ProductStoreGroupMember productStoreGroupMemberToBeUpdated) {

		UpdateProductStoreGroupMember com = new UpdateProductStoreGroupMember(productStoreGroupMemberToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreGroupMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupMemberUpdated.class,
				event -> sendProductStoreGroupMemberChangedMessage(((ProductStoreGroupMemberUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreGroupMember from the database
	 * 
	 * @param productStoreGroupMemberId:
	 *            the id of the ProductStoreGroupMember thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreGroupMemberById(@RequestParam(value = "productStoreGroupMemberId") String productStoreGroupMemberId) {

		DeleteProductStoreGroupMember com = new DeleteProductStoreGroupMember(productStoreGroupMemberId);

		int usedTicketId;

		synchronized (ProductStoreGroupMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupMemberDeleted.class,
				event -> sendProductStoreGroupMemberChangedMessage(((ProductStoreGroupMemberDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreGroupMemberChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreGroupMember/\" plus one of the following: "
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
