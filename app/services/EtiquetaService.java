package services;

import javax.inject.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;

import models.Usuario;
import models.UsuarioRepository;
import models.Tablero;
import models.TableroRepository;
import models.Etiqueta;
import models.EtiquetaRepository;

public class EtiquetaService {
  UsuarioRepository usuarioRepository;
	TableroRepository tableroRepository;
	EtiquetaRepository etiquetaRepository;

	@Inject
  public EtiquetaService(UsuarioRepository usuarioRepository, TableroRepository tableroRepository, 
                    EtiquetaRepository etiquetaRepository) {
		this.usuarioRepository = usuarioRepository;
		this.tableroRepository = tableroRepository;
		this.etiquetaRepository = etiquetaRepository;
  }

  private Usuario comprobarUsuarioExiste (Long idUsuario) {
    Usuario usuario = usuarioRepository.findById(idUsuario);
    if (usuario == null) {
      throw new EtiquetaServiceException("No existe el usuario");
    }
    return usuario;
  }

  private Tablero comprobarTableroExiste (Long idTablero) {
    Tablero tablero = tableroRepository.findById(idTablero);
    if (tablero == null) {
      throw new EtiquetaServiceException("No existe el tablero");
    }
    return tablero;
  }

  private Etiqueta comprobarEtiquetaExiste (Long idEtiqueta) {
    Etiqueta etiqueta = etiquetaRepository.findById(idEtiqueta);
    if (etiqueta == null) {
        throw new EtiquetaServiceException("No existe la etiqueta");
    }
    return etiqueta;
  }
  
  public List<Etiqueta> getEtiquetasTablero(Long idTablero) {
    Tablero tablero = comprobarTableroExiste(idTablero);
    List<Etiqueta> lista = new ArrayList<Etiqueta>(etiquetaRepository.getAll());
    Collections.sort(lista, (a, b) -> a.getId() < b.getId() ? -1 : a.getId() == b.getId() ? 0 : 1);
    return lista;
  }

  public Etiqueta getEtiqueta(Long idEtiqueta) {
    return comprobarEtiquetaExiste(idEtiqueta);
  }

  public Etiqueta crearEtiquetaTablero(Long idTablero, String nombre, String color) {
    Tablero tablero = comprobarTableroExiste(idTablero);
    Etiqueta nueva = new Etiqueta(tablero, nombre, color);
    return etiquetaRepository.add(nueva);
  }

  public Etiqueta modificarEtiquetaTablero(Long idEtiqueta, String nombre, String color) {
    Etiqueta etiqueta = comprobarEtiquetaExiste(idEtiqueta);
    etiqueta.setNombre(nombre);
    etiqueta.setColor(color);
    etiqueta = etiquetaRepository.update(etiqueta);
    return etiqueta;
  }

  public void eliminarEtiquetaTablero(Long idEtiqueta) {
    Etiqueta etiqueta = comprobarEtiquetaExiste(idEtiqueta);
    etiquetaRepository.delete(idEtiqueta);
  }
}