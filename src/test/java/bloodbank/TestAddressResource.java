

/**
 * @author Ehsan
 */

package bloodbank;

import static bloodbank.utility.MyConstants.APPLICATION_API_VERSION;
import static bloodbank.utility.MyConstants.DEFAULT_ADMIN_USER;
import static bloodbank.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;


import bloodbank.entity.Address;
import bloodbank.entity.BloodDonation;

/**
 * 
 * @author Ehsan
 *
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestAddressResource {

	
///	private EntityTransaction et;
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String APPLICATION_CONTEXT_ROOT = "REST-BloodBank";
    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;




    // test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;
    

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic("cst8288", "8288");
    }

    protected WebTarget webTarget;
    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient(
            new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
        webTarget = client.target(uri);
       
    }
    
    @Order(1) 
    @Test 
    public void test_all_address_records_with_adminrole() throws JsonMappingException, JsonProcessingException{
    	Response response = webTarget
    			  .register(adminAuth)
    			  .path("/address")
    			  .request()
    			  .get();
    	  assertThat(response.getStatus(), is(200));
    	  List<Address> address=response.readEntity(new GenericType<List<Address>>(){});
    	  assertThat(address, is(not(empty())));
          int size=address.size();
    	  assertThat(address, hasSize(size));
    			  
    	
    }
    
   @Order(2) 
   @Test
   public void test_get_address_By_ID_by_adminRole() {
	   Response response = webTarget
 			  .register(adminAuth)
 			  .path("/address/1")
 			  .request()
 			  .get();
	   
	   assertThat(response.getStatus(), is(200));
	   
	   assertNotNull(response.getEntity().toString());
	   
   }
   
   @Order(3) 
   @Test
   public void testMediaType() {
	   Response response = webTarget
	 			  .register(adminAuth)
	 			  .path("address/1")
	 			  .request()
	 			  .get();
	   assertThat(response.getStatus(), is(200));
	   assertThat(response.getMediaType().toString(),is(MediaType.APPLICATION_JSON));
   
   
   }
   @Order(4) 
   @Test 
   public void test_address_post() {
	   
//	   Response test = webTarget
// 			  .register(adminAuth)
// 			  .path("/address/1")
// 			  .request()
// 			  .get();
//	   
//	   Address old = test.readEntity(Address.class);
//	   
	   Address ad = new Address();
	   
	   ad.setAddress("23", "Rideau", "Ottawa", "Ontario", "Canada", "K2K0C3");
	   
	 //  ad.setContacts(old.getContacts());
	   
	   //post Address
	   Response responseAddressPost= webTarget
	 			  .register(adminAuth)
	 			  .path("address")
	 			  .request()
	 			  .post(Entity.entity( ad, MediaType.APPLICATION_JSON));
	   
//	   //get address at new id
//	   Response responseAddressGet= webTarget
//	 			  .register(adminAuth)
//	 			  .path("address/"+responseAddressPost.readEntity(Address.class).getId())
//	 			  .request()
//	 			  .get();
	   //read address
	//   Address address= responseAddressGet.readEntity(Address.class);
	   
	   assertEquals(ad.getCity(), "Ottawa");   
	   assertThat(responseAddressPost.getStatus(), is(200));
	   assertNotNull(responseAddressPost.getEntity().toString());
	   
    
	   
   }
   @Order(5) 
   @Test 
   public void test_address_put() {
	   

	 Response responseAddressRecord = webTarget
			  .register(adminAuth)
			  .path("/address/1")
			  .request()
			  .get();
	
	 Address record=responseAddressRecord.readEntity(Address.class);
	  
	  record.setAddress("25", "Klondike", "Ottawa", "Ontario", "USA", "N9A5E7");;
	  	  
	   
	   Response response = webTarget
	 			  .register(adminAuth)
	 			  .path("/address/1")
	 			  .request()
	 			  .put(Entity.entity(record, MediaType.APPLICATION_JSON));
	   
//		 Response newRecord = webTarget
//				  .register(adminAuth)
//				  .path("/address/1")
//				  .request()
//				  .get();
//		
//		// Address r1=newRecord.readEntity(Address.class);
		 
		 
	   assertEquals(record.getCountry(), "USA");
	   assertThat(response.getStatus(), is(200));
	   assertNotNull(response.getEntity().toString());
	   
    
	   
   }
  @Order(9) 
  @Test
  void  test_delete_donationRecord(){
	   Response response = webTarget
	 			  .register(adminAuth)
	 			  .path("/address/2")
	 			  .request()
	 			  .delete();
	   
	    
	   assertThat(response.getStatus(), is(200));

	    
   }
  
  @Order(6)
  @Test
  void  test_Post_constraint_vailoation(){
	  Address newRecord=new Address();
		    
	   Response response = webTarget
	 			  .register(adminAuth)
	 			  .path("/address")
	 			  .request()
	 			  .post(Entity.entity(newRecord, MediaType.APPLICATION_JSON));
	
	    assertThat(response.getStatus(), is(500));
		

	    
  }
  
  @Order(7)
  @Test
  void test_Url_address() {

	assertEquals(uri,  webTarget.getUri());
	
			  
  }
  
  @Order(8)
  @Test
  void test_findIdNotFound() {
	  
	   Response response = webTarget
	 			  .register(adminAuth)
	 			  .path("/address/100")
	 			  .request()
	 			  .get();
	   
	   assertThat(response.getStatus(), is(404));
	   
	   assertNotEquals(response.readEntity(BloodDonation.class).getId(),100);
	  
  }
  
  
  
   
    

}
