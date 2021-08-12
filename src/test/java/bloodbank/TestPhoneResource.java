
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


import bloodbank.entity.Phone;
/**
 * 
 * @author Ehsan
 *
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestPhoneResource {

	
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
    public void test_all_phone_records_with_adminrole() throws JsonMappingException, JsonProcessingException{
    	Response response = webTarget
    			  .register(adminAuth)
    			  .path("/phone")
    			  .request()
    			  .get();
    	  assertThat(response.getStatus(), is(200));
    	  List<Phone> phone=response.readEntity(new GenericType<List<Phone>>(){});
    	  assertThat(phone, is(not(empty())));
          int size=phone.size();
    	  assertThat(phone, hasSize(size));
    			  
    	
    }
    
   @Order(2) 
   @Test
   public void test_get_phone_By_ID_by_adminRole() {
	   Response response = webTarget
 			  .register(adminAuth)
 			  .path("/phone/1")
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
	 			  .path("/phone/1")
	 			  .request()
	 			  .get();
	   assertThat(response.getStatus(), is(200));
	   assertThat(response.getMediaType().toString(),is(MediaType.APPLICATION_JSON));
   
   
   }
   @Order(4) 
   @Test 
   public void test_phone_post() {
	   
//	   Response test = webTarget
// 			  .register(adminAuth)
// 			  .path("/phone/1")
// 			  .request()
// 			  .get();
//	   
	  // Phone pOld = test.readEntity(Phone.class);
	   
	   Phone phone = new Phone();
	   
	   
	   phone.setNumber("+99", "22", "54812365");
	   
	 //  phone.setContacts(pOld.getContacts());
	   
	   //post Phone
	   Response responsePhonePost= webTarget
	 			  .register(adminAuth)
	 			  .path("/phone")
	 			  .request()
	 			  .post(Entity.entity( phone, MediaType.APPLICATION_JSON));
//	   
//	   //get address at new id
//	   Response responsePhoneGet= webTarget
//	 			  .register(adminAuth)
//	 			  .path("/phone/"+responsePhonePost.readEntity(Phone.class).getId())
//	 			  .request()
//	 			  .get();
//	   //read address
   
//	   
	   assertEquals(phone.getAreaCode(), "22");   
	   assertThat(responsePhonePost.getStatus(), is(200));
	   assertNotNull(responsePhonePost.getEntity().toString());
	   
    
	   
   }
   @Order(5) 
   @Test 
   public void test_phone_put() {
	   
	 Response responsePhoneRecord = webTarget
			  .register(adminAuth)
			  .path("/phone/1")
			  .request()
			  .get();
	
	 Phone record=responsePhoneRecord.readEntity(Phone.class);
	  
	 record.setNumber("+99", "69", "54812365");

	   
	   Response response = webTarget
	 			  .register(adminAuth)
	 			  .path("phone/1")
	 			  .request()
	 			  .put(Entity.entity(record, MediaType.APPLICATION_JSON));
	   
		 Response newRecord = webTarget
				  .register(adminAuth)
				  .path("/phone/1")
				  .request()
				  .get();
		
		 Phone r1=newRecord.readEntity(Phone.class);
		 
		 
		 assertEquals(r1.getAreaCode(), "69");
	   assertThat(response.getStatus(), is(200));
	   assertNotNull(response.getEntity().toString());
	   
    
	   
   }
  @Order(9) 
  @Test
  void  test_delete_Phone(){
	   Response response = webTarget
	 			  .register(adminAuth)
	 			  .path("phone/3")
	 			  .request()
	 			  .delete();
	  
	   assertThat(response.getStatus(), is(200));

	    
   }
  
//  @Order(6)
//  @Test
//  void  test_Post_constraint_vailoation(){
//	  Phone	newRecord=new Phone();
//	
//	 
//	
//		    
//	   Response response = webTarget
//	 			  .register(adminAuth)
//	 			  .path("phone")
//	 			  .request()
//	 			  .post(Entity.entity(newRecord, MediaType.APPLICATION_JSON));
//	    logger.debug("inside test_Post_constraint_vailoation(): {}: ",response.getStatusInfo());
//	    assertThat(response.getStatus(), is(500));
//		
//
//	    
//  }
  
  @Order(7)
  @Test
  void test_Url_phone() {

	assertEquals(uri,  webTarget.getUri());
	
			  
  }
  
  @Order(8)
  @Test
  void test_findIdNotFound() {
	  
	   Response response = webTarget
	 			  .register(adminAuth)
	 			  .path("/phone/100")
	 			  .request()
	 			  .get();
	   
	   assertThat(response.getStatus(), is(404));
	   
	   assertNotEquals(response.readEntity(Phone.class).getId(),100);
	  
  }
  
  
  
   
    

}
