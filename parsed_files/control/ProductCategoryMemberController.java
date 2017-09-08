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
import com.skytala.eCommerce.command.AddProductCategoryMember;
import com.skytala.eCommerce.command.DeleteProductCategoryMember;
import com.skytala.eCommerce.command.UpdateProductCategoryMember;
import com.skytala.eCommerce.entity.ProductCategoryMember;
import com.skytala.eCommerce.entity.ProductCategoryMemberMapper;
import com.skytala.eCommerce.event.ProductCategoryMemberAdded;
import com.skytala.eCommerce.event.ProductCategoryMemberDeleted;
import com.skytala.eCommerce.event.ProductCategoryMemberFound;
import com.skytala.eCommerce.event.ProductCategoryMemberUpdated;
import com.skytala.eCommerce.query.FindProductCategoryMembersBy;

@RestController
@RequestMapping("/api/productCategoryMember")
public class ProductCategoryMemberController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductCategoryMember>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductCategoryMemberController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductCategoryMember
	 * @return a List with the ProductCategoryMembers
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductCategoryMember> findProductCategoryMembersBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductCategoryMembersBy query = new FindProductCategoryMembersBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductCategoryMemberController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryMemberFound.class,
				event -> sendProductCategoryMembersFoundMessage(((ProductCategoryMemberFound) event).getProductCategoryMembers(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductCategoryMembersFoundMessage(List<ProductCategoryMember> productCategoryMembers, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productCategoryMembers);
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
	public boolean createProductCategoryMember(HttpServletRequest request) {

		ProductCategoryMember productCategoryMemberToBeAdded = new ProductCategoryMember();
		try {
			productCategoryMemberToBeAdded = ProductCategoryMemberMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductCategoryMember(productCategoryMemberToBeAdded);

	}

	/**
	 * creates a new ProductCategoryMember entry in the ofbiz database
	 * 
	 * @param productCategoryMemberToBeAdded
	 *            the ProductCategoryMember thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductCategoryMember(ProductCategoryMember productCategoryMemberToBeAdded) {

		AddProductCategoryMember com = new AddProductCategoryMember(productCategoryMemberToBeAdded);
		int usedTicketId;

		synchronized (ProductCategoryMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryMemberAdded.class,
				event -> sendProductCategoryMemberChangedMessage(((ProductCategoryMemberAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductCategoryMember(HttpServletRequest request) {

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

		ProductCategoryMember productCategoryMemberToBeUpdated = new ProductCategoryMember();

		try {
			productCategoryMemberToBeUpdated = ProductCategoryMemberMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductCategoryMember(productCategoryMemberToBeUpdated);

	}

	/**
	 * Updates the ProductCategoryMember with the specific Id
	 * 
	 * @param productCategoryMemberToBeUpdated the ProductCategoryMember thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductCategoryMember(ProductCategoryMember productCategoryMemberToBeUpdated) {

		UpdateProductCategoryMember com = new UpdateProductCategoryMember(productCategoryMemberToBeUpdated);

		int usedTicketId;

		synchronized (ProductCategoryMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryMemberUpdated.class,
				event -> sendProductCategoryMemberChangedMessage(((ProductCategoryMemberUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductCategoryMember from the database
	 * 
	 * @param productCategoryMemberId:
	 *            the id of the ProductCategoryMember thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductCategoryMemberById(@RequestParam(value = "productCategoryMemberId") String productCategoryMemberId) {

		DeleteProductCategoryMember com = new DeleteProductCategoryMember(productCategoryMemberId);

		int usedTicketId;

		synchronized (ProductCategoryMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryMemberDeleted.class,
				event -> sendProductCategoryMemberChangedMessage(((ProductCategoryMemberDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductCategoryMemberChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productCategoryMember/\" plus one of the following: "
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
