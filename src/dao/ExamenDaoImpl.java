package dao;

import static dao.DAOUtilitaire.fermeturesSilencieuses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.TreeSet;

import beans.Etudiant;
import beans.Examen;
import beans.FormatExamen;
import beans.Groupe;
import beans.Matiere;
import beans.Note;
import beans.Professeur;

public class ExamenDaoImpl implements ExamenDao 
{
    private static final String SQL_INSERT_EXAMEN = "INSERT INTO gnw_examen(fk_format, nom, date, coefficient, fk_professeur, fk_groupe, fk_matiere, fk_utilisateur) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	public static final String SQL_SELECT_TOUS = "SELECT gnw_examen.id, gnw_examen.nom, gnw_examen.date, gnw_examen.fk_format as formatId, gnw_formatexamen.nom as formatNom, gnw_groupe.nom as groupeNom, gnw_matiere.nom as matiereNom, gnw_examen.moyenne, gnw_examen.coefficient FROM gnw_examen, gnw_matiere, gnw_groupe, gnw_formatexamen WHERE gnw_examen.date_suppr Is NULL AND gnw_examen.fk_groupe = gnw_groupe.id AND gnw_examen.fk_matiere = gnw_matiere.id AND gnw_examen.fk_format = gnw_formatexamen.id AND gnw_examen.fk_professeur = ?";
	private static final String SQL_SELECT_EXAMEN_PAR_ID = "SELECT gnw_examen.id, gnw_examen.nom, gnw_examen.date, gnw_examen.fk_format as formatId, gnw_formatexamen.nom as formatNom, gnw_groupe.nom as groupeNom, gnw_matiere.nom as matiereNom, gnw_examen.moyenne, gnw_examen.coefficient FROM gnw_examen, gnw_matiere, gnw_groupe, gnw_formatexamen WHERE gnw_examen.date_suppr IS NULL AND gnw_examen.fk_groupe = gnw_groupe.id AND gnw_examen.fk_format = gnw_formatexamen.id AND gnw_examen.fk_matiere = gnw_matiere.id AND gnw_examen.id = ?";
	private DAOFactory daoFactory;
	private EtudiantDao etudiantDao;
	
	/**
	 * RéRécupère la daoFactory
	 * 
	 * @param daoFactory
	 */
	ExamenDaoImpl(DAOFactory daoFactory) 
	{
        this.daoFactory = daoFactory;
    }
	
	/**
     * Ajoute un examen dans la base de données
     * 
     * @param examen
     * @throws DAOException
     */
	public void creer(Examen examen)
	{
		Etudiant etudiant = new Etudiant();
		Set<Etudiant> listeEtudiants = new TreeSet<Etudiant>();
		
		ajouterExamen(examen);
		recupererListeNotes(examen);
;		//mettreAJourNotes(examen);
	}
	
	/**
	 * Récupère la liste des notes d'un examen
	 * 
	 * @param examen
	 */
	private void recupererListeNotes(Examen examen)
	{
		Etudiant etudiant = new Etudiant();
		Set<Etudiant> listeEtudiants = new TreeSet<Etudiant>();
		Set<Note> listeNotes = new TreeSet<Note>();
		
		etudiant.setGroupe(examen.getGroupe());
		listeEtudiants = etudiantDao.rechercher(etudiant);
		Object[] etudiants = listeEtudiants.toArray();
		
		for(Object e : etudiants)
		{
			Note note = new Note();
			Etudiant etudiant1 = (Etudiant) e;
			note.setEtudiant(etudiant1);
			listeNotes.add(note);
			
		}
		examen.setListeNotes(listeNotes);
	}
	
	/**
	 * Ajoute un examen dans la base de données
	 * 
	 * @param examen
	 * @throws DAOException
	 */
	public void ajouterExamen(Examen examen) throws DAOException
	{
		Connection connexion = null;
		PreparedStatement preparedStatement = null;
		Groupe groupe = new Groupe(examen.getGroupe());
		Matiere matiere = new Matiere(examen.getMatiere());
		Professeur professeur = new Professeur(examen.getProfesseur());
		FormatExamen format = new FormatExamen(examen.getFormat());
		ResultSet resultSet;
		
		try 
		{
			connexion = daoFactory.getConnection();
			
			// Insertion de l'examen dans la base de données
			preparedStatement = initialisationRequetePreparee(connexion, SQL_INSERT_EXAMEN, true, format.getId(), examen.getNom(), examen.getDate(), examen.getCoefficient(), professeur.getId(), groupe.getId(), matiere.getId(), professeur.getId());
			preparedStatement.executeUpdate();
			
			// Récupération de l'id de l'examen
			resultSet = preparedStatement.getGeneratedKeys();
			
			if (resultSet.next()) 
			{
				examen.setId(resultSet.getLong(1));
			}

		} 
		catch (SQLException e) 
		{
			throw new DAOException(e);
		} 
		finally 
		{
			fermeturesSilencieuses(preparedStatement, connexion);
		}
	}
	
	/**
     * Recherche un ou des examen(s) dans la base de données
     * 
     * @param examen
     * @throws DAOException
     */
	public Set<Examen> rechercher(Examen examen) throws DAOException
	{
		Set<Examen> listeExamens = new TreeSet<Examen>();
		Groupe groupe = new Groupe(examen.getGroupe());
		Matiere matiere = new Matiere(examen.getMatiere());
		Professeur professeur = new Professeur(examen.getProfesseur());
		FormatExamen format = new FormatExamen(examen.getFormat());
		Connection connexion = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String sqlSelectRecherche = SQL_SELECT_TOUS;
		
		try
		{	
			connexion = daoFactory.getConnection();
			
			if(examen.getId() != null) 
			{
				sqlSelectRecherche += " AND gnw_examen.id = ?";
			}
			else
			{
				sqlSelectRecherche += " AND gnw_examen.id IS NOT ?";	
			}
			
			if (examen.getNom() != null)
			{
				sqlSelectRecherche += " AND gnw_examen.nom LIKE ?";
				examen.setNom("%" + examen.getNom() + "%");
			}
			else
			{
				sqlSelectRecherche += " AND gnw_examen.nom IS NOT ?";	
			}
			
			if (examen.getDate() != null)
			{
				sqlSelectRecherche += " AND gnw_examen.date = ?";
			}
			else
			{
				sqlSelectRecherche += " AND gnw_examen.date IS NOT ?";	
			}
			
			if (format.getId() != null)
			{
				sqlSelectRecherche += " AND gnw_examen.fk_format = ?";
			}
			else
			{
				sqlSelectRecherche += " AND gnw_examen.fk_format IS NOT ?";	
			}
			
			if(groupe.getId() != null)
			{
				sqlSelectRecherche += " AND gnw_examen.fk_groupe = ?";
			}
			else
			{
				sqlSelectRecherche += " AND gnw_examen.fk_groupe IS NOT ?";	
			}
			
			if(matiere.getId() != null)
			{
				sqlSelectRecherche += " AND gnw_examen.fk_matiere = ?";
			}
			else
			{
				sqlSelectRecherche += " AND gnw_examen.fk_matiere IS NOT ?";	
			}
			
			preparedStatement = initialisationRequetePreparee(connexion, sqlSelectRecherche, true, professeur.getId(), examen.getId(), examen.getNom(), examen.getDate(), format.getId(), groupe.getId(), matiere.getId());
			System.out.println(preparedStatement.toString());
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next()) 
			{
				examen = mapExamen(resultSet);
				listeExamens.add(examen);
	        }
		} 
		catch (SQLException e) 
		{
			throw new DAOException(e);
		} 
		finally 
		{
			fermeturesSilencieuses(resultSet, preparedStatement, connexion);
		}
		
		return listeExamens;
	}
	
	/**
	 * Edite un examen dans la base de données
	 * 
	 * @param examen
	 * @throws DAOException
	 */
	public Examen editer(Examen examen) throws DAOException
	{

		return examen;
	}
	
	/**
	 * Trouve un examen dans la base de données
	 * 
	 * @param examen
	 * @throws DAOException
	 */
	public Examen trouver(Examen examen) throws DAOException
	{
		Connection connexion = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try 
		{
			connexion = daoFactory.getConnection();
			preparedStatement = initialisationRequetePreparee(connexion, SQL_SELECT_EXAMEN_PAR_ID, true, examen.getId());
			resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next()) 
			{
				examen = mapExamen(resultSet);
	        }
		} 
		catch (SQLException e) 
		{
			throw new DAOException(e);
		} 
		finally 
		{
			fermeturesSilencieuses(preparedStatement, connexion);
		}
		
		return examen;
	}

	/**
	 * Supprime un examen dans la base de données
	 * 
	 * @param examen
	 * @throws DAOException
	 */
	public int supprimer(Examen examen) throws DAOException
	{
		return 1;
	}
	
	/**
	 * Prépare une requête SQL sur mesure
	 * 
	 * @param connexion
	 * @param sql
	 * @param returnGeneratedKeys
	 * @param objets
	 * @return preparedStatement
	 * @throws SQLException
	 */
	public static PreparedStatement initialisationRequetePreparee(Connection connexion, String sql, boolean returnGeneratedKeys, Object... objets) throws SQLException 
	{
		PreparedStatement preparedStatement = connexion.prepareStatement(sql, returnGeneratedKeys ?Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
		
		for (int i = 0; i < objets.length; i++) 
		{
			preparedStatement.setObject(i + 1, objets[i]);
		}
		
		return preparedStatement;
	}
	
	/**
	 * Transfère les données du resultSet vers un objet Examen
	 * 
	 * @param resultSet
	 * @return groupe
	 * @throws SQLException
	 */
	private static Examen mapExamen(ResultSet resultSet) throws SQLException 
	{
		Examen examen = new Examen();
		Matiere matiere = new Matiere();
		Groupe groupe = new Groupe();
		FormatExamen format = new FormatExamen();
		
		examen.setId(resultSet.getLong("id"));
		examen.setNom(resultSet.getString("nom"));
		examen.setCoefficient(resultSet.getFloat("coefficient"));
		examen.setDate(convertirDateToString(resultSet.getDate("date")));
		format.setId(resultSet.getLong("formatId"));
		format.setNom(resultSet.getString("formatNom"));
		examen.setFormat(format);
		examen.setMoyenneGenerale(resultSet.getFloat("moyenne"));
		groupe.setNom(resultSet.getString("groupeNom"));
		matiere.setNom(resultSet.getString("matiereNom"));
		examen.setGroupe(groupe);
		examen.setMatiere(matiere);
		
		return examen;
	}
	
	/**
	 * Converti une Date en chaine de caractères
	 * 
	 * @param date
	 * @return 
	 */
	private static String convertirDateToString(java.sql.Date date)
	{
		DateFormat APP_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
		
		return APP_FORMAT.format(date);
		
	}
}
