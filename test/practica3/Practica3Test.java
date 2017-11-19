import org.junit.*;
import static org.junit.Assert.*;

import play.db.jpa.*;

import org.dbunit.*;
import org.dbunit.dataset.*;
import org.dbunit.dataset.xml.*;
import org.dbunit.operation.*;
import java.io.FileInputStream;

import play.inject.guice.GuiceApplicationBuilder;
import play.inject.Injector;
import play.inject.guice.GuiceInjectorBuilder;
import play.Environment;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.List;

import models.Usuario;
import models.Tarea;

import services.UsuarioService;
import services.UsuarioServiceException;
import services.TareaService;
import services.TareaServiceException;

public class Practica3Test {
  static private Injector injector;

  // Se ejecuta sólo una vez, al principio de todos los tests
  @BeforeClass
  static public void initApplication() {
    GuiceApplicationBuilder guiceApplicationBuilder =
    new GuiceApplicationBuilder().in(Environment.simple());
    injector = guiceApplicationBuilder.injector();
    injector.instanceOf(JPAApi.class);
  }

  @Before
  public void initData() throws Exception {
    JndiDatabaseTester databaseTester = new JndiDatabaseTester("DBTest");
    IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new FileInputStream("test/resources/usuarios_dataset.xml"));
    databaseTester.setDataSet(initialDataSet);
    databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
    databaseTester.onSetup();
  }

  private UsuarioService newUsuarioService() {
    return injector.instanceOf(UsuarioService.class);
  }

  private TareaService newTareaService() {
    return injector.instanceOf(TareaService.class);
  }

  @Test
  public void crearTareaFechaLimite() {
		UsuarioService usuarioService = newUsuarioService();
    TareaService tareaService = newTareaService();
		Usuario usuario = usuarioService.creaUsuario("UsuarioTest", "test@test.com", "testPass");
		Tarea test = null;
		try {
			test = tareaService.nuevaTarea(usuario.getId(), "Comprar pan", new SimpleDateFormat("dd/MM/yyyy HH:mm").parse("23/12/2017 12:23"));
		}
		catch (Exception e) {
			fail();
		}
		assertEquals("2017-12-23 12:23:00.0", test.getFechaLimite().toString());
		assertEquals(test.getFechaLimiteString(), "23/12/2017 - 12:23");
  }

  @Test(expected = TareaServiceException.class)
	public void crearTareaFechaLimiteAnterior() {
		UsuarioService usuarioService = newUsuarioService();
		TareaService tareaService = newTareaService();
		Usuario usuario = usuarioService.creaUsuario("UsuarioTest", "test@test.com", "testPass");
		Date fecha = null;
		try {
			fecha = new SimpleDateFormat("dd/MM/yyyy - HH:mm").parse("23/12/1900 - 12:23");
		}
		catch (Exception e) {
			fail();
		}
		Tarea test = tareaService.nuevaTarea(usuario.getId(), "Comprar agua", fecha);
  }
}
