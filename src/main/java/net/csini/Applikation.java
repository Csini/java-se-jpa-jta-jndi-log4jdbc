package net.csini;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.csini.entity.Land;
import net.sf.log4jdbc.Log4jdbcProxyDataSource;

/**
 * 
 * @author csini
 */
public class Applikation {
	private static final String PAK_PERSISTENCE_UNIT = "pakPersistence";
	private static final String JNDI = "java:jboss/datasources/PakDB";

	private final static Logger LOG = Logger.getLogger(Applikation.class);

	public static void main(String[] args) throws Exception {

		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
		System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

		System.setProperty("file.encoding", "UTF-8");

		final BasicDataSource ds = new BasicDataSource();
		ds.setUrl("jdbc:h2:mem:pak;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=DB2;create=true");
		ds.setUsername("sa");
		ds.setDriverClassName("org.h2.jdbcx.JdbcDataSource");
		ds.setPassword("");

		final Context context = new InitialContext();
		try {

			context.createSubcontext("java:");
			context.createSubcontext("java:jboss");
			context.createSubcontext("java:jboss/datasources");
			context.bind(JNDI, new Log4jdbcProxyDataSource(ds));

			LOG.info("BINDED------------");

			LOG.info(context);


			Map<String, String> persistenceMap = new HashMap<String, String>();

			persistenceMap.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");

			final EntityManagerFactory emf = Persistence.createEntityManagerFactory(PAK_PERSISTENCE_UNIT,
					persistenceMap);

			processExcel(emf);

		} finally {
			context.close();
		}

	}

	private static void bind() throws NamingException {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
		System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

		final BasicDataSource ds = new BasicDataSource();
		ds.setUrl(
				"jdbc:h2:mem:pak;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=DB2;create=true?characterEncoding=UTF-8 ");
		ds.setUsername("sa");
		ds.setDriverClassName("org.h2.jdbcx.JdbcDataSource");
		ds.setPassword("");

		final Context context = new InitialContext();
		try {
			context.createSubcontext("java:");
			context.createSubcontext("java:jboss");
			context.createSubcontext("java:jboss/datasources/PakDB");
			context.bind(JNDI, ds);
		} finally {
			context.close();
		}
	}

	private static void lookup() throws NamingException, SQLException {
		final Context context = new InitialContext();
		try {
			final DataSource ds = (DataSource) context.lookup(JNDI);
			try (final Connection cn = ds.getConnection();
					final Statement st = cn.createStatement();
					final ResultSet rs = st.executeQuery("SELECT CURRENT_TIMESTAMP FROM SYSIBM.SYSDUMMY1")) {
				while (rs.next()) {
					System.out.println(rs.getTimestamp(1));
				}
			}
		} finally {
			context.close();
		}
	}

	private static void populate(final EntityManagerFactory emf)
			throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException,
			HeuristicMixedException, HeuristicRollbackException {
		final EntityManager em = emf.createEntityManager();
		try {

			BitronixTransactionManager btm = TransactionManagerServices.getTransactionManager();

			btm.begin();

			em.joinTransaction();

			final Land emp = new Land();
			emp.setCode("XX");
			emp.setLabel("ZootropiaÃ¤Ã¤Ã¤Ã¤Ã¤Ã¶Ã¶Ã¶Ã¶Ã¶Ã¶");
			em.persist(emp);

			btm.commit();

		} finally {
			em.close();
		}
	}

	private static void query(final EntityManagerFactory emf) {
		final EntityManager em = emf.createEntityManager();
		try {
			LOG.info(em.find(Land.class, "AT").getLabel());
			LOG.info(em.find(Land.class, "XX").getLabel());
		} finally {
			em.close();
		}
	}

	public static void processExcel(final EntityManagerFactory emf)
			throws IOException, ScriptException, SecurityException, IllegalStateException, RollbackException,
			HeuristicMixedException, HeuristicRollbackException, SystemException, NotSupportedException {

		final EntityManager em = emf.createEntityManager();
		try {

			BitronixTransactionManager btm = TransactionManagerServices.getTransactionManager();

			btm.begin();

			em.joinTransaction();

			ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

			Object obj = engine.eval(new BufferedReader(new InputStreamReader(
					Applikation.class.getClassLoader().getResourceAsStream("country_codes.json"))));

			Collection array = ((jdk.nashorn.api.scripting.ScriptObjectMirror) obj).values();
			for (Object elem : array) {
				ScriptObjectMirror scriptObjectMirror = (jdk.nashorn.api.scripting.ScriptObjectMirror) elem;
				String label = scriptObjectMirror.get("label").toString();
				String countryCode = scriptObjectMirror.get("countryCode").toString();
				LOG.info(countryCode + " --- " + label);
				Land land = new Land(countryCode, label);

				 em.persist(land);

			}

			

			btm.commit();

		} finally {
			em.close();
		}
	}
}
