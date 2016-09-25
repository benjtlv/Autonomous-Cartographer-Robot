package calculs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Cartographe {
    public static final int H = 600;
    public static final int W = 800;

    public static final int PORTEE = 150;

    //public static int xR = 400;
    //public static int yR = 300;

    //int[][] carte = new int[W][H];
    int[][] matriceTmp = new int[W][H];
    ArrayList<Coor> list = new ArrayList<Coor>();
    ArrayList<Obstacle> comps = new ArrayList<Obstacle>();
    //ArrayList<Obstacle> oldComps = new ArrayList<Obstacle>();
    //ArrayList<Obstacle> compsTmp = new ArrayList<Obstacle>();
    ArrayList<Issue> issues = new ArrayList<Issue>();
    //ArrayList<Issue> issuesTmp = new ArrayList<Issue>();
    ArrayList<Sommet> sommetsAEnvoyer = new ArrayList<Sommet>();
    //Graphe graphe = new Graphe();
    //Point robot = new Point(xR,yR);
    //Sommet current = new Sommet(robot.x,robot.y);
    int i = 0;

    public void close(int[][] carte,Sommet current) {

	createObstacle(carte);
	
	for (Obstacle obs : oldComps) {
	    compsTmp.get(0).union(obs,carte,oldComps);
	}
	
	fillZone(compsTmp.get(0),carte,false,current);
	
	unify(carte);
	
	fusion();
	
	System.out.println("comps.size() = " + comps.size());
    }

    public void scan(ArrayList<Coor> list, Sommet current, int[][] carte) {
	// La super fonction magique qui à partir de ce que le robot calcule va compléter le graphe des issues 
	createObstacle(matriceTmp); //Consiste en un union-find sur les points que donne le robot pour établir les obstacles en tant que composantes connexes avec une racine commune pour chacun
	for (Obstacle o1 : comps)
	    for (Obstacle o2 : comps)
		o1.union(o2,matriceTmp,comps); //Si deux composantes sont proches on enregistre juste qu'on doit les fusionner
	for (Obstacle obs : compsTmp) fillZone(obs,matriceTmp,true,current); // Pour chaque obstacle on trace deux droites depuis les extrémités. Ceci nous donne un polygone à 4 coté donc on peut remplir la region qui correspond à cette Zone dans la matriceTmp de la valeur -1 depuis le barycentre. Les droites ont valeur -3
	createIssues(current); //Créé les arcs de cercle des issues dans la matrice (valeur -2) ainsi que les objets Issues correspondants grâce au union-find
	fusion();// On fusionne les zones à fusionner
	Remplissage.remplir(matriceTmp,current.getX(),current.getY(),1,true); // On remplit a matrice de la valeur 1 depuis le sommet courant pour créer l'espace connu
	unify(matriceTmp);// remplace toutes les valeurs -1 -2 et -3 par du 0. Donc tout deviendra noir 
	detectBox(carte);// Si après fusion on detecte un obstacle fermé, on sait qu'il s'agit d'un Objet réél (une caisse)
        associateExitWithObstacle(carte); //Ceci n'a pas été encore testé mais il existe des cas où le robot n'arrive pas à detecter les coins et donc ceci va être interprété comme une issue. Donc lorsque le robot se dirigera vers cette issue, le sonar lui dira qu'il approche d'un objet donc il doit s'arreter et faire un scan entre les deux bouts d'obstacle entre lesquels il se dirige, puisqu'en se rapprochant on a remarqué qu'il detectait enfin les coins. Cette fonction nous permet de connaître ces deux bouts d'obstacle, car chaque sommet va contenir l'objet Issue auquel il appartient, et cette Issue va nous donner ces deux bouts d'obstacles.
	for (Issue iss : issues) editIssue(iss,current); //Permet d'établir différentes sorties sur L'issue par rapport à la taille de celle-ci. Pas vraiment aux points pour les arcs de cercle supérieurs à Pi radians.
	for (Issue iss : issues) { //Pour chaque issues on créé des objets sommets correspondant aux sorties calculées par editIssue() et on les ajoute aux voisins du sommet courant. C'est ainsi que les graphe est construit : de proche en proche
	    for (Point p : iss.exits) {
		Point next = Bresenham.projete(matriceTmp,current.getX(),current.getY(),p.x,p.y,W,H,2);
		Sommet s = new Sommet(next.x,next.y);
		s.issue = iss;
		current.voisins.add(s);
		s.voisins.add(current);
		sommetsAEnvoyer.add(s);
	    }
	}
	maxCarte(carte); // C'est la pierre angulaire de la fonction Scan. Grâce à cette fonction on va pouvoir étendre la zone visible. Ce qui est visible vaut 1 et ce qui est inconnu vaut 0. Donc le max nous permet bien d'étendre cette zone.
	killExits(carte); // Pour effacer les anciennes issue qui se retrouvent dans la zone connue après extension. 
    }

    public killExits(int[][] carte) {
	ArrayList<Coor> coordsToRemove = new ArrayList<Coor>();
	ArrayList<Issue> issuesToRemove = new ArrayList<Issue>();
	for (Issue iss : issues) {
	    for (Coor c : iss) {
		if (Math.max(matriceTmp[c.x][c.y],carte[c.x][c.y]) == 1) {
		    coordsToRemove.add(c);
		}
	    }
	    iss.removeAll(coordsToRemove);
	    if (iss.size()==0) issuesToRemove.add(iss);
	    coordsToRemove.removeAll(coordsToRemove);
	}
	issues.removeAll(issuesToRemove);
    }

    public void associateExitWithObstacle(int[][] carte) {
	for (Issue iss : issuesTmp) {
	    for (Coor c : iss) {
		for (Obstacle obs : comps) {
		    Point ext1 = Bresenham.projete(carte,current.getX(),current.getY(),obs.extrem1.x,obs.extrem1.y,W,H,1);
		    Point ext2 = Bresenham.projete(carte,current.getX(),current.getY(),obs.extrem2.x,obs.extrem2.y,W,H,1);
		    if (iss.extrem1 == null) {
			if (c.isNVoisin(ext1,5)) {
			    iss.extrem1 = c;
			    iss.oe1 = obs.extrem1;
			} else if (c.isNVoisin(ext2,5)) {
			    iss.extrem1 = c;
			    iss.oe1 = obs.extrem2;
			}
		    } else {
			if (c.isNVoisin(ext1,5)) {
			    iss.extrem2 = c;
			    iss.oe2 = obs.extrem1;
			} else if (c.isNVoisin(ext2,5)) {
			    iss.extrem2 = c;
			    iss.oe2 = obs.extrem2;
			}
		    }
		}
	    }
	}
    }

    public void detectBox(int[][] carte) {
	for (Obstacle obs : comps) {
	    int xb = 0, yb = 0;
	    if (obs.closed) {
		System.out.println("closed");
		for (Coor c : obs) {
		    xb += c.x;
		    yb += c.y;
		}
		xb = (int)(xb/(double)obs.size());
		yb = (int)(yb/(double)obs.size());
		System.out.println("ici");
		Remplissage.remplir(carte,xb,yb,2,true);
	    }
	}
    }

    public void maxCarte(int[][] carte) {	
	for (int i=0; i<W; i++) {
	    for (int j=0; j<H; j++) {
		carte[i][j] = Math.max(matriceTmp[i][j],carte[i][j]);
	    }
	}
    }

    private void fusion() {
	for (Obstacle obs : compsTmp) {
	    if (obs.mixWith1 != null) {
		Obstacle o = new Obstacle();
		o.addAll(obs);
		o.addAll(obs.mixWith1);
		if (obs.mixWith2 != null) {
		    o.addAll(obs.mixWith2);
		    comps.remove(obs.mixWith2);
		}
		o.extrem1 = obs.newExtrem1;
		o.extrem2 = obs.newExtrem2;
		o.closed = obs.closed;
		comps.remove(obs);
		comps.remove(obs.mixWith1);
		comps.add(o);
	    }
	}
    }

    private void unify(int[][] m) {
	for (int i=0; i<W; i++) {
	    for (int j=0; j<H; j++) {
		if (m[i][j] == -1 || m[i][j] == -2 || m[i][j] == -3) {
		    m[i][j] = 0;
		}
	    }
	}
    }
	
    private void createObstacle(int[][] m) {
    
	/* -list : mesure courante
	   -on créé les composantes connexes
	*/
		
	for (Coor c1 : list) {
	    for (Coor c2 : list) {
		if (!c1.equals(c2)) {
		    if (c1.isNVoisin(c2,20)) {
			c1.union(c2);
		    }
		}
	    }
	}
		
	/* on créé donc les obstacles (liste comps) */
		
	oldComps.addAll(comps);
	for (Coor c : list) {
	    if (c.isRacine() && c.taille > 5) {
		Obstacle obstacle = new Obstacle();
		for (Coor ci : list) {
		    if (ci.pere == c) {
			obstacle.add(ci);
		    }
		}
		obstacle.init();
		comps.add(obstacle);
		compsTmp.add(obstacle);
	    }
	}

	/* on relie les points des obstacles */
		
	for (Obstacle obs : compsTmp) {
	    for (int i=0; i<obs.size()-1; i++) {
		Bresenham.tracerSegment(m,obs.get(i).x,obs.get(i).y,obs.get(i+1).x,obs.get(i+1).y,2);
	    }
	}
    }

    private void createIssues(Sommet current) {
	ArrayList<Coor> cercle = Bresenham.drawCircle(matriceTmp,current.getX(),current.getY(),PORTEE,W,H);

	for (int i=0; i<2; i++) {
	    for (Coor c1 : cercle) {
	    	for (Coor c2 : cercle) {
	    		if (!c1.equals(c2)) {
	    			if (c1.isNVoisin(c2,1)) {
	    				c1.union(c2);
	    			}
	    		}	
	    	}
	    }
	}
		
	for (Coor c : cercle) {
	    if (c.isRacine() && c.taille > 5) {
		Issue issue = new Issue();
		for (Coor ci : cercle) {
		    if (ci.pere == c) {
			issue.add(ci);
		    }
		}
		issues.add(issue);
		issuesTmp.add(issue);
	    }
	}
    }

    private void editIssue(Issue issue, Sommet current) {
	Point robot = new Point(current.getX(),current.getY());
    issue.init(robot);
	int s = issue.size();
	double e = issue.ecart;
	if (s < 400) {
	    createExits(issue,2,e,current);
	} else if (s >= 400 && s < 600) {
	    createExits(issue,3,e,current);
	} else if (s >= 600 && s < 848) {
	    createExits(issue,4,e,current);
	} else {
	    issue.exits.add(issue.extrem1);
	    createExits(issue,5,e,current);
	}

    }

    private void createExits(Issue iss, int div, double e, Sommet current) {
	double pas = e/div;
	double d = pas;
	double eps = 2;
	int nb = 0;
	Point robot = new Point(current.getX(),current.getY());
        while (Math.abs(d) < e) {
	    for (Coor coor : iss) {
		double ang = robot.angle(iss.extrem1,coor)*180.0/Math.PI;
		if (ang >= d-eps && ang <= d+eps) {
		    iss.exits.add(coor);
		    nb++;
		    break;
		}
	    }
	    d += pas;
	}
	ArrayList<Point> toRemove = new ArrayList<Point>();
	for (Point p : iss.exits) {
	    if (p.isNVoisin(iss.extrem1,30) || p.isNVoisin(iss.extrem2,30)) {
		toRemove.add(p);
	    }
	}
	iss.exits.removeAll(toRemove);
    }

    private boolean closeToExtrem(Issue iss) {
	for (Point p : iss.exits) 
	    if (p.isNVoisin(iss.extrem1,30) || p.isNVoisin(iss.extrem2,30))
		return true;
	return false;
    }
    
    private void fillZone(Obstacle obs, int[][] m, boolean b,Sommet current) {
	Coor extrem1, extrem2;
	Point barycentre;
	Point proj1, proj2;
	ZoneInc zone;
	zone = new ZoneInc();
	extrem1 = obs.getExtrem1();
	extrem2 = obs.getExtrem2();
	
	zone.add(extrem1);
	proj1 = Bresenham.projete(m,current.getX(),current.getY(),extrem1.x,extrem1.y,W,H,0);
	Bresenham.tracerSegment(m,extrem1.x,extrem1.y,proj1.x,proj1.y,-3);
	zone.add(proj1);
	
	proj2 = Bresenham.projete(m,current.getX(),current.getY(),extrem2.x,extrem2.y,W,H,0);
	zone.add(proj2);
	zone.add(extrem2);
	Bresenham.tracerSegment(m,extrem2.x,extrem2.y,proj2.x,proj2.y,-3);
	
	barycentre = zone.barycentre();
	
	Remplissage.remplir(m,barycentre.x,barycentre.y,-1,b);
    }
    
    public ArrayList<Sommet> getSommetsAEnvoyer() {
    	return sommetsAEnvoyer;
    }
   
}
