package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class ListaReproduccion implements ListModel<String>, Serializable {
	private static final long serialVersionUID = 1L;
	ArrayList<FicheroMultimedia> ficherosLista = new ArrayList();
	int ficheroEnCurso = -1;
	private static final boolean ANYADIR_A_FIC_LOG = true;
	private static Logger logger = Logger.getLogger(ListaReproduccion.class.getName());
	private static Random genAleat;
	transient ArrayList<ListDataListener> misEscuchadores = this.initDataListeners();

	static {
		try {
			logger.setLevel(Level.FINEST);
			Formatter f = new SimpleFormatter() {
				public synchronized String format(LogRecord record) {
					if (record.getLevel().intValue() < Level.CONFIG.intValue()) {
						return "\t\t(" + record.getLevel() + ") " + record.getMessage() + "\n";
					} else {
						return record.getLevel().intValue() < Level.WARNING.intValue()
								? "\t(" + record.getLevel() + ") " + record.getMessage() + "\n"
								: "(" + record.getLevel() + ") " + record.getMessage() + "\n";
					}
				}
			};
			FileOutputStream fLog = new FileOutputStream(ListaReproduccion.class.getName() + ".log", true);
			Handler h = new StreamHandler(fLog, f);
			h.setLevel(Level.FINEST);
			logger.addHandler(h);
			logger.addHandler(new FileHandler(ListaReproduccion.class.getName() + ".log.xml", true));
		} catch (IOException | SecurityException var3) {
			logger.log(Level.SEVERE, "No se ha podido crear fichero de log en clase ListaReproduccion");
		}

		logger.log(Level.INFO, "");
		logger.log(Level.INFO, DateFormat.getDateTimeInstance(1, 1).format(new Date()));
		genAleat = new Random();
	}

	public int add(String carpetaFicheros, String filtroFicheros, boolean cargarDeBD) {
		int ficsAnyadidos = 0;
		if (carpetaFicheros != null) {
			logger.log(Level.INFO, "Añadiendo ficheros con filtro " + filtroFicheros);

			try {
				filtroFicheros = filtroFicheros.replaceAll("\\.", "\\\\.");
				filtroFicheros = filtroFicheros.replaceAll("\\*", ".*");
				logger.log(Level.INFO, "expresión regular  filtro: " + filtroFicheros);
				Pattern pFics = Pattern.compile(filtroFicheros, 2);
				File fInic = new File(carpetaFicheros);
				ficsAnyadidos = this.procesaCarpeta(fInic, pFics, cargarDeBD);
			} catch (PatternSyntaxException var7) {
				logger.log(Level.SEVERE, "Error en patrón de expresión  ", var7);
			}
		}

		logger.log(Level.INFO, "ficheros añadidos: " + ficsAnyadidos);
		return ficsAnyadidos;
	}

	private int procesaCarpeta(File fic, Pattern pFics, boolean cargarDeBD) {
		logger.log(Level.FINE, "Procesando fichero/carpeta " + fic.getName());
		if (!fic.isDirectory()) {
			if (pFics.matcher(fic.getName()).matches()) {
				logger.log(Level.INFO, "Añadido vídeo: " + fic.getName());
				this.add(fic, cargarDeBD);
				return 1;
			} else {
				return 0;
			}
		} else {
			int ficsAnyadidos = 0;
			File[] var8;
			int var7 = (var8 = fic.listFiles()).length;

			for (int var6 = 0; var6 < var7; ++var6) {
				File f = var8[var6];
				ficsAnyadidos += this.procesaCarpeta(f, pFics, cargarDeBD);
			}

			return ficsAnyadidos;
		}
	}

	public File getFic(int posi) throws IndexOutOfBoundsException {
		return ((FicheroMultimedia) this.ficherosLista.get(posi)).file;
	}

	public FicheroMultimedia getFicMM(int posi) throws IndexOutOfBoundsException {
		return (FicheroMultimedia) this.ficherosLista.get(posi);
	}

	public void intercambia(int posi1, int posi2) {
		if (posi1 >= 0 && posi2 >= 0 && posi1 < this.ficherosLista.size() && posi2 <= this.ficherosLista.size()) {
			FicheroMultimedia temp = (FicheroMultimedia) this.ficherosLista.get(posi1);
			this.ficherosLista.set(posi1, (FicheroMultimedia) this.ficherosLista.get(posi2));
			this.ficherosLista.set(posi2, temp);
		}
	}

	public int size() {
		return this.ficherosLista.size();
	}

	public void add(File f, boolean cargarDeBD) {
		FicheroMultimedia fm = new FicheroMultimedia(f);
		this.ficherosLista.add(fm);
		this.avisarAnyadido(this.ficherosLista.size() - 1);
		if (cargarDeBD) {
			fm.cargarDeTabla(BaseDeDatos.getStatement());
		}

	}

	public void add(FicheroMultimedia f) {
		this.ficherosLista.add(f);
		this.avisarAnyadido(this.ficherosLista.size() - 1);
	}

	public void removeFic(int posi) throws IndexOutOfBoundsException {
		this.ficherosLista.remove(posi);
		if (posi == this.ficheroEnCurso) {
			this.ficheroEnCurso = -1;
		} else if (posi < this.ficheroEnCurso) {
			--this.ficheroEnCurso;
		}

		Iterator var3 = this.misEscuchadores.iterator();

		while (var3.hasNext()) {
			ListDataListener ldl = (ListDataListener) var3.next();
			ldl.intervalAdded(new ListDataEvent(this, 2, posi, posi));
		}

	}

	public void clear() {
		this.ficherosLista.clear();
	}

	public boolean irAPrimero() {
		for (this.ficheroEnCurso = 0; this.ficheroEnCurso < this.ficherosLista.size()
				&& ((FicheroMultimedia) this.ficherosLista.get(this.ficheroEnCurso)).erroneo; ++this.ficheroEnCurso) {
		}

		if (this.ficheroEnCurso >= this.ficherosLista.size()) {
			this.ficheroEnCurso = -1;
			return false;
		} else {
			return true;
		}
	}

	public boolean irAUltimo() {
		for (this.ficheroEnCurso = this.ficherosLista.size() - 1; this.ficheroEnCurso >= 0
				&& ((FicheroMultimedia) this.ficherosLista.get(this.ficheroEnCurso)).erroneo; --this.ficheroEnCurso) {
		}

		return this.ficheroEnCurso != -1;
	}

	public boolean irAAnterior() {
		if (this.ficheroEnCurso >= 0) {
			--this.ficheroEnCurso;
		}

		while (this.ficheroEnCurso >= 0 && ((FicheroMultimedia) this.ficherosLista.get(this.ficheroEnCurso)).erroneo) {
			--this.ficheroEnCurso;
		}

		return this.ficheroEnCurso != -1;
	}

	public boolean irASiguiente() {
		++this.ficheroEnCurso;

		while (this.ficheroEnCurso < this.ficherosLista.size()
				&& ((FicheroMultimedia) this.ficherosLista.get(this.ficheroEnCurso)).erroneo) {
			++this.ficheroEnCurso;
		}

		if (this.ficheroEnCurso >= this.ficherosLista.size()) {
			this.ficheroEnCurso = -1;
			return false;
		} else {
			return true;
		}
	}

	public boolean irA(int posi) {
		for (this.ficheroEnCurso = posi; this.ficheroEnCurso < this.ficherosLista.size()
				&& ((FicheroMultimedia) this.ficherosLista.get(this.ficheroEnCurso)).erroneo; ++this.ficheroEnCurso) {
		}

		if (this.ficheroEnCurso >= this.ficherosLista.size()) {
			this.ficheroEnCurso = -1;
			return false;
		} else {
			return true;
		}
	}

	public int getFicSeleccionado() {
		return this.ficheroEnCurso;
	}

	public boolean irARandom() {
		if (this.ficherosLista.size() == 0) {
			this.ficheroEnCurso = -1;
			return false;
		} else {
			for (int i = 0; i < 500; ++i) {
				this.ficheroEnCurso = genAleat.nextInt(this.ficherosLista.size());
				if (!((FicheroMultimedia) this.ficherosLista.get(this.ficheroEnCurso)).erroneo) {
					return true;
				}
			}

			return false;
		}
	}

	public void setFicErroneo(int posi, boolean erroneo) throws IndexOutOfBoundsException {
		((FicheroMultimedia) this.ficherosLista.get(posi)).erroneo = erroneo;
	}

	public boolean isErroneo(int posi) throws IndexOutOfBoundsException {
		return ((FicheroMultimedia) this.ficherosLista.get(posi)).erroneo;
	}

	public int getSize() {
		return this.ficherosLista.size();
	}

	public String getElementAt(int index) {
		return ((FicheroMultimedia) this.ficherosLista.get(index)).file.getName();
	}

	public ArrayList<ListDataListener> initDataListeners() {
		this.misEscuchadores = new ArrayList();
		return this.misEscuchadores;
	}

	public void addListDataListener(ListDataListener l) {
		this.misEscuchadores.add(l);
	}

	public void removeListDataListener(ListDataListener l) {
		this.misEscuchadores.remove(l);
	}

	private void avisarAnyadido(int posi) {
		Iterator var3 = this.misEscuchadores.iterator();

		while (var3.hasNext()) {
			ListDataListener ldl = (ListDataListener) var3.next();
			ldl.intervalAdded(new ListDataEvent(this, 1, posi, posi + 1));
		}

	}

	public void mergeSort(Comparator<FicheroMultimedia> cfm) {
		this.mergeSort(cfm, 0, this.ficherosLista.size() - 1);
		Iterator var3 = this.misEscuchadores.iterator();

		while (var3.hasNext()) {
			ListDataListener ldl = (ListDataListener) var3.next();
			ldl.intervalAdded(new ListDataEvent(this, 0, 0, this.ficherosLista.size()));
		}

	}

	private void mergeSort(Comparator<FicheroMultimedia> cfm, int ini, int fin) {
		if (ini < fin) {
			int med = (ini + fin) / 2;
			this.mergeSort(cfm, ini, med);
			this.mergeSort(cfm, med + 1, fin);
			this.mezclaMergeSort(cfm, ini, med, fin);
		}
	}

	private void mezclaMergeSort(Comparator<FicheroMultimedia> cfm, int ini1, int fin1, int fin2) {
		int initotal = ini1;
		int ini2 = fin1 + 1;
		FicheroMultimedia[] destino = new FicheroMultimedia[fin1 - ini1 + fin2 - ini2 + 2];
		int posDest = 0;

		int posEnCurso;
		for (posEnCurso = -1; ini1 <= fin1 || ini2 <= fin2; ++posDest) {
			boolean menorEsIni1 = true;
			if (ini1 > fin1) {
				menorEsIni1 = false;
			} else if (ini2 <= fin2 && cfm.compare((FicheroMultimedia) this.ficherosLista.get(ini1),
					(FicheroMultimedia) this.ficherosLista.get(ini2)) > 0) {
				menorEsIni1 = false;
			}

			if (menorEsIni1) {
				destino[posDest] = (FicheroMultimedia) this.ficherosLista.get(ini1);
				if (this.ficheroEnCurso == ini1) {
					posEnCurso = posDest;
				}

				++ini1;
			} else {
				destino[posDest] = (FicheroMultimedia) this.ficherosLista.get(ini2);
				if (this.ficheroEnCurso == ini2) {
					posEnCurso = posDest;
				}

				++ini2;
			}
		}

		posDest = 0;

		for (int i = initotal; i <= fin2; ++i) {
			this.ficherosLista.set(i, destino[posDest]);
			if (posEnCurso == posDest) {
				this.ficheroEnCurso = i;
			}

			++posDest;
		}

	}
}
