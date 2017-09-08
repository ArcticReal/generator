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
import com.skytala.eCommerce.command.AddProductPriceRule;
import com.skytala.eCommerce.command.DeleteProductPriceRule;
import com.skytala.eCommerce.command.UpdateProductPriceRule;
import com.skytala.eCommerce.entity.ProductPriceRule;
import com.skytala.eCommerce.entity.ProductPriceRuleMapper;
import com.skytala.eCommerce.event.ProductPriceRuleAdded;
import com.skytala.eCommerce.event.ProductPriceRuleDeleted;
import com.skytala.eCommerce.event.ProductPriceRuleFound;
import com.skytala.eCommerce.event.ProductPriceRuleUpdated;
import com.skytala.eCommerce.query.FindProductPriceRulesBy;

@RestController
@RequestMapping("/api/productPriceRule")
public class ProductPriceRuleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPriceRule>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPriceRuleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPriceRule
	 * @return a List with the ProductPriceRules
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPriceRule> findProductPriceRulesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPriceRulesBy query = new FindProductPriceRulesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPriceRuleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceRuleFound.class,
				event -> sendProductPriceRulesFoundMessage(((ProductPriceRuleFound) event).getProductPriceRules(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPriceRulesFoundMessage(List<ProductPriceRule> productPriceRules, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPriceRules);
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
	public boolean createProductPriceRule(HttpServletRequest request) {

		ProductPriceRule productPriceRuleToBeAdded = new ProductPriceRule();
		try {
			productPriceRuleToBeAdded = ProductPriceRuleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPriceRule(productPriceRuleToBeAdded);

	}

	/**
	 * creates a new ProductPriceRule entry in the ofbiz database
	 * 
	 * @param productPriceRuleToBeAdded
	 *            the ProductPriceRule thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPriceRule(ProductPriceRule productPriceRuleToBeAdded) {

		AddProductPriceRule com = new AddProductPriceRule(productPriceRuleToBeAdded);
		int usedTicketId;

		synchronized (ProductPriceRuleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceRuleAdded.class,
				event -> sendProductPriceRuleChangedMessage(((ProductPriceRuleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPriceRule(HttpServletRequest request) {

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

		ProductPriceRule productPriceRuleToBeUpdated = new ProductPriceRule();

		try {
			productPriceRuleToBeUpdated = ProductPriceRuleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPriceRule(productPriceRuleToBeUpdated);

	}

	/**
	 * Updates the ProductPriceRule with the specific Id
	 * 
	 * @param productPriceRuleToBeUpdated the ProductPriceRule thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPriceRule(ProductPriceRule productPriceRuleToBeUpdated) {

		UpdateProductPriceRule com = new UpdateProductPriceRule(productPriceRuleToBeUpdated);

		int usedTicketId;

		synchronized (ProductPriceRuleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceRuleUpdated.class,
				event -> sendProductPriceRuleChangedMessage(((ProductPriceRuleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPriceRule from the database
	 * 
	 * @param productPriceRuleId:
	 *            the id of the ProductPriceRule thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPriceRuleById(@RequestParam(value = "productPriceRuleId") String productPriceRuleId) {

		DeleteProductPriceRule com = new DeleteProductPriceRule(productPriceRuleId);

		int usedTicketId;

		synchronized (ProductPriceRuleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceRuleDeleted.class,
				event -> sendProductPriceRuleChangedMessage(((ProductPriceRuleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPriceRuleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPriceRule/\" plus one of the following: "
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
