import java.util.ArrayList;

public class Sommet {
    private int x;
    private int y;
    boolean isVisite = false;
    Issue issue;
    ArrayList<Sommet> voisins = new ArrayList<Sommet>();
	
    public Sommet(int x, int y){
	this.x = x;
	this.y = y;
    }

    public int getX() {
	return x;
    }

    public boolean isVisite() {
	return isVisite;
    }

    public void setVisite(boolean isVisite) {
	this.isVisite = isVisite;
    }

    public void setX(int x) {
	this.x = x;
    }

    public int getY() {
	return y;
    }

    public void setY(int y) {
	this.y = y;
    }
	
	
}
