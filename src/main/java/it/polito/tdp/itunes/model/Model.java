package it.polito.tdp.itunes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.itunes.db.ItunesDAO;

public class Model {
	
	private Graph<Album,DefaultEdge> grafo;
	private int dimMax;
	private List<Album> setMax;
	
	public void creaGrafo(Double duration) {
		
		// inizializzo il grafo
		this.grafo = new SimpleWeightedGraph<>(DefaultEdge.class);
		
		ItunesDAO dao = new ItunesDAO();
		// aggiungo i vertici richiamando direttamente la funzione dal dao
		Graphs.addAllVertices(this.grafo, dao.getAllAlbumsWithDuration(duration));
		
		Map<Integer,Album> albumIdMap = new HashMap();
		
		for(Album a : this.grafo.vertexSet()) {
			albumIdMap.put(a.getAlbumId(), a);
		}
		
		
		// aggiungo gli archi
		// coppie di album, album 1 e 2. 
		// join tra due album, seleziono una track di questi due album che devono appartenere all'album
		
		List<Pair<Integer, Integer>> archi = dao.getCompatibleAlbums();
		
		for(Pair<Integer,Integer> arco : archi) {
			if(albumIdMap.containsKey(arco.getFirst()) && albumIdMap.containsKey(arco.getSecond()) 
					&& (!arco.getFirst().equals(arco.getSecond()))){
				this.grafo.addEdge(albumIdMap.get(arco.getFirst()), albumIdMap.get(arco.getSecond()));
			}
		}
		
		System.out.println("i vertici sono: "+this.grafo.vertexSet().size());
		System.out.println("gli archi sono: "+this.grafo.edgeSet().size()+"\n");
	}
	
	public List<Album> getAlbums(){
		List<Album> list = new ArrayList<Album>(this.grafo.vertexSet());
		
		Collections.sort(list);
		
		return list;
	}
	
	// CALCOLO LA COMPONENTE CONNESSA DI UN ALBUM SELEZIONATO
	public Set<Album> getComponente(Album a1){
		// restituisce un set di vertici della componente connessa
		ConnectivityInspector<Album, DefaultEdge> ci = new ConnectivityInspector<>(this.grafo);
		
		return ci.connectedSetOf(a1);
		
	}
	
	
	public Set<Album> ricercaSetMassimo(Album a1, double dTot){
		
		if(a1.getDurata()>dTot) {
			return null; // perche la soluzione non c'è
		}
		
		List<Album> parziale = new ArrayList<>();
//		parziale.add(a1);
		
		List<Album> tutti = new ArrayList<>(getComponente(a1));
		tutti.remove(a1);
		
		dimMax = 1;
		setMax = new ArrayList<>(parziale);
		
		cerca(parziale,0,0.0,dTot-a1.getDurata(),tutti);
		
		Set<Album> risultato = new HashSet<>(setMax);
		risultato.add(a1);
		
		return risultato;
	}
	
	// ricorsione
	
	private void cerca(List<Album> parziale, int livello, double durataParziale,double dTot, List<Album> tutti) {
		
		// gestisco il caso ottimo
		
		if(parziale.size()>dimMax) {
			dimMax = parziale.size();
			setMax = new ArrayList<>(parziale); // mi faccio una coppia di parziale
			
		}
		
		for(Album nuovo : tutti) {
			if(livello==0 || nuovo.getAlbumId() > parziale.get(parziale.size()-1).getAlbumId() && durataParziale + nuovo.getDurata() <= dTot) {
				parziale.add(nuovo);
				cerca(parziale,livello+1,durataParziale+nuovo.getDurata(),dTot, tutti);
				parziale.remove(parziale.size()-1);
			}
		}
	}
	
}
