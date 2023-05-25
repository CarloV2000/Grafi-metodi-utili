package it.polito.tdp.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.DB.DAO;

	public class Model {
		
		private List<Album>allAlbum;
		private SimpleDirectedWeightedGraph<Album, DefaultWeightedEdge>graph;
		private DAO dao;
		
		private List<Album>bestPath;//campi globali per la ricorsione
		private int bestScore;
		
		public Model() {
			super();
			this.allAlbum = new ArrayList<>();
			
			this.dao = new DAO();
		}
		
		
		public void buildGraph(int n) {
			this.loadNodes(n);
			//creo grafo
			this.graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
			
			//vertici
			Graphs.addAllVertices(graph, this.allAlbum);
			System.out.println(this.graph.vertexSet().size());
			//archi
			for(Album a1 : this.allAlbum) {
				for(Album a2 : this.allAlbum) {
					int peso = a1.getNumSongs()-a2.getNumSongs();
					if(peso > 0) {//se a1 ha piu canzoni di a2
						Graphs.addEdgeWithVertices(this.graph, a2, a1, peso);
					}
				}
			}
			System.out.println(this.graph.edgeSet().size());
		}
		
		
		
		/**
		 * metodo che crea grafo e in caso di archi ripetuti incremento il peso all'arco gia esistente
		 * @param nMinCompagnieAeree
		 */
			public void creaGrafo(int nMinCompagnieAeree) {
				//grafo(gia creato nel costruttore)
				//idMap
				for(Airport x : this.allAeroporti) {
					this.aeroportiIdMap.put(x.getId(), x);
				}
				
				//vertici (gia compresi i vincoli sul numero di compagnie aeree)
				Graphs.addAllVertices(grafo, this.dao.getVertici(nMinCompagnieAeree, aeroportiIdMap));
				
				//archi
				List<CoppiaA>edges = dao.getArchi(aeroportiIdMap);
				for(CoppiaA x : edges) {
					Airport origin = x.getPartenza();
					Airport destination = x.getArrivo();
					int peso = x.getN();
				//metto controllo del tipo: se esistono i vertici: se l'arco esiste gia ci incremento il peso, altrimenti lo creo nuovo
					if(grafo.vertexSet().contains(origin) && grafo.vertexSet().contains(destination)) {
						DefaultWeightedEdge edge = this.grafo.getEdge(origin, destination);
						if(edge!=null) {
							double weight = this.grafo.getEdgeWeight(edge);
							weight += peso;
							this.grafo.setEdgeWeight(origin, destination, weight);
						} else {
							this.grafo.addEdge(origin, destination);
							this.grafo.setEdgeWeight(origin, destination, peso);
						}
					}
					
					
				}

		
		
		/**
		 * posto bilancio = (somma pesi archi entranti) - (somma peso archi uscenti)
		 * @param a è il vertice di cui calcolare il bilancio
		 * @return un int rappresentante il bilancio
		 */
		public int getBilancio(Album a){
			int bilancio = 0;
			List<DefaultWeightedEdge>entranti = new ArrayList<>(this.grafo.incomingEdgesOf(a));
			List<DefaultWeightedEdge>uscenti = new ArrayList<>(this.grafo.outgoingEdgesOf(a));
			
			for(DefaultWeightedEdge x : entranti) {
				bilancio += this.graph.getEdgeWeight(x);
			}
			for(DefaultWeightedEdge x : uscenti) {
				bilancio -= this.graph.getEdgeWeight(x);
			}
			
			return bilancio;
		}
		
		
		/**
		 * metodo per trovare tutti i vertici successori di un vertice ed ordinarli in base al bilancio(calcolato sopra)
		 * @param x è l'album di cui calcolare i successori
		 * @return una lista di oggetti Bilancioalbum(classe appositamente creata per avere due parametri(Album e int bilancio per ordinarli facilmente))
		 */
		public List<BilancioAlbum> successoriDiAlbum(Album x){
			List<Album> successori = new ArrayList<>(Graphs.successorListOf(this.grafo, x));
			List<BilancioAlbum>bilancioSuccessori = new ArrayList<>();
			for(Album a: successori) {
				BilancioAlbum bil = new BilancioAlbum(a, getBilancio(x));
				bilancioSuccessori.add(bil);
			}
			Collections.sort(bilancioSuccessori);
			return bilancioSuccessori;
		}
		
		
		/**
		 * determina il percorso minimo tra i due vertici con grafo pesato (usa algoritmo Dijkstra)
		 * @param partenza indica la fermata di partenza
		 * @param arrivo indica la fermata di arrivo
		 * @return lista di fermate rappresentanti il percorso piu breve
		 */
		public List<Album>percorso(Album partenza, Album arrivo){
			
			DijkstraShortestPath<Album, DefaultWeightedEdge> sp = new DijkstraShortestPath<>(this.graph);
			GraphPath<Album, DefaultWeightedEdge> gp = sp.getPath(partenza, arrivo);
			List<Album>percorso = new ArrayList<>(gp.getVertexList());
			
			return percorso;
		}
		
		/**
		 * metodo che conta i (Country) confinanti dato un (Country)
		 * @param c è il paese di cui calcolare il numero di confinanti
		 * @param anno è l'anno inserito dall'utente (entro il quale i cambiamenti di confini storici sono validi)
		 * @return un int rappresentante il numero di stati confinanti
		 */
		public int contaConfinantiDatoPaese(Album c, int anno) {
			int n = 0;
			DAO dao = new BordersDAO();
			for(Border b : dao.getCountryPairs(anno, countryIdMap)) {
				if(b.getPaese1().equals(c)) {
					n++;
				}
			}
			return n;
		}
		
		public int getNumberOfConnectedComponents(){
			int nComponentiConnesse = 0;
			
			ConnectivityInspector<Album, DefaultWeightedEdge> inspector = new ConnectivityInspector<>(this.graph);
	                  List<Set<Album>> connectedComponents = inspector.connectedSets();
	                  for (Set<Album> component : connectedComponents) {
	                 	    nComponentiConnesse++;
		         }
	        
	        return nComponentiConnesse;
	    }
		
		
		/**
		 * viene visualizzata la lista di tutti i vertici raggiungibili nel grafo
		a partire da un vertice selezionato, che coincide con la componente connessa del grafo relativa allo stato
		scelto (ho usato il metodo Graphs.neighborListOf(grafo, vertice) per trovare tutti i confinanti).
		 * @param c è il (Country) di cui voglio sapere i confinanti
		 * @return una list di (Country):  adiacenti a quello inserito
		 */
		public List<Album> trovaConfinanti(Album c, int anno) {
			List<Album>confinanti = new ArrayList<Album>();
			confinanti = Graphs.neighborListOf(graph, c);		
			return confinanti;
		}


		
	
}
