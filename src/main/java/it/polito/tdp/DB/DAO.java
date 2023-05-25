package it.polito.tdp.DB;

public class DAO {
	
	
	
	/**
	 * metodo per trovare tutte le coppie aeroportoPartenza e aeroportoArrivo(CoppiaA è una classe che contiene partenza, arrivo e peso)
	 * @param aeroportiIdMap è la idMap che associa ogni aeroporto al suo id
	 * @return lista di tutte le coppie
	 */
	public List<CoppiaA> getAllCoppie (Map<Integer, Airport>aeroportiIdMap) {
		String sql = "SELECT distinct ORIGIN_AIRPORT_ID, DESTINATION_AIRPORT_ID, DISTANCE "
				+ "FROM flights ";
		List<CoppiaA>allCoppie = new ArrayList<>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();
			
			while (rs.next()) {
				CoppiaA coppia = new CoppiaA(aeroportiIdMap.get(rs.getInt("ORIGIN_AIRPORT_ID")),   aeroportiIdMap.get(rs.getInt("DESTINATION_AIRPORT_ID")), rs.getInt("DISTANCE"));
				allCoppie.add(coppia);
			
			}
			conn.close();
			return allCoppie;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}


}
