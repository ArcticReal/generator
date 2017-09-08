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
import com.skytala.eCommerce.command.AddProductMaint;
import com.skytala.eCommerce.command.DeleteProductMaint;
import com.skytala.eCommerce.command.UpdateProductMaint;
import com.skytala.eCommerce.entity.ProductMaint;
import com.skytala.eCommerce.entity.ProductMaintMapper;
import com.skytala.eCommerce.event.ProductMaintAdded;
import com.skytala.eCommerce.event.ProductMaintDeleted;
import com.skytala.eCommerce.event.ProductMaintFound;
import com.skytala.eCommerce.event.ProductMaintUpdated;
import com.skytala.eCommerce.query.FindProductMaintsBy;

@RestController
@RequestMapping("/api/productMaint")
public class ProductMaintController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductMaint>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductMaintController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductMaint
	 * @return a List with the ProductMaints
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductMaint> findProductMaintsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductMaintsBy query = new FindProductMaintsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductMaintController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMaintFound.class,
				event -> sendProductMaintsFoundMessage(((ProductMaintFound) event).getProductMaints(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductMaintsFoundMessage(List<ProductMaint> productMaints, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productMaints);
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
	public boolean createProductMaint(HttpServletRequest request) {

		ProductMaint productMaintToBeAdded = new ProductMaint();
		try {
			productMaintToBeAdded = ProductMaintMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductMaint(productMaintToBeAdded);

	}

	/**
	 * creates a new ProductMaint entry in the ofbiz database
	 * 
	 * @param productMaintToBeAdded
	 *            the ProductMaint thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductMaint(ProductMaint productMaintToBeAdded) {

		AddProductMaint com = new AddProductMaint(productMaintToBeAdded);
		int usedTicketId;

		synchronized (ProductMaintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMaintAdded.class,
				event -> sendProductMaintChangedMessage(((ProductMaintAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductMaint(HttpServletRequest request) {

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

		ProductMaint productMaintToBeUpdated = new ProductMaint();

		try {
			productMaintToBeUpdated = ProductMaintMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductMaint(productMaintToBeUpdated);

	}

	/**
	 * Updates the ProductMaint with the specific Id
	 * 
	 * @param productMaintToBeUpdated the ProductMaint thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductMaint(ProductMaint productMaintToBeUpdated) {

		UpdateProductMaint com = new UpdateProductMaint(productMaintToBeUpdated);

		int usedTicketId;

		synchronized (ProductMaintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMaintUpdated.class,
				event -> sendProductMaintChangedMessage(((ProductMaintUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductMaint from the database
	 * 
	 * @param productMaintId:
	 *            the id of the ProductMaint thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductMaintById(@RequestParam(value = "productMaintId") String productMaintId) {

		DeleteProductMaint com = new DeleteProductMaint(productMaintId);

		int usedTicketId;

		synchronized (ProductMaintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMaintDeleted.class,
				event -> sendProductMaintChangedMessage(((ProductMaintDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductMaintChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productMaint/\" plus one of the following: "
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
