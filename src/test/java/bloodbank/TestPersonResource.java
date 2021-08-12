/**
 * File: OrderSystemTestSuite.java
 * Course materials (21S) CST 8277
 * Teddy Yap
 * (Original Author) Mike Norman
 *
 * @date 2020 10
 *
 * (Modified) @author Student Name
 */
package bloodbank;

import static bloodbank.utility.MyConstants.APPLICATION_API_VERSION;
import static bloodbank.utility.MyConstants.DEFAULT_ADMIN_USER;
import static bloodbank.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static bloodbank.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static bloodbank.utility.MyConstants.DEFAULT_USER_PREFIX;
import static bloodbank.utility.MyConstants.PERSON_RESOURCE_NAME;
import static bloodbank.utility.MyConstants.BLOOD_DONATION_RESOURCE_NAME;
import static bloodbank.utility.MyConstants.BLOODBANK_RESOURCE_NAME;
import static bloodbank.utility.MyConstants.PHONE_RECORD_RESOURCE_NAME;
import static bloodbank.utility.MyConstants.CUSTOMER_ADDRESS_SUBRESOURCE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.time.Instant;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import bloodbank.entity.Address;
import bloodbank.entity.BloodBank;
import bloodbank.entity.BloodDonation;
import bloodbank.entity.Person;
import bloodbank.entity.Phone;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestPersonResource{
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String APPLICATION_CONTEXT_ROOT = "REST-BloodBank-Skeleton";
    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;

    // test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature defaultUserAuth;
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
        //defaultUserAuth = HttpAuthenticationFeature.basic(DEFAULT_USER_PREFIX, DEFAULT_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic("cst8288", "8288");
    }

    protected WebTarget webTarget;
	
	@BeforeEach
	public void setUp() { 
		Client client = ClientBuilder.newClient(
				new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature())); 
		webTarget = client.target(uri); }
	 

    @Test
    public void test01_all_persons_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            //.register(userAuth)
            .register(adminAuth)
            .path(PERSON_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Person> persons = response.readEntity(new GenericType<List<Person>>(){});
        assertThat(persons, is(not(empty())));
        assertThat(persons, hasSize(1));
    }
    
    @Test
    public void test02_all_persons_with_userrole() throws JsonMappingException, JsonProcessingException {
    	
        Response response = webTarget
            .register(userAuth)
            .path(PERSON_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(403));
    }
    
	  @Test 
	  public void test03_get_one_person_with_adminrole() throws JsonMappingException, JsonProcessingException { 
		  Response response = webTarget
				  				//.register(userAuth) 
				  				.register(adminAuth) 
				  				.path(PERSON_RESOURCE_NAME + "/1") 
				  				.request() 
				  				.get(); 
		  assertThat(response.getStatus(),is(200)); 
		  Person person = response.readEntity(Person.class);
		  assertNotNull(person);
		  assertEquals(person.getId(), 1);
		  assertEquals(person.getFirstName(), "Teddy");
		  assertEquals(person.getLastName(), "Yap");
		  assertEquals(person.getCreated(), Instant.ofEpochMilli(Long.valueOf("1617265125540")));
		  assertEquals(person.getUpdated(), Instant.ofEpochMilli(Long.valueOf("1617265125540")));
		  
		 
	}
	  
	  @Test 
	  public void test04_get_one_person_with_userrole() throws JsonMappingException, JsonProcessingException {
		  Response response = webTarget
				  				.register(userAuth) 
				  				.path(PERSON_RESOURCE_NAME + "/1") 
				  				.request() 
				  				.get(); 
		  assertThat(response.getStatus(),is(200)); 
		  Person person = response.readEntity(Person.class);
		  assertNotNull(person);
		  assertEquals(person.getId(), 1);
		  assertEquals(person.getFirstName(), "Teddy");
		  assertEquals(person.getLastName(), "Yap");
		  assertEquals(person.getCreated(), Instant.ofEpochMilli(Long.valueOf("1617265125540")));
		  assertEquals(person.getUpdated(), Instant.ofEpochMilli(Long.valueOf("1617265125540")));
		  
		  Response response2 = webTarget
	  				.register(userAuth) 
	  				.path(PERSON_RESOURCE_NAME + "/2") 
	  				.request() 
	  				.get(); 
		  assertThat(response2.getStatus(),is(403)); 
		  
		  
	  }	
	  
	    
	    
	    @Test
	    public void test09_all_blood_banks_with_adminrole() throws JsonMappingException, JsonProcessingException {
	    	
	        Response response = webTarget
	            //.register(userAuth)
	            .register(adminAuth)
	            .path(BLOODBANK_RESOURCE_NAME)
	            .request()
	            .get();
	        assertThat(response.getStatus(), is(200));
	        List<BloodBank> bloodBanks = response.readEntity(new GenericType<List<BloodBank>>(){});
	        assertThat(bloodBanks, is(not(empty())));
	        assertThat(bloodBanks, hasSize(2));
	    }
	    
	    @Test
	    public void test10_all_blood_banks_with_userrole() throws JsonMappingException, JsonProcessingException {
	    	
	        Response response = webTarget
	            //.register(userAuth)
	            .register(userAuth)
	            .path(BLOODBANK_RESOURCE_NAME)
	            .request()
	            .get();
	        assertThat(response.getStatus(), is(200));
	        List<BloodBank> bloodBanks = response.readEntity(new GenericType<List<BloodBank>>(){});
	        assertThat(bloodBanks, is(not(empty())));
	        assertThat(bloodBanks, hasSize(2));
	    }
	    
	    @Test
	    public void test11_all_phones_with_adminrole() throws JsonMappingException, JsonProcessingException {
	    	
	        Response response = webTarget
	            //.register(userAuth)
	            .register(adminAuth)
	            .path(PHONE_RECORD_RESOURCE_NAME)
	            .request()
	            .get();
	        assertThat(response.getStatus(), is(200));
	        List<Phone> phones = response.readEntity(new GenericType<List<Phone>>(){});
	        assertThat(phones, is(not(empty())));
	        assertThat(phones, hasSize(2));
	    }
	    
	    @Test
	    public void test12_all_phones_with_userrole() throws JsonMappingException, JsonProcessingException {
	    	
	        Response response = webTarget
	            //.register(userAuth)
	            .register(userAuth)
	            .path(PHONE_RECORD_RESOURCE_NAME)
	            .request()
	            .get();
	        assertThat(response.getStatus(), is(403));
	    
	    }
	    
	    @Test
	    public void test13_all_addresses_with_adminrole() throws JsonMappingException, JsonProcessingException {
	    	
	        Response response = webTarget
	            //.register(userAuth)
	            .register(adminAuth)
	            .path(CUSTOMER_ADDRESS_SUBRESOURCE_NAME)
	            .request()
	            .get();
	        assertThat(response.getStatus(), is(200));
	        List<Address> addresses = response.readEntity(new GenericType<List<Address>>(){});
	        assertThat(addresses, is(not(empty())));
	        assertThat(addresses, hasSize(1));
	    }
	    
	    @Test
	    public void test14_all_addresses_with_userrole() throws JsonMappingException, JsonProcessingException {
	    	
	        Response response = webTarget
	            //.register(userAuth)
	            .register(userAuth)
	            .path(CUSTOMER_ADDRESS_SUBRESOURCE_NAME)
	            .request()
	            .get();
	        assertThat(response.getStatus(), is(403));
	    
	    }
	    
	    @Test 
		  public void test15_get_one_blood_bank_with_adminrole() throws JsonMappingException, JsonProcessingException { 
			  Response response = webTarget
					  				//.register(userAuth) 
					  				.register(adminAuth) 
					  				.path(BLOODBANK_RESOURCE_NAME + "/1") 
					  				.request() 
					  				.get(); 
			  assertThat(response.getStatus(),is(200)); 
			  BloodBank bloodBank = response.readEntity(BloodBank.class);
			  assertEquals(bloodBank.getId(), 1);
			  assertEquals(bloodBank.getName(), "Bloody Bank");

			 
		}
		  
		  @Test 
		  public void test16_get_one_blood_bank_with_userrole() throws JsonMappingException, JsonProcessingException {
			  Response response = webTarget
					  				.register(userAuth) 
					  				.path(BLOODBANK_RESOURCE_NAME + "/1") 
					  				.request() 
					  				.get(); 
			  assertThat(response.getStatus(),is(200)); 


			  Response response2 = webTarget
		  				.register(userAuth) 
		  				.path(BLOODBANK_RESOURCE_NAME + "/2") 
		  				.request() 
		  				.get(); 
			  assertThat(response2.getStatus(),is(200)); 
			  
			  
		  }	
	    @Test
	    public void test_add_person_with_adminrole() throws JsonMappingException, JsonProcessingException {
	    	
	    Person newPerson = new Person();
	    //newPerson.setId(2);
	    newPerson.setFirstName("test");
	    newPerson.setLastName("test");
	    newPerson.setCreatedEpochMilli(Long.valueOf("1617265125540"));
	    newPerson.setUpdatedEpochMilli(Long.valueOf("1617265125540"));
	    newPerson.setVersion(0);
	   
	    //Response response = Response.ok(newPerson).build();
	    //JSONObject jo = new JSONObject(newPerson);
		
		  Response response = webTarget
				  .register(adminAuth)
				  .path(PERSON_RESOURCE_NAME)
				  .request(MediaType.APPLICATION_JSON) 
				  .post(Entity.entity(newPerson,MediaType.APPLICATION_JSON));
		
		//Able to persist new person into database but still receive 500 error
	    System.out.println(response.getStatusInfo());

	    assertEquals( 200, response.getStatus());
	    }
	    
	    @Test
	    public void test_add_person_with_userrole() throws JsonMappingException, JsonProcessingException {
	    	
	    Person newPerson = new Person();
	    //newPerson.setId(2);
	    newPerson.setFirstName("test");
	    newPerson.setLastName("test");
	    newPerson.setCreatedEpochMilli(Long.valueOf("1617265125540"));
	    newPerson.setUpdatedEpochMilli(Long.valueOf("1617265125540"));
	    newPerson.setVersion(0);
		
		  Response response = webTarget
				  .register(userAuth)
				  .path(PERSON_RESOURCE_NAME)
				  .request(MediaType.APPLICATION_JSON) 
				  .post(Entity.entity(newPerson,MediaType.APPLICATION_JSON));

	    assertEquals( 403, response.getStatus());
	    }
}
