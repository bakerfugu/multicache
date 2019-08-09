package com.tvg.cachetests;

import com.tvg.cachetests.controller.ContactController;
import com.tvg.cachetests.exception.ContactNotFoundException;
import com.tvg.cachetests.model.Contact;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.tvg.cachetests.repository.ContactRepository;
import com.tvg.cachetests.service.ContactService;
import org.springframework.util.StopWatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = "standard")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MulticacheTest {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@LocalServerPort
	private int port;

	@Autowired
	private ContactController contactController;

	@Autowired
	private ContactService contactService;

	@MockBean
	private ContactRepository contactRepository;

	private Contact alex = new Contact( 2000L, "alex", "alex@mail.com", "123 456");
	private Contact notAlex = new Contact( 2000L, "not-alex", "not-alex@mail.com", "654 321");

	@Before
	public void setUp() {
		log.info("Setting up tests!");

		contactService.clearCache();
		Mockito.when(contactRepository.existsById(eq(alex.getId())))
				.thenReturn(true);
		Mockito.when(contactRepository.findById(eq(alex.getId())))
				.thenReturn(java.util.Optional.of(alex));
	}

	@Test
	public void whenValidName_thenContactShouldBeFound() throws Exception {
		log.info("Running test 1");

		Contact found = contactService.findById(alex.getId());

		assertThat(found.getId())
				.isEqualTo(alex.getId());
		assertThat(found.getName())
				.isEqualTo(alex.getName());

	}

	@Test
	public void whenContactInRepoChanged_thenCachedContactShouldBeUnchanged() throws Exception {
		log.info("Running test 2");

		Contact found = contactService.findById(alex.getId());

		Mockito.when(contactRepository.findById(eq(notAlex.getId())))
				.thenReturn(java.util.Optional.empty());

		Contact nextFound = contactService.findById(notAlex.getId());

		assertThat(nextFound.getId())
				.isEqualTo(alex.getId());
		assertThat(nextFound.getName())
				.isEqualTo(alex.getName());
	}

	@Test
	public void whenContactDeletedFromCache_thenNotAlexIsFound() throws Exception {
		//ensures cache is properly cleared
		log.info("Running test 3");

		Contact shouldBeAlex = contactService.findById(alex.getId());

		Mockito.when(contactRepository.existsById(eq(notAlex.getId())))
				.thenReturn(true);
		Mockito.when(contactRepository.findById(eq(notAlex.getId())))
				.thenReturn(java.util.Optional.of(notAlex));

		//deletes alex from cache, but does not delete notAlex because repo.delete is not mocked
		contactService.delete(alex.getId());

		Contact shouldBeNotAlex = contactService.findById(notAlex.getId());

		assertThat(shouldBeAlex.getName())
				.isEqualTo(alex.getName());

		assertThat(shouldBeNotAlex.getId())
				.isEqualTo(notAlex.getId());
		assertThat(shouldBeNotAlex.getName())
				.isEqualTo(notAlex.getName());
	}

	@Test
	public void firstCallSlow_ThenFollowingCallsAreFastBecauseCache() throws Exception {
		//well, it kinda makes
		StopWatch watch = new StopWatch();
		watch.start();
		Contact shouldBeAlex = contactService.findById(alex.getId());
		watch.stop();
		log.info(String.format("StopWatch: %s took %d millis ", "First call to get alex", watch.getTotalTimeMillis()));
		watch.start();
		Contact nextAlex = contactService.findById(alex.getId());
		watch.stop();
		log.info(String.format("StopWatch: %s took %d millis ", "Second call (cached) to get alex", watch.getTotalTimeMillis()));

		watch.start();
		Contact shouldBeAlexFriend = contactService.findFriendById(alex.getId());
		watch.stop();
		log.info(String.format("StopWatch: %s took %d millis ", "First friend call to get alex", watch.getTotalTimeMillis()));
		watch.start();
		Contact nextAlexFriend = contactService.findFriendById(alex.getId());
		watch.stop();
		log.info(String.format("StopWatch: %s took %d millis ", "Second friend call (cached) to get alex", watch.getTotalTimeMillis()));
	}

}
