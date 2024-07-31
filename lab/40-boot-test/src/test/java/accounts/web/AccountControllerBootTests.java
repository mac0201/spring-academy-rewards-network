package accounts.web;

import accounts.AccountManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.money.Percentage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import rewards.internal.account.Account;
import rewards.internal.account.Beneficiary;

// -06: Get yourself familiarized with various testing utility classes
// - Uncomment the import statements below
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// -07: Replace @ExtendWith(SpringExtension.class) with the following annotation
// - @WebMvcTest(AccountController.class) // includes @ExtendWith(SpringExtension.class)
@WebMvcTest(AccountController.class)
public class AccountControllerBootTests {

	// -08: Autowire MockMvc bean
	@Autowired
	private MockMvc mockMvc;

	// -09: Create AccountManager mock bean using @MockBean annotation
	@MockBean
	private AccountManager accountManager;

	// -10: Write positive test for GET request for an account
	// - Uncomment the code and run the test and verify it succeeds
	@Test
	public void accountDetails() throws Exception {

		given(accountManager.getAccount(0L))
				.willReturn(new Account("1234567890", "John Doe"));

		mockMvc.perform(get("/accounts/0"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(jsonPath("name").value("John Doe"))
			   .andExpect(jsonPath("number").value("1234567890"));

		verify(accountManager).getAccount(0L);

	}

	// -11: Write negative test for GET request for a non-existent account
	// - Uncomment the "given" and "verify" statements
	// - Write code between the "given" and "verify" statements
	// - Run the test and verify it succeeds
	@Test
	public void accountDetailsFail() throws Exception {

		given(accountManager.getAccount(any(Long.class)))
				.willThrow(new IllegalArgumentException("No such account with id " + 0L));

		// (Write code here)
		// - Use mockMvc to perform HTTP Get operation using "/accounts/9999"
        //   as a non-existent account URL
		// - Verify that the HTTP response status is 404

		mockMvc.perform(get("/accounts/9999"))
						.andExpect(status().isNotFound());

		verify(accountManager).getAccount(any(Long.class));

	}

    // -12: Write test for `POST` request for an account
	// - Uncomment Java code below
	// - Write code between the "given" and "verify" statements
	// - Run the test and verify it succeeds
	@Test
	public void createAccount() throws Exception {

		Account testAccount = new Account("1234512345", "Mary Jones");
		testAccount.setEntityId(21L);

		given(accountManager.save(any(Account.class)))
				.willReturn(testAccount);

		// (Write code here)
		// Use mockMvc to perform HTTP Post operation to "/accounts"
		// - Set the request content type to APPLICATION_JSON
		// - Set the request content with Json string of the "testAccount"
		//   (Use "asJsonString" method below to convert the "testAccount"
		//   object into Json string)
		// - Verify that the response status is 201
		// - Verify that the response "Location" header contains "http://localhost/accounts/21"

		mockMvc.perform(post("/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(testAccount))
				)
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/accounts/21"));

		verify(accountManager).save(any(Account.class));

	}

    // Utility class for converting an object into JSON string
	protected static String asJsonString(final Object obj) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final String jsonContent = mapper.writeValueAsString(obj);
			return jsonContent;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// -13 (Optional): Experiment with @MockBean vs @Mock
	// - Change `@MockBean` to `@Mock` for the `AccountManager dependency above
	// - Run the test and observe a test failure
	// - Change it back to `@MockBean`

	@Test
	public void getAllAccounts() throws Exception {

		given(accountManager.getAllAccounts())
				.willReturn(List.of(
						getMockAccount("12355", "John Doe"),
						getMockAccount("12366", "Kate Joss"),
						getMockAccount("12377", "Jack Kola")
				));

		mockMvc.perform(get("/accounts"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[0].name").exists())
				.andExpect(jsonPath("$[0].name").value("John Doe"))
				.andExpect(jsonPath("$[0].number").value("12355"));

		verify(accountManager).getAllAccounts();
	}

	@Test
	void getValidBeneficiary() throws Exception {

		var account = getMockAccount("12345", "John Doe", "Adam");

		given(accountManager.getAccount(0L)).willReturn(account);

		mockMvc.perform(get("/accounts/{accountId}/beneficiaries/{beneficiary}", 0L, "Adam"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("name").value("Adam"))
				.andExpect(jsonPath("allocationPercentage").value(1.00));

		verify(accountManager).getAccount(any(Long.class));

	}

	@Test
	void getInvalidBeneficiary() throws Exception {
		mockMvc.perform(get("/accounts/{accountId}/beneficiaries/{beneficiary}", 0L, "Adam"))
				.andExpect(status().isNotFound());

		verify(accountManager).getAccount(any(Long.class));
	}

	@Test
	void addBeneficiary() throws Exception {
		mockMvc.perform(post("/accounts/{accountId}/beneficiaries", 0L)
				.content("Adam"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/accounts/0/beneficiaries/Adam"));
	}

	@Test
	void removeBeneficiary() throws Exception {
		given(accountManager.getAccount(0L))
				.willReturn(getMockAccount("12345", "John Doe", "Adam"));
		mockMvc.perform(delete("/accounts/{accountId}/beneficiaries/{beneficiary}", 0L, "Adam"))
				.andExpect(status().isNoContent());
		verify(accountManager).getAccount(anyLong());
	}

	@Test
	void removeNonExistentBeneficiary() throws Exception {
		given(accountManager.getAccount(0L))
				.willReturn(getMockAccount("12345", "John Doe", "Adam", "Bob"));

		mockMvc.perform(
					delete("/accounts/{accountId}/beneficiaries/{beneficiary}", 0L, "Charlie"))
				.andExpect(status().isNotFound());
	}

	/**
	* Utility method to create Account objects with or without beneficiaries
	* */
	private Account getMockAccount(String number, String name, String... beneficiaries) {
		var account = new Account(number, name);
		for (String b : beneficiaries) {
			account.addBeneficiary(b);
		}
		return account;
	}

}
