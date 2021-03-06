import org.junit.*;

import static org.junit.Assert.*;

import play.db.Database;
import play.db.Databases;
import play.db.jpa.*;

import play.inject.guice.GuiceApplicationBuilder;
import play.inject.Injector;
import play.inject.guice.GuiceInjectorBuilder;
import play.Environment;

import play.Logger;

import java.sql.*;

import org.dbunit.*;
import org.dbunit.dataset.*;
import org.dbunit.dataset.xml.*;
import org.dbunit.operation.*;
import java.io.FileInputStream;

import java.util.List;

import models.Usuario;
import models.Tarea;
import models.Tablero;
import models.UsuarioRepository;
import models.JPAUsuarioRepository;
import models.TareaRepository;
import models.JPATareaRepository;
import models.TableroRepository;
import models.JPATableroRepository;

public class TareaTest {
   static Database db;
   static private Injector injector;

   // Se ejecuta sólo una vez, al principio de todos los tests
   @BeforeClass
   static public void initApplication() {
     GuiceApplicationBuilder guiceApplicationBuilder =
          new GuiceApplicationBuilder().in(Environment.simple());
     injector = guiceApplicationBuilder.injector();
     db = injector.instanceOf(Database.class);
     // Necesario para inicializar JPA
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

   private TareaRepository newTareaRepository() {
      return injector.instanceOf(TareaRepository.class);
   }

   private UsuarioRepository newUsuarioRepository() {
      return injector.instanceOf(UsuarioRepository.class);
   }

   private TableroRepository newTableroRepository() {
      return injector.instanceOf(TableroRepository.class);
   }

   // Test #11: testCrearTarea
   @Test
   public void testCrearTarea() {
      Usuario usuario = new Usuario("juangutierrez", "juangutierrez@gmail.com");
      Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS", null, false);

      assertEquals("juangutierrez", tarea.getUsuario().getLogin());
      assertEquals("juangutierrez@gmail.com", tarea.getUsuario().getEmail());
      assertEquals("Práctica 1 de MADS", tarea.getTitulo());
   }

   @Test
   public void testCrearTareaTablero() {
      Usuario usuario = new Usuario("juangutierrez", "juangutierrez@gmail.com");
      Tablero tablero = new Tablero(usuario, "TestTarea");
      Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS", null, false, tablero);
      assertEquals("TestTarea", tarea.getTablero().getNombre());
   }

   // Test #14: testEqualsTareasConId
   @Test
   public void testEqualsTareasConId() {
      Usuario usuario = new Usuario("juangutierrez", "juangutierrez@gmail.com");
      Tarea tarea1 = new Tarea(usuario, "Práctica 1 de MADS", null, false);
      Tarea tarea2 = new Tarea(usuario, "Renovar DNI", null, false);
      Tarea tarea3 = new Tarea(usuario, "Pagar el alquiler", null, false);
      tarea1.setId(1000L);
      tarea2.setId(1000L);
      tarea3.setId(2L);
      assertEquals(tarea1, tarea2);
      assertNotEquals(tarea1, tarea3);
   }

   // Test #15
   @Test
   public void testEqualsTareasSinId() {
      Usuario usuario = new Usuario("juangutierrez", "juangutierrez@gmail.com");
      Tarea tarea1 = new Tarea(usuario, "Renovar DNI", null, false);
      Tarea tarea2 = new Tarea(usuario, "Renovar DNI", null, false);
      Tarea tarea3 = new Tarea(usuario, "Pagar el alquiler", null, false);
      assertEquals(tarea1, tarea2);
      assertNotEquals(tarea1, tarea3);
   }

   // Test #16: testAddTareaJPARepositoryInsertsTareaDatabase
   @Test
   public void testAddTareaJPARepositoryInsertsTareaDatabase() {
      UsuarioRepository usuarioRepository = newUsuarioRepository();
      TareaRepository tareaRepository = newTareaRepository();
      Usuario usuario = new Usuario("juangutierrez", "juangutierrez@gmail.com");
      usuario = usuarioRepository.add(usuario);
      Tarea tarea = new Tarea(usuario, "Renovar DNI", null, false);
      tarea = tareaRepository.add(tarea);
      Logger.info("Número de tarea: " + Long.toString(tarea.getId()));
      assertNotNull(tarea.getId());
      assertEquals("Renovar DNI", getTituloFromTareaDB(tarea.getId()));
   }

   @Test
   public void testAddTareaTableroJPARepositoryInsertsTareaDatabase() {
      UsuarioRepository usuarioRepository = newUsuarioRepository();
      TareaRepository tareaRepository = newTareaRepository();
      TableroRepository tableroRepository = newTableroRepository();
      Usuario usuario = new Usuario("juangutierrez", "juangutierrez@gmail.com");
      usuario = usuarioRepository.add(usuario);
      Tablero tablero = new Tablero(usuario, "TestTarea");
      tablero = tableroRepository.add(tablero);
      Tarea tarea = new Tarea(usuario, "Prueba", null, false, tablero);
      tarea = tareaRepository.add(tarea);
      assertNotNull(tablero.getId());
      assertEquals("Prueba", getTituloFromTareaDB(tarea.getId()));
   }

   private String getTituloFromTareaDB(Long tareaId) {
      String titulo = db.withConnection(connection -> {
         String selectStatement = "SELECT TITULO FROM Tarea WHERE ID = ? ";
         PreparedStatement prepStmt = connection.prepareStatement(selectStatement);
         prepStmt.setLong(1, tareaId);
         ResultSet rs = prepStmt.executeQuery();
         rs.next();
         return rs.getString("TITULO");
      });
      return titulo;
   }

   // Test #17 testFindTareaById
   @Test
   public void testFindTareaPorId() {
      TareaRepository repository = newTareaRepository();
      Tarea tarea = repository.findById(1000L);
      assertEquals("Renovar DNI", tarea.getTitulo());
   }

   // Test #18 testFindAllTareasUsuario
   @Test
   public void testFindAllTareasUsuario() {
     UsuarioRepository repository = newUsuarioRepository();
     Usuario usuario = repository.findById(1000L);
     assertEquals(2, usuario.getTareas().size());
   }
}
