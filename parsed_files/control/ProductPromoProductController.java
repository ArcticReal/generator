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
import com.skytala.eCommerce.command.AddProductPromoProduct;
import com.skytala.eCommerce.command.DeleteProductPromoProduct;
import com.skytala.eCommerce.command.UpdateProductPromoProduct;
import com.skytala.eCommerce.entity.ProductPromoProduct;
import com.skytala.eCommerce.entity.ProductPromoProductMapper;
import com.skytala.eCommerce.event.ProductPromoProductAdded;
import com.skytala.eCommerce.event.ProductPromoProductDeleted;
import com.skytala.eCommerce.event.ProductPromoProductFound;
import com.skytala.eCommerce.event.ProductPromoProductUpdated;
import com.skytala.eCommerce.query.FindProductPromoProductsBy;

@RestController
@RequestMapping("/api/productPromoProduct")
public class ProductPromoProductController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPromoProduct>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPromoProductController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPromoProduct
	 * @return a List with the ProductPromoProducts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPromoProduct> findProductPromoProductsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPromoProductsBy query = new FindProductPromoProductsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPromoProductController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoProductFound.class,
				event -> sendProductPromoProductsFoundMessage(((ProductPromoProductFound) event).getProductPromoProducts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPromoProductsFoundMessage(List<ProductPromoProduct> productPromoProducts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPromoProducts);
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
	public boolean createProductPromoProduct(HttpServletRequest request) {

		ProductPromoProduct productPromoProductToBeAdded = new ProductPromoProduct();
		try {
			productPromoProductToBeAdded = ProductPromoProductMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPromoProduct(productPromoProductToBeAdded);

	}

	/**
	 * creates a new ProductPromoProduct entry in the ofbiz database
	 * 
	 * @param productPromoProductToBeAdded
	 *            the ProductPromoProduct thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPromoProduct(ProductPromoProduct productPromoProductToBeAdded) {

		AddProductPromoProduct com = new AddProductPromoProduct(productPromoProductToBeAdded);
		int usedTicketId;

		synchronized (ProductPromoProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoProductAdded.class,
				event -> sendProductPromoProductChangedMessage(((ProductPromoProductAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPromoProduct(HttpServletRequest request) {

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

		ProductPromoProduct productPromoProductToBeUpdated = new ProductPromoProduct();

		try {
			productPromoProductToBeUpdated = ProductPromoProductMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPromoProduct(productPromoProductToBeUpdated);

	}

	/**
	 * Updates the ProductPromoProduct with the specific Id
	 * 
	 * @param productPromoProductToBeUpdated the ProductPromoProduct thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPromoProduct(ProductPromoProduct productPromoProductToBeUpdated) {

		UpdateProductPromoProduct com = new UpdateProductPromoProduct(productPromoProductToBeUpdated);

		int usedTicketId;

		synchronized (ProductPromoProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoProductUpdated.class,
				event -> sendProductPromoProductChangedMessage(((ProductPromoProductUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPromoProduct from the database
	 * 
	 * @param productPromoProductId:
	 *            the id of the ProductPromoProduct thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPromoProductById(@RequestParam(value = "productPromoProductId") String productPromoProductId) {

		DeleteProductPromoProduct com = new DeleteProductPromoProduct(productPromoProductId);

		int usedTicketId;

		synchronized (ProductPromoProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoProductDeleted.class,
				event -> sendProductPromoProductChangedMessage(((ProductPromoProductDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPromoProductChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPromoProduct/\" plus one of the following: "
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
