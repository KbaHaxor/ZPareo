package servlets.ai.professeur;

import java.io.IOException;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import beans.Administrateur;
import beans.Groupe;
import beans.Matiere;
import dao.DAOFactory;
import dao.GroupeDao;
import dao.MatiereDao;
import dao.ProfesseurDao;
import forms.ProfesseurForm;

@SuppressWarnings("serial")
@WebServlet("/ai/professeur/edition")
public class ProfesseurEdition extends HttpServlet 
{
	private static final String CONF_DAO_FACTORY           = "daofactory";
	private static final String ATT_SESSION_ADMINISTRATEUR = "sessionAdministrateur";
	private static final String ATT_MATIERES               = "listeMatieres";
	private static final String ATT_GROUPES                = "listeGroupes";
	private static final String ATT_PROFESSEUR             = "professeur";
	private static final String ATT_FORM                   = "form";
	private static final String VUE_EDITION                = "/WEB-INF/ai/professeur/edition.jsp";
	private ProfesseurDao professeurDao;
	private GroupeDao groupeDao;
	private MatiereDao matiereDao;
	
	public void init() throws ServletException 
    {
		this.matiereDao = ((DAOFactory) getServletContext().getAttribute(CONF_DAO_FACTORY)).getMatiereDao();
        this.groupeDao = ((DAOFactory) getServletContext().getAttribute(CONF_DAO_FACTORY)).getGroupeDao();
        this.professeurDao = ((DAOFactory) getServletContext().getAttribute(CONF_DAO_FACTORY)).getProfesseurDao();
    }

    public ProfesseurEdition() 
    {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		Matiere matiere = new Matiere();
		Groupe groupe = new Groupe();
		Set<Groupe> listeGroupes = this.groupeDao.rechercher(groupe);
		Set<Matiere> listeMatieres = this.matiereDao.rechercher(matiere);
		ProfesseurForm form = new ProfesseurForm(this.professeurDao);
		beans.Professeur professeur = form.trouverProfesseur(request);
		
		request.setAttribute(ATT_FORM, form);
		request.setAttribute(ATT_PROFESSEUR, professeur);
        request.setAttribute(ATT_MATIERES, listeMatieres);
        request.setAttribute(ATT_GROUPES, listeGroupes);
        this.getServletContext().getRequestDispatcher(VUE_EDITION).forward(request, response);   
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		Matiere matiere = new Matiere();
		Groupe groupe = new Groupe();
		ProfesseurForm form = new ProfesseurForm(this.professeurDao);
		Set<Groupe> listeGroupes = this.groupeDao.rechercher(groupe);
		Set<Matiere> listeMatieres = this.matiereDao.rechercher(matiere);
		beans.Professeur professeur = form.editerProfesseur(request);
		
		if (form.getErreurs().isEmpty()) response.sendRedirect("http://localhost:8080/ZPareo/ai/professeur");   
	    else
	    {
	    	request.setAttribute(ATT_FORM, form);
	    	request.setAttribute(ATT_PROFESSEUR, professeur);
	        request.setAttribute(ATT_MATIERES, listeMatieres);
	        request.setAttribute(ATT_GROUPES, listeGroupes);
	        this.getServletContext().getRequestDispatcher(VUE_EDITION).forward(request, response);   
	    }
	}
}