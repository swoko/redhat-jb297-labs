package br.net.rafaeltuelho.jb297.unit2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.net.rafaeltuelho.jb297.unit3.model.PolicyHolder;
import br.net.rafaeltuelho.jb297.unit3.model.PolicyHolder_;

@RunWith(Arquillian.class)
public class PolicyHolderEntityTest {

	@Deployment
	public static Archive<?> createDeployment() {
		// You can use war packaging...
		@SuppressWarnings("unused")
		WebArchive war = ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addPackage(PolicyHolder.class.getPackage())
				.addAsManifestResource(
						new File("src/main/resources/META-INF",
								"persistence.xml"))
				.addAsWebInfResource("arquillian-ds.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		// or jar packaging...
		JavaArchive jar = ShrinkWrap
				.create(JavaArchive.class)
				.addPackage(PolicyHolder.class.getPackage())
				.addAsManifestResource(
						new File("src/main/resources/META-INF",
								"persistence.xml"))
				.addAsManifestResource("arquillian-ds.xml")
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

		// choose your packaging here
		return jar;
	}

	private static final String ENTITY_NAME = PolicyHolder.class
			.getSimpleName();

	private static final String[] MOCK_DATA = { "PH", "PH", "PH" };

	@PersistenceContext
	EntityManager em;

	@Inject
	UserTransaction utx;

	@Before
	public void preparePersistenceTest() throws Exception {
		clearData();
		insertData();
		startTransaction();
	}

	private void clearData() throws Exception {
		utx.begin();
		em.joinTransaction();
		System.out.println("Dumping old records...");
		em.createQuery("delete from " + ENTITY_NAME).executeUpdate();
		utx.commit();
	}

	private void insertData() throws Exception {
		utx.begin();
		em.joinTransaction();
		System.out.println("Inserting records...");
		long id = 0L;

		for (String name : MOCK_DATA) {
			PolicyHolder policyHolder = new PolicyHolder(id, name,
					10, new BigDecimal("1000.99"));

			em.persist(policyHolder);
			id++;
		}
		utx.commit();
		// reset the persistence context (cache)
		em.clear();
	}

	private void startTransaction() throws Exception {
		utx.begin();
		em.joinTransaction();
	}

	@After
	public void commitTransaction() throws Exception {
		utx.commit();
	}

	@Test
	public void shouldFindAllUsingJpqlQuery() throws Exception {
		// given
		String fetchingAllInJpql = "select e from " + ENTITY_NAME
				+ " e order by e.id";

		// when
		System.out.println("Selecting (using JPQL)...");
		List<PolicyHolder> entities = em.createQuery(fetchingAllInJpql,
				PolicyHolder.class).getResultList();

		// then
		System.out.println("Found " + entities.size()
				+ " entities (using JPQL):");
		assertContainsAllEntity(entities);
	}

	@Test
	public void shouldFindAllUsingCriteriaApi() throws Exception {
		// given
		CriteriaBuilder builder = em.getCriteriaBuilder();

		CriteriaQuery<PolicyHolder> criteria = builder
				.createQuery(PolicyHolder.class);

		Root<PolicyHolder> entity = criteria.from(PolicyHolder.class);
		criteria.select(entity);
		// TIP: If you don't want to use the JPA 2 Metamodel,
		// you can change the get() method call to get("id")
		criteria.orderBy(builder.asc(entity.get(PolicyHolder_.id)));
		// No WHERE clause, which implies select all

		// when
		System.out.println("Selecting (using Criteria)...");
		List<PolicyHolder> entities = em.createQuery(criteria).getResultList();

		// then
		System.out.println("Found " + entities.size()
				+ " entities (using Criteria):");
		assertContainsAllEntity(entities);
	}

	private static void assertContainsAllEntity(
			Collection<PolicyHolder> retrievedEntities) {
		assertEquals(MOCK_DATA.length, retrievedEntities.size());
		final Set<String> retrievedData = new HashSet<String>();
		
		for (PolicyHolder entity : retrievedEntities) {
			System.out.println("* " + entity);
			retrievedData.add(entity.getName());
		}
		
		assertTrue(retrievedData.size() == MOCK_DATA.length);
		assertTrue(retrievedData.containsAll(Arrays.asList(MOCK_DATA)));
	}

}
