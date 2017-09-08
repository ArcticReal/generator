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
import com.skytala.eCommerce.command.AddProductGlAccount;
import com.skytala.eCommerce.command.DeleteProductGlAccount;
import com.skytala.eCommerce.command.UpdateProductGlAccount;
import com.skytala.eCommerce.entity.ProductGlAccount;
import com.skytala.eCommerce.entity.ProductGlAccountMapper;
import com.skytala.eCommerce.event.ProductGlAccountAdded;
import com.skytala.eCommerce.event.ProductGlAccountDeleted;
import com.skytala.eCommerce.event.ProductGlAccountFound;
import com.skytala.eCommerce.event.ProductGlAccountUpdated;
import com.skytala.eCommerce.query.FindProductGlAccountsBy;

@RestController
@RequestMapping("/api/productGlAccount")
public class ProductGlAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductGlAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductGlAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductGlAccount
	 * @return a List with the ProductGlAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductGlAccount> findProductGlAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductGlAccountsBy query = new FindProductGlAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductGlAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductGlAccountFound.class,
				event -> sendProductGlAccountsFoundMessage(((ProductGlAccountFound) event).getProductGlAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductGlAccountsFoundMessage(List<ProductGlAccount> productGlAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productGlAccounts);
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
	public boolean createProductGlAccount(HttpServletRequest request) {

		ProductGlAccount productGlAccountToBeAdded = new ProductGlAccount();
		try {
			productGlAccountToBeAdded = ProductGlAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductGlAccount(productGlAccountToBeAdded);

	}

	/**
	 * creates a new ProductGlAccount entry in the ofbiz database
	 * 
	 * @param productGlAccountToBeAdded
	 *            the ProductGlAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductGlAccount(ProductGlAccount productGlAccountToBeAdded) {

		AddProductGlAccount com = new AddProductGlAccount(productGlAccountToBeAdded);
		int usedTicketId;

		synchronized (ProductGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductGlAccountAdded.class,
				event -> sendProductGlAccountChangedMessage(((ProductGlAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductGlAccount(HttpServletRequest request) {

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

		ProductGlAccount productGlAccountToBeUpdated = new ProductGlAccount();

		try {
			productGlAccountToBeUpdated = ProductGlAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductGlAccount(productGlAccountToBeUpdated);

	}

	/**
	 * Updates the ProductGlAccount with the specific Id
	 * 
	 * @param productGlAccountToBeUpdated the ProductGlAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductGlAccount(ProductGlAccount productGlAccountToBeUpdated) {

		UpdateProductGlAccount com = new UpdateProductGlAccount(productGlAccountToBeUpdated);

		int usedTicketId;

		synchronized (ProductGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductGlAccountUpdated.class,
				event -> sendProductGlAccountChangedMessage(((ProductGlAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductGlAccount from the database
	 * 
	 * @param productGlAccountId:
	 *            the id of the ProductGlAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductGlAccountById(@RequestParam(value = "productGlAccountId") String productGlAccountId) {

		DeleteProductGlAccount com = new DeleteProductGlAccount(productGlAccountId);

		int usedTicketId;

		synchronized (ProductGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductGlAccountDeleted.class,
				event -> sendProductGlAccountChangedMessage(((ProductGlAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductGlAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productGlAccount/\" plus one of the following: "
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
