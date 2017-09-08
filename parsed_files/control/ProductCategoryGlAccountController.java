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
import com.skytala.eCommerce.command.AddProductCategoryGlAccount;
import com.skytala.eCommerce.command.DeleteProductCategoryGlAccount;
import com.skytala.eCommerce.command.UpdateProductCategoryGlAccount;
import com.skytala.eCommerce.entity.ProductCategoryGlAccount;
import com.skytala.eCommerce.entity.ProductCategoryGlAccountMapper;
import com.skytala.eCommerce.event.ProductCategoryGlAccountAdded;
import com.skytala.eCommerce.event.ProductCategoryGlAccountDeleted;
import com.skytala.eCommerce.event.ProductCategoryGlAccountFound;
import com.skytala.eCommerce.event.ProductCategoryGlAccountUpdated;
import com.skytala.eCommerce.query.FindProductCategoryGlAccountsBy;

@RestController
@RequestMapping("/api/productCategoryGlAccount")
public class ProductCategoryGlAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductCategoryGlAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductCategoryGlAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductCategoryGlAccount
	 * @return a List with the ProductCategoryGlAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductCategoryGlAccount> findProductCategoryGlAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductCategoryGlAccountsBy query = new FindProductCategoryGlAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductCategoryGlAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryGlAccountFound.class,
				event -> sendProductCategoryGlAccountsFoundMessage(((ProductCategoryGlAccountFound) event).getProductCategoryGlAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductCategoryGlAccountsFoundMessage(List<ProductCategoryGlAccount> productCategoryGlAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productCategoryGlAccounts);
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
	public boolean createProductCategoryGlAccount(HttpServletRequest request) {

		ProductCategoryGlAccount productCategoryGlAccountToBeAdded = new ProductCategoryGlAccount();
		try {
			productCategoryGlAccountToBeAdded = ProductCategoryGlAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductCategoryGlAccount(productCategoryGlAccountToBeAdded);

	}

	/**
	 * creates a new ProductCategoryGlAccount entry in the ofbiz database
	 * 
	 * @param productCategoryGlAccountToBeAdded
	 *            the ProductCategoryGlAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductCategoryGlAccount(ProductCategoryGlAccount productCategoryGlAccountToBeAdded) {

		AddProductCategoryGlAccount com = new AddProductCategoryGlAccount(productCategoryGlAccountToBeAdded);
		int usedTicketId;

		synchronized (ProductCategoryGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryGlAccountAdded.class,
				event -> sendProductCategoryGlAccountChangedMessage(((ProductCategoryGlAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductCategoryGlAccount(HttpServletRequest request) {

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

		ProductCategoryGlAccount productCategoryGlAccountToBeUpdated = new ProductCategoryGlAccount();

		try {
			productCategoryGlAccountToBeUpdated = ProductCategoryGlAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductCategoryGlAccount(productCategoryGlAccountToBeUpdated);

	}

	/**
	 * Updates the ProductCategoryGlAccount with the specific Id
	 * 
	 * @param productCategoryGlAccountToBeUpdated the ProductCategoryGlAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductCategoryGlAccount(ProductCategoryGlAccount productCategoryGlAccountToBeUpdated) {

		UpdateProductCategoryGlAccount com = new UpdateProductCategoryGlAccount(productCategoryGlAccountToBeUpdated);

		int usedTicketId;

		synchronized (ProductCategoryGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryGlAccountUpdated.class,
				event -> sendProductCategoryGlAccountChangedMessage(((ProductCategoryGlAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductCategoryGlAccount from the database
	 * 
	 * @param productCategoryGlAccountId:
	 *            the id of the ProductCategoryGlAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductCategoryGlAccountById(@RequestParam(value = "productCategoryGlAccountId") String productCategoryGlAccountId) {

		DeleteProductCategoryGlAccount com = new DeleteProductCategoryGlAccount(productCategoryGlAccountId);

		int usedTicketId;

		synchronized (ProductCategoryGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryGlAccountDeleted.class,
				event -> sendProductCategoryGlAccountChangedMessage(((ProductCategoryGlAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductCategoryGlAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productCategoryGlAccount/\" plus one of the following: "
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
